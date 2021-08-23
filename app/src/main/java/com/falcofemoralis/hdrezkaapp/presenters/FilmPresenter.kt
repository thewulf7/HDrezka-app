package com.falcofemoralis.hdrezkaapp.presenters

import android.widget.ImageView
import com.falcofemoralis.hdrezkaapp.models.ActorModel
import com.falcofemoralis.hdrezkaapp.models.BookmarksModel
import com.falcofemoralis.hdrezkaapp.models.CommentsModel
import com.falcofemoralis.hdrezkaapp.models.FilmModel
import com.falcofemoralis.hdrezkaapp.objects.*
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper.catchException
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException

class FilmPresenter(private val filmView: FilmView, val film: Film) {
    private val COMMENTS_PER_AGE = 18

    private val activeComments: ArrayList<Comment> = ArrayList()
    private val loadedComments: ArrayList<Comment> = ArrayList()
    private var commentsPage = 1
    private var isCommentsLoading: Boolean = false

    fun initFilmData() {
        GlobalScope.launch {
            try {
                if (!film.hasMainData) {
                    FilmModel.getMainData(film)
                }

                if (!film.hasAdditionalData) {
                    FilmModel.getAdditionalData(film)
                }

                withContext(Dispatchers.Main) {
                    filmView.setFilmBaseData(film)
                    filmView.setFilmRatings(film)
                    film.genres?.let { filmView.setGenres(it) }
                    film.countries?.let { filmView.setCountries(it) }
                    film.directors?.let { filmView.setDirectors(it) }
                    film.bookmarks?.let { filmView.setBookmarksList(it) }
                    film.seriesSchedule?.let { filmView.setSchedule(it) }
                    film.collection?.let { filmView.setCollection(it) }
                    film.related?.let { filmView.setRelated(it) }
                    film.title?.let { film.filmLink?.let { it1 -> filmView.setShareBtn(it, it1) } }
                    film.ratingHR.let {
                        film.isHRratingActive.let { it1 ->
                            if (it != null && !film.isAwaiting) {
                                filmView.setHRrating(it.toFloat(), it1)
                            } else{
                                filmView.setHRrating(-1f, false)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                catchException(e, filmView)
                return@launch
            }
        }
    }

    fun initFullSizeImage() {
        film.fullSizePosterPath?.let { filmView.setFullSizeImage(it) }
    }

    fun initActors() {
        if (film.actors != null) {

            val actors = arrayOfNulls<Actor>(film.actors!!.size)

            for ((index, actor) in film.actors!!.withIndex()) {
                GlobalScope.launch {
                    try {
                        actors[index] = ActorModel.getActorMainInfo(actor)

                        if (index == actors.size - 1) {
                            val list: ArrayList<Actor> = ArrayList()
                            for (item in actors) {
                                if (item != null) {
                                    list.add(item)
                                }
                            }

                            withContext(Dispatchers.Main) {
                                filmView.setActors(list)
                            }
                        }
                    } catch (e: Exception) {
                        catchException(e, filmView)
                        return@launch
                    }
                }
            }
        }
    }

    fun initPlayer() {
        film.filmLink?.let { filmView.setPlayer(it) }
    }

    fun setBookmark(bookmarkId: String) {
        film.filmId?.let {
            GlobalScope.launch {
                try {
                    BookmarksModel.postBookmark(it, bookmarkId)
                } catch (e: Exception) {
                    catchException(e, filmView)
                    return@launch
                }
            }
        }
    }

    fun initComments() {
        film.filmId?.let {
            filmView.setCommentEditor(it.toString())
            filmView.setCommentsList(activeComments, it.toString())
            getNextComments()
        }
    }

    fun getNextComments() {
        filmView.setCommentsProgressState(true)

        if (isCommentsLoading) {
            return
        }

        if (loadedComments.size > 0) {
            for ((index, comment) in (loadedComments.clone() as ArrayList<Comment>).withIndex()) {
                activeComments.add(comment)
                loadedComments.removeAt(0)

                if (index == COMMENTS_PER_AGE - 1) {
                    break
                }
            }

            filmView.redrawComments()
            filmView.setCommentsProgressState(false)
        } else {
            isCommentsLoading = true

            GlobalScope.launch {
                try {
                    film.filmId?.let {
                        CommentsModel.getCommentsFromPage(commentsPage, it.toString())
                    }?.let {
                        loadedComments.addAll(it)
                    }

                    commentsPage++
                    isCommentsLoading = false

                    withContext(Dispatchers.Main) {
                        getNextComments()
                    }
                } catch (e: HttpStatusException) {
                    if (e.statusCode != 404) {
                        catchException(e, filmView)
                    }
                    isCommentsLoading = false

                    withContext(Dispatchers.Main) {
                        filmView.setCommentsProgressState(false)
                    }
                    return@launch
                }
            }
        }
    }

    fun addComment(comment: Comment, position: Int) {
        activeComments.add(position, comment)
        filmView.redrawComments()
    }

    fun updateWatch(scheduleItem: Schedule, btn: ImageView) {
        GlobalScope.launch {
            scheduleItem.watchId?.let {
                try {
                    FilmModel.postWatch(it)
                    scheduleItem.isWatched = !scheduleItem.isWatched

                    withContext(Dispatchers.Main) {
                        filmView.changeWatchState(scheduleItem.isWatched, btn)
                    }
                } catch (e: Exception) {
                    catchException(e, filmView)
                }
            }
        }
    }

    fun createNewCatalogue(name: String) {
        GlobalScope.launch {
            try {
                val bookmark: Bookmark = BookmarksModel.postCatalog(name)
                film.filmId?.let { BookmarksModel.postBookmark(it, bookmark.catId) }
                bookmark.isChecked = true
                film.bookmarks?.add(0, bookmark)

                //redraw bookmarks
                withContext(Dispatchers.Main) {
                    film.bookmarks?.let { filmView.setBookmarksList(it) }
                    filmView.updateBookmarksPager()
                }
            } catch (e: Exception) {
                catchException(e, filmView)
            }
        }
    }

    fun updateRating(rating: Float) {
        GlobalScope.launch {
            try {
                FilmModel.postRating(film, rating)

                withContext(Dispatchers.Main) {
                    film.ratingHR?.let { filmView.setHRrating(it.toFloat(), film.isHRratingActive) }
                    filmView.setFilmRatings(film)
                }
            } catch (e: Exception) {
                catchException(e, filmView)
            }
        }
    }

    fun showTranslations(isDownload: Boolean) {
        film.translations?.let { film.isMovieTranslation?.let { it1 -> filmView.showTranslations(it, isDownload, it1) } }
    }

    fun initStreams(translation: Voice, isDownload: Boolean, vararg additionalInfo: String) {
        GlobalScope.launch {
            try {
                val streams: ArrayList<Stream> = FilmModel.parseStreams(translation, film.filmId!!)
                val additionalTitle = StringBuilder()
                translation.name?.let {
                    additionalTitle.append(it)
                }
                for (info in additionalInfo) {
                    if (additionalTitle.isNotEmpty()) additionalTitle.append(" ")
                    additionalTitle.append("$info")
                }

                withContext(Dispatchers.Main) {
                    film.title?.let {
                        if (SettingsData.isMaxQuality == true) {
                            filmView.openStream(streams[streams.size - 1], it, additionalTitle.toString(), isDownload)
                        } else {
                            filmView.showStreams(streams, it, additionalTitle.toString(), isDownload)
                        }
                    }
                }
            } catch (e: Exception) {
                catchException(e, filmView)
            }
        }
    }

    fun initTranslationsSeries(translation: Voice, callback: (seasons: HashMap<String, ArrayList<String>>) -> Unit) {
        GlobalScope.launch {
            try {
                film.filmId?.let {
                    if (translation.seasons == null) {
                        FilmModel.getSeason(it, translation)
                    }

                    withContext(Dispatchers.Main) {
                        translation.seasons?.let { it1 -> callback(it1) }
                    }
                }
            } catch (e: Exception) {
                catchException(e, filmView)
            }
        }
    }

    fun genAndOpenEpisodeStream(translation: Voice, season: String, episode: String, isDownload: Boolean) {
        GlobalScope.launch {
            try {
                film.filmId?.let {
                    translation.streams = FilmModel.getStreamsByEpisodeId(it, translation, season, episode)

                    initStreams(translation, isDownload, "Сезон $season -", "Эпизод $episode")
                }
            } catch (e: Exception) {
                catchException(e, filmView)
            }
        }
    }
}