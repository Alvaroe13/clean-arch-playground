package com.codingwithmitch.cleannotes.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

private const val TAG = "AppDebug" // Tag for logs
private const val DEBUG = true // enable logging

var isUnitTest = false

fun printLogD(className: String?, message: String) {
    if (DEBUG && !isUnitTest) {
        Log.d(TAG, "$className: $message")
    } else if (DEBUG && isUnitTest) {
        println("$className: $message")
    }
}

fun crashLog(msg: String?) {
    msg?.let {
        if (!DEBUG) {
            FirebaseCrashlytics.getInstance().log(it)
        }
    }
}

