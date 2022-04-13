package com.example.learnopengles

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class MatrixSurfaceView(context: Context, attr: AttributeSet? = null) :
    GLSurfaceView(context, attr) {

    init {
        setEGLContextClientVersion(3)
        setRenderer(MatrixRender(this))
        renderMode = RENDERMODE_WHEN_DIRTY
    }

}