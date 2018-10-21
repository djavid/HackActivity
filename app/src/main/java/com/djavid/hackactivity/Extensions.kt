package com.djavid.hackactivity

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.tbruyelle.rxpermissions2.RxPermissions

fun Context?.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this ?: return false,
        permission) == PackageManager.PERMISSION_GRANTED
}

@SuppressLint("CheckResult")
fun AppCompatActivity.requestPermissions(permission: String, action: (Boolean) -> Unit) {
    RxPermissions(this).request(permission)
        .subscribe { action.invoke(it) }
}

fun View.visible(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.invisible(invisible: Boolean) {
    this.visibility = if (invisible) View.INVISIBLE else View.VISIBLE
}

fun Context.getDpi(): Float {
    return this.resources.displayMetrics.density
}

fun Context.toPx(dp: Double): Int {
    return (dp * this.getDpi() + 0.5f).toInt()
}

fun EditText.setTextChangedListener(action: (value: String) -> Unit) {

    addTextChangedListener(object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (s != null) action.invoke(s.toString())
        }
    })
}