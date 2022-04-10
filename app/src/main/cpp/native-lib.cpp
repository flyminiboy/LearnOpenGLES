#include <jni.h>
#include "EGL/egl.h"
#include "EGL/eglext.h" //
#include <GLES3/gl3.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>

//
// Created by fly on 2022/4/7.
//


EGLDisplay eglDisplay = EGL_NO_DISPLAY;
EGLConfig eglConfig;
EGLContext eglContext = EGL_NO_CONTEXT;
EGLSurface eglSurface = EGL_NO_SURFACE;

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_learnopengles_NDKEGLHelper_createEGL(JNIEnv *env, jobject thiz) {
    eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (eglDisplay == EGL_NO_DISPLAY) {
        return -1;
    }

    GLint majorVersion, minorVersion;
    if (!eglInitialize(eglDisplay, &majorVersion, &minorVersion)) {
        return -1;
    }

    EGLint renderable_type = EGL_OPENGL_ES2_BIT | EGL_OPENGL_ES3_BIT_KHR;
    EGLint attrib_list_config[] = {
            EGL_RED_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_ALPHA_SIZE, 8,  //EGL14.EGL_DEPTH_SIZE, 16,
            //EGL14.EGL_STENCIL_SIZE, 8,
            EGL_RENDERABLE_TYPE, renderable_type,
            EGL_NONE
    };

    EGLint num_config;
    if (!eglChooseConfig(eglDisplay, attrib_list_config, &eglConfig, 1, &num_config)) {
        return -1;
    }

    EGLint attrib_list_context[] = {
            EGL_CONTEXT_CLIENT_VERSION, 3,
            EGL_NONE
    };
    eglContext = eglCreateContext(eglDisplay, eglConfig, EGL_NO_CONTEXT, attrib_list_context);
    if (eglContext == EGL_NO_CONTEXT) {
        return -1;
    }

    return 1;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_learnopengles_NDKEGLHelper_createSurface(JNIEnv *env, jobject thiz,
                                                          jobject surface) {
    const EGLint attrib_list[] = {
            EGL_NONE
    };


    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);
    eglSurface = eglCreateWindowSurface(eglDisplay, eglConfig, window, attrib_list);
    if (eglSurface == EGL_NO_SURFACE) {
        return -1;
    }
    return 1;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_learnopengles_NDKEGLHelper_makeCurrent(JNIEnv *env, jobject thiz) {

    if (eglSurface == EGL_NO_SURFACE) {
        return;
    }

    eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_learnopengles_NDKEGLHelper_swap(JNIEnv *env, jobject thiz) {
    eglSwapBuffers(eglDisplay, eglSurface);
}