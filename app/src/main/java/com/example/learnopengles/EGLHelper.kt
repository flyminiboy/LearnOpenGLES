package com.example.learnopengles

import android.opengl.*
import android.view.Surface


class EGLHelper {

    lateinit var eglDisplay: EGLDisplay
    var eglConfig: EGLConfig?=null
    lateinit var eglContext: EGLContext
    lateinit var eglSurface: EGLSurface

    fun start() {
        // 获取 EGLDisplay 对象，建立与本地窗口系统的连接
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)

        if (eglDisplay === EGL14.EGL_NO_DISPLAY) {
            throw java.lang.RuntimeException("eglGetDisplay failed")
        } else {
            logE("eglGetDisplay success")
        }

        val versions = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, versions, 0, versions, 1)) {
            throw RuntimeException("eglInitialize failed [ ${EGL14.eglGetError()} ]")
        } else {
            logE("eglInitialize success")
        }
// 获取FrameBuffer格式和能力
        // 获取FrameBuffer格式和能力

        // The actual surface is generally RGBA or RGBX, so situationally omitting alpha
        // doesn't really help.  It can also lead to a huge performance hit on glReadPixels()
        // when reading into a GL_RGBA buffer.
        var renderableType = EGL14.EGL_OPENGL_ES2_BIT
        renderableType = renderableType or EGLExt.EGL_OPENGL_ES3_BIT_KHR
        renderableType = EGLExt.EGL_OPENGL_ES3_BIT_KHR
        val configAttribs = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,  //EGL14.EGL_DEPTH_SIZE, 16,
            //EGL14.EGL_STENCIL_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, renderableType,
            EGL14.EGL_NONE
        )

        val eglConfigs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        EGL14.eglChooseConfig(
            eglDisplay,
            configAttribs,
            0,
            eglConfigs,
            0,
            eglConfigs.size,
            numConfigs,
            0
        )

        eglConfig = eglConfigs[0]!!

        // 创建OpenGL上下文
        val contextAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
            EGL14.EGL_NONE
        )
        eglContext =
            EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
        if (eglContext == EGL14.EGL_NO_CONTEXT) {
            throw RuntimeException("eglCreateContext failed [ ${EGL14.eglGetError()} ]")
        } else {
            logE("eglCreateContext success")
        }

    }

    fun createSurface(surface: Surface) {
        // 创建窗口
        val surfaceAttribs = intArrayOf(
            EGL14.EGL_NONE
        )
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surface, surfaceAttribs, 0)
        if (eglSurface == EGL14.EGL_NO_SURFACE) {

            when (EGL14.eglGetError()) {
                EGL14.EGL_BAD_MATCH -> logE("EGL_BAD_MATCH")
                EGL14.EGL_BAD_CONFIG -> logE("EGL_BAD_CONFIG")
                EGL14.EGL_BAD_NATIVE_WINDOW -> logE("EGL_BAD_NATIVE_WINDOW")
                EGL14.EGL_BAD_ALLOC -> logE("EGL_BAD_ALLOC")
            }

        } else {
            if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
                logE("eglMakeCurrent failed [${EGL14.eglGetError()}]")
            } else {
                logE("eglMakeCurrent success")
            }
        }
    }

    fun swap() {



        if (!EGL14.eglSwapBuffers(eglDisplay, eglSurface)) {
            logE("eglSwapBuffers failed [${EGL14.eglGetError()}]")
        } else {
            logE("eglSwapBuffers success")
        }
    }

    fun releaseSurface() {
        EGL14.eglDestroySurface(eglDisplay, eglSurface)
        eglSurface = EGL14.EGL_NO_SURFACE
    }

    fun release() {
        if (eglDisplay !== EGL14.EGL_NO_DISPLAY) {
            // Android is unusual in that it uses a reference-counted EGLDisplay.  So for
            // every eglInitialize() we need an eglTerminate().
            EGL14.eglMakeCurrent(
                eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT
            )
            EGL14.eglDestroyContext(eglDisplay, eglContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(eglDisplay)
        }

        eglDisplay = EGL14.EGL_NO_DISPLAY
        eglContext = EGL14.EGL_NO_CONTEXT
        eglConfig = null
    }

}