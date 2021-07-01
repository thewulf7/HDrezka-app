package com.BSLCommunity.onlinefilmstracker.viewsInterface

import com.BSLCommunity.onlinefilmstracker.objects.Film

interface NewestFilmsView {
    fun setFilms(films: ArrayList<Film>)

    fun redrawFilms()

    fun setProgressBarState(state: Boolean)

    fun showStopToast()
}