package com.example.learnopengles

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class TriangleRender(val surfaceView: TriangleSurfaceView) : GLSurfaceView.Renderer {

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

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        logE("onSurfaceCreated")

        val vertexShaderSource = loadShaderSource(context, R.raw.triangle_vertex_shader)
        val fragmentShaderSource = loadShaderSource(context, R.raw.triangle_fragment_shader)

        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderSource)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderSource)

        program = createAndLinkProgrm(context, vertexShader, fragmentShader)

        // 链接顶点属性
        // 第一个参数指定我们要配置的顶点属性。
        // 还记得我们在顶点着色器中使用layout(location = 0)定义了position顶点属性的位置值(Location)吗？
        // 它可以把顶点属性的位置值设置为0。因为我们希望把数据传递到这一个顶点属性中，所以这里我们传入0

        // 第二个参数指定顶点属性的大小。这里我们在定义顶点数组是定义了一个2D的图形，只有x,y俩个维度，所以大小是2
        // 第三个参数指定数据的类型，这里是GL_FLOAT(GLSL中vec*都是由浮点数值组成的)

        // 是否希望数据被标准化(Normalize)。如果我们设置为GL_TRUE，所有数据都会被映射到0（对于有符号型signed数据是-1）到1之间。
        // 我们把它设置为GL_FALSE

        // 步长(Stride)，它告诉我们在连续的顶点属性组之间的间隔 由于下个组位置数据在2个float之后 我们把步长设置为3 * sizeof(float)4
        // 表示位置数据在缓冲中起始位置的偏移量,由于位置数据在数组的开头，所以这里是0.在本示例中我们指定以了位置数据
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 2 * 4, verBuffer)
        // 现在我们已经定义了OpenGL该如何解释顶点数据，我们现在应该使用glEnableVertexAttribArray，以顶点属性位置值作为参数，启用顶点属性
        GLES30.glEnableVertexAttribArray(0)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        logE("onSurfaceChanged")
        // 设置窗口的维度
        // glViewport函数前两个参数控制窗口左下角的位置。第三个和第四个参数控制渲染窗口的宽度和高度（像素）
        // OpenGL幕后使用glViewport中定义的位置和宽高进行2D坐标的转换，将OpenGL中的位置坐标转换为你的屏幕坐标
        GLES30.glViewport(0, 0, width, height)
    }

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

            // 使用当前激活的着色器，之前定义的顶点属性配置，和VBO的顶点数据（通过VAO间接绑定）来绘制图元。
            // 第一个参数是我们打算绘制的OpenGL图元的类型
            // 指定了顶点数组的起始索引
            // 指定我们打算绘制多少个顶点
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
        }


    }

}