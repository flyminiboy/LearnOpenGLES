package com.example.learnopengles

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class TextureSurfaceView(context: Context, attrs: AttributeSet?=null):GLSurfaceView(context, attrs) {

    init {
        setEGLContextClientVersion(3)
        setRenderer(TextureRender(this))
        renderMode = RENDERMODE_WHEN_DIRTY
    }

}