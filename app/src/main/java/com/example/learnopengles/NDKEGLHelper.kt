package com.example.learnopengles

import android.opengl.EGLSurface
import android.view.Surface

object NDKEGLHelper {

    init {
        System.loadLibrary("native-lib")
    }

    external fun createEGL(): Int

    external fun createSurface(surface: Surface): Int

    external fun makeCurrent()

    external fun swap()

}