package com.djavid.hackactivity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.WindowManager
import com.djavid.hackactivity.data.Event
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_place.*
import kotlinx.android.synthetic.main.layout_progress.*

class PlaceActivity : AppCompatActivity() {

    private var disposable: Disposable? = null
    private var placeId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place)
        setRecycler()

        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val navbarHeight = resources.getDimensionPixelSize(resourceId)
        frameLayout.setPadding(0, 0, 0, navbarHeight)

        placeId = intent.getIntExtra("placeId", -1)
        if (placeId != -1) getPlaceEvents(placeId, true)
        toolbarTitle.text = intent.getStringExtra("placeName")
        backButton.setOnClickListener {
            finish()
        }
        plusButton.setOnClickListener {
            val intent = Intent(this, NewEventActivity::class.java).apply {
                putExtra("placeId", placeId)
            }
            startActivity(intent)
        }

        swipeRefreshLayout.setOnRefreshListener { getPlaceEvents(placeId) }
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.blue))
    }

    override fun onResume() {
        super.onResume()
        if (placeId != -1) getPlaceEvents(placeId)
    }

    private fun setRecycler() {
        eventsRecycler.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(10)
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun getPlaceEvents(eventId: Int, refreshWhole: Boolean = false) {
        disposable?.dispose()
        disposable = App.getApi().getPlaceEvents(App.getPreferences().getInt("token", 2), eventId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                if (refreshWhole) showProgress(true)
            }
            .doOnEvent { _, _ ->
                showProgress(false)
                swipeRefreshLayout.isRefreshing = false
            }
            .subscribe({
                if (it != null && it.isNotEmpty())
                    showEvents(it)
            }, {
                it.printStackTrace()
            })
    }

    private fun showEvents(events: List<Event>) {
        eventsRecycler.removeAllViews()
        events.forEach {
            eventsRecycler.addView(EventItem(this, it))
        }
    }

    private fun showProgress(show: Boolean) {
        progressLayout.visible(show)
    }

}
