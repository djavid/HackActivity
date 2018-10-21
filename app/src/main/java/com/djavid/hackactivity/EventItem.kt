package com.djavid.hackactivity

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.content.ContextCompat
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.djavid.hackactivity.data.Event
import com.mindorks.placeholderview.annotations.Layout
import com.mindorks.placeholderview.annotations.Position
import com.mindorks.placeholderview.annotations.Resolve
import com.mindorks.placeholderview.annotations.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import java.util.*

@Layout(R.layout.item_event)
class EventItem(
    private val context: Context?,
    private val event: Event
) {

    private var disposable: Disposable? = null

    @View(R.id.eventTitle)
    lateinit var eventTitle: TextView

    @View(R.id.joinButton)
    lateinit var joinButton: TextView

    @View(R.id.activityIcon)
    lateinit var activityIcon: ImageView

    @View(R.id.activityTitle)
    lateinit var activityTitle: TextView

    @View(R.id.membersCountIcon)
    lateinit var membersCountIcon: ImageView

    @View(R.id.membersCountTitle)
    lateinit var membersCountTitle: TextView

    @View(R.id.dateIcon)
    lateinit var dateIcon: ImageView

    @View(R.id.dateTitle)
    lateinit var dateTitle: TextView

    @View(R.id.timeIcon)
    lateinit var timeIcon: ImageView

    @View(R.id.timeTitle)
    lateinit var timeTitle: TextView

    @View(R.id.progressBar)
    lateinit var progressBar: ProgressBar

    @View(R.id.description)
    lateinit var description: TextView

    @View(R.id.descFrameLayout)
    lateinit var descFrameLayout: FrameLayout

    @JvmField
    @Position
    var position: Int = 0

    private var membersCount = 0


    @Resolve
    fun onResolved() {
        context ?: return

        eventTitle.text = event.name
        setEventJoined(event.joined == 1)
        joinButton.setOnClickListener { joinEvent(event.id) }

        activityTitle.text = event.type
        membersCount = event.membersCount
        membersCountTitle.text = "$membersCount / ${event.maxAllowed}"

        val start = DateTime(event.dateTime * 1000)
        val end = start.plusMinutes(event.duration)

        dateTitle.text = start.toString("MMMMMMMMM dd, yyyy", Locale.US)
        timeTitle.text = start.toString("hh:mm") + " - " + end.toString("hh:mm")

        if (event.description.isNotEmpty()) {
            description.text = event.description
        } else {
            descFrameLayout.visible(false)
            description.visible(false)
        }
    }

    @SuppressLint("CheckResult")
    private fun joinEvent(eventId: Int) {
        disposable?.dispose()
        disposable = App.getApi().joinEvent(App.getPreferences().getInt("token", 2), eventId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showJoinProgress(true) }
            .doOnEvent { _, _ -> showJoinProgress(false) }
            .subscribe({
                when (it) {
                    2 -> setEventJoined(false)
                    1 -> setEventJoined(true)
                }
            }, {
                it.printStackTrace()
            })
    }

    private fun showJoinProgress(show: Boolean) {
        progressBar.visible(show)
        joinButton.visible(!show)
    }

    private fun setEventJoined(joined: Boolean) {
        context ?: return

        joinButton.text = if (joined) context.getString(R.string.joined) else context.getString(R.string.join)
        joinButton.setTextColor(
            if (joined) ContextCompat.getColor(context, R.color.green)
            else ContextCompat.getColor(context, R.color.blue)
        )

        membersCount = if (joined) membersCount + 1 else membersCount - 1
        membersCountTitle.text = "$membersCount / ${event.maxAllowed}"
    }

}