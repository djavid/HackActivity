package com.djavid.hackactivity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import es.dmoral.toasty.Toasty
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_new_event.*
import kotlinx.android.synthetic.main.layout_progress.*
import org.joda.time.DateTime
import java.util.*
import net.danlew.android.joda.ResUtils.getIdentifier



class NewEventActivity : AppCompatActivity() {

    private var dateTime: DateTime? = null
    private var disposable: Disposable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_event)

        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val navbarHeight = resources.getDimensionPixelSize(resourceId)
        frameLayout.setPadding(0, 0, 0, navbarHeight)

        backButton.setOnClickListener { finish() }
        toolbarTitle.text = getString(R.string.create_new_event)
        setInputs()

        val placeId = intent.getIntExtra("placeId", -1)
        dateInput.setOnClickListener { createDatePicker() }
        submitBtn.setOnClickListener { if (placeId != -1) submit(placeId) }
    }

    private fun setInputs() {
        setSubmitActive(false)
        activityTypeSpinner.setSelection(0)

        nameInput.setTextChangedListener { setSubmitActive(validateInputs()) }
        descriptionInput.setTextChangedListener { setSubmitActive(validateInputs()) }
        dateInput.setTextChangedListener { setSubmitActive(validateInputs()) }
        maxAllowedInput.setTextChangedListener { setSubmitActive(validateInputs()) }
        durationInput.setTextChangedListener { setSubmitActive(validateInputs()) }
    }

    private fun validateInputs(): Boolean {
        if (nameInput.text.isEmpty()) return false
        if (descriptionInput.text.isEmpty()) return false
        if (dateInput.text.isEmpty()) return false
        if (maxAllowedInput.text.isEmpty()) return false
        if (durationInput.text.isEmpty()) return false

        return true
    }

    private fun submit(placeId: Int) {
        disposable?.dispose()
        dateTime ?: return

        val date = dateTime!!.millis / 1000
        disposable = App.getApi().createEvent(nameInput.text.toString(), date, maxAllowedInput.text.toString().toInt(),
            durationInput.text.toString().toInt(), descriptionInput.text.toString(), activityTypeSpinner.selectedItem.toString(),
            placeId, App.getPreferences().getInt("token", 2))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showProgress(true) }
            .doOnEvent { _, _ -> showProgress(false) }
            .subscribe({
                if (it) {
                    Toasty.success(this, "Event successfully created").show()
                    finish()
                } else {
                    Toasty.error(this, "Error occured").show()
                }
            }, {
                it.printStackTrace()
                Toasty.error(this, "Error occured").show()
            })
    }

    private fun createDatePicker() {

        val calendar = Calendar.getInstance()

        DatePickerDialog(this, { view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            createTimePicker(calendar)
        },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun createTimePicker(calendar: Calendar) {

        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)

            dateTime = DateTime(calendar)

            if (dateTime!!.isBeforeNow) {
                dateTime = null
                Toasty.warning(this, "You can't set choose in the past!").show()
            } else {
                dateInput.setText(dateTime?.toString("dd/MM/yyyy HH:mm") ?: "")
            }
        },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun setSubmitActive(active: Boolean) {
        submitBtn.isEnabled = active
        submitBtn.alpha = if (active) 1f else 0.5f
    }

    private fun showProgress(show: Boolean) {
        progressLayout.visible(show)
    }
}
