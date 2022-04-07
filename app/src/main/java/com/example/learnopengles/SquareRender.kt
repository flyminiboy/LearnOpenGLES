package com.example.learnopengles

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SquareRender(val surfaceView: GLSurfaceView) : GLSurfaceView.Renderer {

    val context by lazy {
        surfaceView.context
    }

    var program: Int? = null

    /**
     * 顶点坐标
     *
     * 定义这样的顶点数据以后，我们会把它作为输入发送给图形渲染管线的第一个处理阶段：顶点着色器
     *
     * 它会在GPU上创建内存用于储存我们的顶点数据
     *
     * 我们通过顶点缓冲对象(Vertex Buffer Objects, VBO)管理这个内存,类型 GL_ARRAY_BUFFER
     */
//    val vexCoords = floatArrayOf(
//        -0.5f, -0.5f,
//        0.5f, -0.5f,
//        -0.5f, 0.5f,
//
//        0.5f, -0.5f,
//        -0.5f, 0.5f,
//        0.5f, 0.5f,
//
//    )

    val vexCoords = floatArrayOf(
        -0.5f, -0.5f,
        0.5f, -0.5f,
        -0.5f, 0.5f,
        0.5f, 0.5f
    )

    val index = intArrayOf(
        0, 1, 2, 1, 2, 3
    )
    val indexBuffer by lazy {
        ByteBuffer.allocateDirect(index.size * 4)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer().apply {
                put(index)
                position(0)
            }
    }

    val verBuffer by lazy {
        // Must use a native order direct Buffer
        ByteBuffer.allocateDirect(vexCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().also { buffer ->
                buffer.put(vexCoords)
                buffer.position(0)
            }
    }

    val vbos = IntArray(2)
    val vaos = IntArray(2)

    val vexCoordsTriangle = floatArrayOf(
        -0.5f, -0.5f,
        0.5f, -0.5f,
        0f, 0.5f
    )

    val verBufferTriangle by lazy {
        // Must use a native order direct Buffer
        ByteBuffer.allocateDirect(vexCoordsTriangle.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().also { buffer ->
                buffer.put(vexCoordsTriangle)
                buffer.position(0)
            }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        logE("onSurfaceCreated")

        val vertexShaderSource = loadShaderSource(context, R.raw.triangle_vertex_shader)
        val fragmentShaderSource = loadShaderSource(context, R.raw.triangle_fragment_shader)

        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderSource)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderSource)

        program = createAndLinkProgrm(context, vertexShader, fragmentShader)

        // VAO
        GLES30.glGenVertexArrays(2, vaos, 0)
        // VBO
        GLES30.glGenBuffers(2, vbos, 0)
        // 要想使用VAO，要做的只是使用glBindVertexArray绑定VAO
        GLES30.glBindVertexArray(vaos[0])
        // 复制顶点数组到缓冲中供OpenGL使用
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbos[0])
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            vexCoords.size * 4,
            verBuffer,
            GLES30.GL_STATIC_DRAW
        )

        // 使用VBO
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 2 * 4, 0)
        // 现在我们已经定义了OpenGL该如何解释顶点数据，我们现在应该使用glEnableVertexAttribArray，以顶点属性位置值作为参数，启用顶点属性
        GLES30.glEnableVertexAttribArray(0)

        // 要想使用VAO，要做的只是使用glBindVertexArray绑定VAO
        GLES30.glBindVertexArray(vaos[1])
        // 复制顶点数组到缓冲中供OpenGL使用
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbos[1])
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            vexCoordsTriangle.size * 4,
            verBufferTriangle,
            GLES30.GL_STATIC_DRAW
        )

        // 使用VBO
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 2 * 4, 0)
        // 现在我们已经定义了OpenGL该如何解释顶点数据，我们现在应该使用glEnableVertexAttribArray，以顶点属性位置值作为参数，启用顶点属性
        GLES30.glEnableVertexAttribArray(0)

        //8. 解绑VBO VAO
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glBindVertexArray(0)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        logE("onSurfaceChanged")
        // 设置窗口的维度
        // glViewport函数前两个参数控制窗口左下角的位置。第三个和第四个参数控制渲染窗口的宽度和高度（像素）
        // OpenGL幕后使用glViewport中定义的位置和宽高进行2D坐标的转换，将OpenGL中的位置坐标转换为你的屏幕坐标
        GLES30.glViewport(0, 0, width, height)
    }

    var i = 0
    override fun onDrawFrame(gl: GL10?) {

        logE("onDrawFrame")

        program?.let {

            // 使用一个自定义的颜色清空屏幕
            // 在每个新的渲染迭代开始的时候我们总是希望清屏，否则我们仍能看见上一次迭代的渲染结果

            // 设置清空屏幕所用的颜色 当调用glClear函数，清除颜色缓冲之后，整个颜色缓冲都会被填充为glClearColor里所设置的颜色
            GLES30.glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
            // 我们可以通过调用glClear函数来清空屏幕的颜色缓冲,glClear它接受一个缓冲位(Buffer Bit)来指定要清空的缓冲,这里我们只关心颜色
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

            // 激活程序对象
            // 在glUseProgram函数调用之后，每个着色器调用和渲染调用都会使用这个程序对象（也就是之前写的着色器)了
            GLES30.glUseProgram(it)

            if (i % 2 == 0) {
                logE("999999999")
                GLES30.glBindVertexArray(vaos[0]) // TODO  重要
            } else {
                logE("ffffffffff")
                GLES30.glBindVertexArray(vaos[1]) // TODO  重要
            }


            // 使用当前激活的着色器，之前定义的顶点属性配置，和VBO的顶点数据（通过VAO间接绑定）来绘制图元。
            // 第一个参数是我们打算绘制的OpenGL图元的类型
            // 指定了顶点数组的起始索引
            // 指定我们打算绘制多少个顶点


//            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6)

            // 注意第三个参数type的选择
            // GLES30.GL_UNSIGNED_INT
            // GLES30.GL_UNSIGNED_SHORT
            GLES30.glDrawElements(GLES30.GL_TRIANGLE_FAN, 6, GLES30.GL_UNSIGNED_INT, indexBuffer)


            surfaceView.postDelayed({
                i++
                surfaceView.requestRender()
            }, 5000L)

            // 资源释放操作，这个现在使用GLSurfaceView 我先不关心这部分代码。
//            GLES30.glDeleteVertexArrays(1, )
//            GLES30.glDeleteBuffers(1, &VBO)
//            GLES30.glDeleteBuffers(1, &EBO)
//            GLES30.glDeleteProgram(shaderProgram)

        }


    }

}