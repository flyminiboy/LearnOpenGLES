package com.example.learnopengles

import android.content.Context
import android.opengl.GLES30
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CameraSurfaceView(context: Context, attr: AttributeSet? = null) : SurfaceView(context, attr) {

    val glThread = GLThread()
    val glHandler by lazy {
        glThread.start()
        Handler(glThread.looper)
    }
//    val eglHelper = EGLHelper()

    val vexCoords = floatArrayOf(
        -0.5f, -0.5f,
        0.5f, -0.5f,
        0f, 0.5f
    )

    val verBuffer by lazy {
        // Must use a native order direct Buffer
        ByteBuffer.allocateDirect(vexCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().also { buffer ->
                buffer.put(vexCoords)
                buffer.position(0)
            }
    }

    var program: Int = 0

    init {

        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {

            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                mWidth = width
                mHeight = height
                glThread.render()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {

            }

        })

        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    var mWidth:Int = 0;
    var mHeight:Int = 0;

    inner class GLThread : HandlerThread("opengl") {

        fun render() {
            glHandler.post {

                NDKEGLHelper.createEGL()
                NDKEGLHelper.createSurface(holder.surface)
                NDKEGLHelper.makeCurrent()

//                eglHelper.start()
//                eglHelper.createSurface(holder.surface)

                val vertexShader = loadShaderSource(context, R.raw.triangle_vertex_shader).run {
                    loadShader(GLES30.GL_VERTEX_SHADER, this)
                }
                val fragmentShader = loadShaderSource(context, R.raw.triangle_fragment_shader).run {
                    loadShader(GLES30.GL_FRAGMENT_SHADER, this)
                }
                logE("createandlink [$vertexShader - $fragmentShader]")
                program = createAndLinkProgrm(context, vertexShader, fragmentShader)
                logE("createandlink result [$program]")

                GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 2 * 4, verBuffer)
                // 现在我们已经定义了OpenGL该如何解释顶点数据，我们现在应该使用glEnableVertexAttribArray，以顶点属性位置值作为参数，启用顶点属性
                GLES30.glEnableVertexAttribArray(0)

                GLES30.glViewport(0, 0, mWidth, mHeight)

                GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
                GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

                GLES30.glUseProgram(program)

                GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)

//                eglHelper.swap()
                NDKEGLHelper.swap()

//                eglHelper.releaseSurface()
//                eglHelper.release()

            }
        }
    }

}