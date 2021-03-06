package com.falcofemoralis.hdrezkaapp.models

import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import org.jsoup.HttpStatusException
import org.jsoup.nodes.Document

object FilmsListModel {
    const val FILMS = "div.b-content__inline_item"
    const val FILM_IMG = "div.b-content__inline_item-cover a img"
    const val SUB_INFO = "div.b-content__inline_item-cover a span.info"

    fun getFilmsFromPage(doc: Document): ArrayList<Film> {
        val films: ArrayList<Film> = ArrayList()

        for (el in doc.select(FILMS)) {
            val film = Film(el.attr("data-id").toInt())
            film.filmLink = el.attr("data-url")
            if(film.filmLink.isNullOrEmpty()){
                film.filmLink = el.select("div.b-content__inline_item-cover a").attr("href")

                if(film.filmLink.isNullOrEmpty()){
                    film.filmLink = el.select("div.b-content__inline_item-link a").attr("href")
                }
            }
            film.posterPath = el.select(FILM_IMG).attr("src")
            film.subInfo = el.select(SUB_INFO).text()

            val text = el.select("div.b-content__inline_item-link div").text()
            val separated = text.split(",")
            film.year = separated[0]
            film.countries = ArrayList()
            film.countries!!.add(separated[1].drop(1))
            films.add(film)
        }

        return films
    }
}