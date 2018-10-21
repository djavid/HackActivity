package com.djavid.hackactivity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.WindowManager
import com.djavid.hackactivity.data.Event
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_recommendations.*
import kotlinx.android.synthetic.main.layout_progress.*

class RecommendationsActivity : AppCompatActivity() {

    private var disposable: Disposable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommendations)
        setRecycler()

        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val navbarHeight = resources.getDimensionPixelSize(resourceId)
        frameLayout.setPadding(0, 0, 0, navbarHeight)
        backButton.setOnClickListener { finish() }

        getRecommendedEvents()
    }

    private fun setRecycler() {
        eventsRecycler.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(10)
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun getRecommendedEvents() {
        disposable?.dispose()
        disposable = App.getApi().getRecommendedEvents(App.getPreferences().getInt("token", 2))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showProgress(true) }
            .doOnEvent { _, _ -> showProgress(false) }
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
