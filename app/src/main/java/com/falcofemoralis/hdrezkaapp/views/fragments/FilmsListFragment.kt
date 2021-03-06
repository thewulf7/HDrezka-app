package com.falcofemoralis.hdrezkaapp.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.constants.AdapterAction
import com.falcofemoralis.hdrezkaapp.interfaces.IProgressState
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import com.falcofemoralis.hdrezkaapp.utils.FragmentOpener
import com.falcofemoralis.hdrezkaapp.views.adapters.FilmsListRecyclerViewAdapter
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmListCallView
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmsListView

open class FilmsListFragment : Fragment(), FilmsListView {
    private lateinit var currentView: View
    private lateinit var viewList: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: NestedScrollView
    private lateinit var fragmentListener: OnFragmentInteractionListener
    private lateinit var callView: FilmListCallView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentListener = context as OnFragmentInteractionListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currentView = inflater.inflate(R.layout.fragment_films_list, container, false)

        progressBar = currentView.findViewById(R.id.fragment_films_list_pb_data_loading)

        viewList = currentView.findViewById(R.id.fragment_films_list_rv_films)
        viewList.layoutManager = SettingsData.filmsInRow?.let { GridLayoutManager(context, it) }

        scrollView = currentView.findViewById(R.id.fragment_films_list_nsv_films)
        scrollView.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
                val view = scrollView.getChildAt(scrollView.childCount - 1)
                val diff = view.bottom - (scrollView.height + scrollView.scrollY)

                if (diff == 0) {
                    setProgressBarState(IProgressState.StateType.LOADING)
                    callView.triggerEnd()
                }
            }
        })

        callView.onFilmsListCreated()

        return currentView
    }

    fun setCallView(cv: FilmListCallView) {
        callView = cv
    }

    override fun setFilms(films: ArrayList<Film>) {
        viewList.adapter = context?.let { FilmsListRecyclerViewAdapter(it, films, ::listCallback) }
    }

    override fun redrawFilms(from: Int, count: Int, action: AdapterAction) {
        when (action) {
            AdapterAction.ADD -> viewList.adapter?.notifyItemRangeInserted(from, count)
            AdapterAction.UPDATE -> viewList.adapter?.notifyItemRangeChanged(from, count)
            AdapterAction.DELETE -> viewList.adapter?.notifyItemRangeRemoved(from, count)
        }
        callView.dataInited()
    }

    override fun setProgressBarState(type: IProgressState.StateType) {
        progressBar.visibility = when (type) {
            IProgressState.StateType.LOADED -> View.GONE
            IProgressState.StateType.LOADING -> View.VISIBLE
        }
    }

    private fun listCallback(film: Film) {
        FragmentOpener.openWithData(this, fragmentListener, film, "film")
    }
}