package com.example.learnopengles

import android.app.Activity
import android.widget.Toast

fun Activity.toast(msg:String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

fun Activity.tipAll(msg: String) {
    logE(msg)
    toast(msg)
}