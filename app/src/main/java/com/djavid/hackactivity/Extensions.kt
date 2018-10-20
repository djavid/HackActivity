package com.djavid.hackactivity

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
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