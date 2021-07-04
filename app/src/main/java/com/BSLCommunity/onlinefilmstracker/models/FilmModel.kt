package com.BSLCommunity.onlinefilmstracker.models

import com.BSLCommunity.onlinefilmstracker.objects.Film
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException

object FilmModel {
    private val FILM_LINK = "div.b-content__inline_item-cover a"
    private val FILM_TITLE = "div.b-post__title h1"
    private val FILM_POSTER = "div.b-sidecover a img"
    private val FILM_TABLE_INFO = "table.b-post__info tbody tr"
    private val FILM_IMDB_RATING = "span.imdb span"

    private fun getFilmPage(link: String): Document {
        return Jsoup.connect(link).get()
    }

    fun getMainData(element: Element, callback: (Film?) -> Unit) {
        GlobalScope.launch {
            val link: String = element.select(FILM_LINK).attr("href")

            try {
                val filmPage: Document = getFilmPage(link)
                val table: Elements = filmPage.select(FILM_TABLE_INFO)

                val title: String = filmPage.select(FILM_TITLE).text()
                val type: String = element.select("span.cat i")[0].text()
                val posterPath: String = filmPage.select(FILM_POSTER).attr("src")
                var ratingIMDB: String? = null
                var date = ""
                var year = ""
                val countries: ArrayList<String> = ArrayList()
                val genres: ArrayList<String> = ArrayList()

                // Parse info table
                for (tr in table) {
                    val td: Elements = tr.select("td")
                    if (td.size > 0) {
                        val h2Els: Elements = td[0].select("h2")
                        if (h2Els.size > 0) {
                            val h2: Element = h2Els[0]

                            when (h2.text()) {
                                "Рейтинги" -> {
                                    ratingIMDB = td[1].select(FILM_IMDB_RATING).text()
                                }
                                "Дата выхода" -> {
                                    date = td[1].ownText()
                                    year = td[1].select("a").text()
                                }
                                "Страна" -> {
                                    for (el in td[1].select("a")) {
                                        countries.add(el.text())
                                    }
                                }
                                "Жанр" -> {
                                    for (el in td[1].select("a")) {
                                        genres.add(el.select("span").text())
                                    }
                                }
                            }
                        }
                    }
                }

                callback(Film(link, title, date, year, posterPath, countries, ratingIMDB, genres, type))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun setAdditionalData(film: Film) {
        val document: Document = getFilmPage(film.link)
        film.origTitle = document.select("div.b-post__origtitle").text()
        film.description = document.select("div.b-post__description_text").text()
        film.votes = document.select("span.imdb i").text()
        film.runtime = document.select("td[itemprop=duration]").text()

        val actors: ArrayList<String> = ArrayList()
        val directors: ArrayList<String> = ArrayList()

        val personsElements: Elements = document.select("div.persons-list-holder")
        for (el in personsElements) {
            val els: Elements = el.select("span.item")

            if (el.select("span.inline h2").text() == "В ролях актеры") {
                for (actorElement in els) {
                    val actor: String = actorElement.select("span a span").text()
                    if (actor.isNotEmpty()) {
                        actors.add(actor)
                    }
                }
            } else {
                for (directorElement in els) {
                    directors.add(directorElement.select("span a span").text())
                }
            }
        }

        film.directors = directors
        film.actors = actors
    }
}