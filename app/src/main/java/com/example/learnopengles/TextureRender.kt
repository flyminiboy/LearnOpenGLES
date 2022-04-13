package com.example.learnopengles

import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.view.TextureView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL10

class TextureRender(val surfaceView: TextureSurfaceView) : GLSurfaceView.Renderer {

    var program: Int? = null

    // 不同的绘制方式，一定要注意自己的顶点顺序，
    // 其实不管是顶点法还是索引法，本质都是一样的，顶点法无法共享顶点，需要我们自己把定重复的顶点都定义出来。
    // 索引法，就是可以一直共享这些顶点，通过索引去取值，实现一个排序，我们把索引法，定义的序列，都写出来其实就是我们的顶点法定义的顶点顺序。
    // GL_TRIANGLE_FAN 该方式下 绘制矩形，需要的顶点 第一个点和之后所有相邻的两个点构成一个三角形 012 023 034
    val vertexCoords = floatArrayOf(
        -1.0f, -1.0f, // 左下
        1.0f, -1.0f, // 右下
        1.0f, 1.0f, // 右上
        -1.0f, 1.0f // 左上
    )

//        val vertexCoords = floatArrayOf(
//        -1.0f, -1.0f, // 左下
//        1.0f, -1.0f, // 右下
//        -1.0f, 1.0f, // 左上
//        1.0f, 1.0f // 右上
//    )

    // GL_TRIANGLES 该方式下 绘制矩形，需要的顶点 每3个点构成一个三角形 012 345
//    val vertexCoords = floatArrayOf(
//        -1.0f, -1.0f, // 左下
//        1.0f, -1.0f, // 右下
//        -1.0f, 1.0f, // 左上
//        -1.0f, 1.0f, // 左上
//        1.0f, -1.0f, // 右下
//        1.0f, 1.0f // 右上
//    )

    // GL_TRIANGLES_STRIP 相邻3个点构成一个三角形，不包括收尾两个点 012 123 4个顶点例子，
//    val vertexCoords = floatArrayOf(
//        -1.0f, -1.0f, // 左下
//        1.0f, -1.0f, // 右下
//        -1.0f, 1.0f, // 左上
//        1.0f, 1.0f // 右上
//    )

    // GL_TRIANGLES_STRIP 相邻3个点构成一个三角形，不包括收尾两个点 012 123 5个顶点例子，
//    val vertexCoords = floatArrayOf(
//        -1.0f, -1.0f, // 左下
//        1.0f, -1.0f, // 右下
//        1.0f, 1.0f, // 右上
//        -1.0f, 1.0f, // 左上
//        -1.0f, -1.0f // 左下
//    )

    // 纹理坐标 这样最终绘制出来的图片是倒的 转了180度

    // GL_TRIANGLE_FAN
//    val textureCoords = floatArrayOf(
//        0.0f, 0.0f, // 左下
//        1.0f, 0.0f, // 右下
//        1.0f, 1.0f, // 右上
//        0.0f, 1.0f // 左上
//    )



//    做一个镜像
//    val textureCoords = floatArrayOf(
//        1.0f, 1.0f, // 右上
//        0.0f, 1.0f, // 左上
//        0.0f, 0.0f, // 左下
//        1.0f, 0.0f // 右下
//    )

    // 修正纹理显示倒 旋转180度 上下倒
    val textureCoords = floatArrayOf(
        0.0f, 1.0f, // 左上
        1.0f, 1.0f, // 右上
        1.0f, 0.0f, // 右下
        0.0f, 0.0f // 左下
    )

//    val textureCoords = floatArrayOf(
//        0.0f, 0.0f, // 左下
//        1.0f, 0.0f, // 右下
//        0.0f, 1.0f, // 左上
//        1.0f, 1.0f // 右上
//    )

    // GL_TRIANGLES
//    val textureCoords = floatArrayOf(
//        0.0f, 0.0f, // 左下
//        1.0f, 0.0f, // 右下
//        0.0f, 1.0f, // 左上
//        0.0f, 1.0f, // 左上
//        1.0f, 0.0f, // 右上
//        1.0f, 1.0f // 左上
//    )


//    val textureCoords = floatArrayOf(
//        0.0f, 0.0f, // 左下
//        1.0f, 0.0f, // 右下
//        0.0f, 1.0f, // 左上
//        1.0f, 1.0f // 右上
//    )

//    val textureCoords = floatArrayOf(
//        0.0f, 0.0f, // 左下
//        1.0f, 0.0f, // 右下
//        1.0f, 1.0f, // 右上
//        0.0f, 1.0f, // 左上
//        0.0f, 0.0f // 左下
//    )

    // OpenGL坐标和屏幕坐标上下相反。

    val vertexCoordsBuffer by lazy {
        ByteBuffer.allocateDirect(vertexCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(vertexCoords)
                position(0)
            }
    }

    val textureBuffer by lazy {
        ByteBuffer.allocateDirect(textureCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(textureCoords)
                position(0)
            }
    }

    val textures = intArrayOf(1)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {


        program = with(surfaceView.context) {
            // 加载着色器
            // 编译着色器
            val vertexShader = loadShaderSource(this, R.raw.texture_vertex_shader).run {
                loadShader(GLES30.GL_VERTEX_SHADER, this)
            }

            val fragmentShader = loadShaderSource(this, R.raw.texture_fragment_shader).run {
                loadShader(GLES30.GL_FRAGMENT_SHADER, this)
            }
            // 创建并链接着色器程序
            createAndLinkProgrm(this, vertexShader, fragmentShader)
        }

        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 2 * 4, vertexCoordsBuffer)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 2 * 4, textureBuffer)

        GLES30.glEnableVertexAttribArray(0)
        GLES30.glEnableVertexAttribArray(1)


        // 生成纹理ID引用
        // 第一个参数 生成纹理的数量
        // 第二个参数 储存纹理ID
        GLES30.glGenTextures(1, textures, 0)
        // 绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0])
        // 为当前绑定的纹理对象设置环绕、过滤方式
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT)
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_LINEAR
        )
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_LINEAR
        )

        // 将bitmaph转换纹理
        val bitmap = BitmapFactory.decodeResource(surfaceView.context.resources, R.drawable.a)
        // 第一个参数指定了纹理目标(Target)
        // 第二个参数为纹理指定多级渐远纹理的级别，如果你希望单独手动设置每个多级渐远纹理的级别的话。这里我们填0，也就是基本级别 TODO 不懂
        // 下个参数应该总是被设为0（历史遗留的问题）
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
//        GLES30.glTexImage2D()

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // 设置视口
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        // 绘制
        program?.let {
            // 设置清空屏幕所用的颜色 当调用glClear函数，清除颜色缓冲之后，整个颜色缓冲都会被填充为glClearColor里所设置的颜色
            GLES30.glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
            // 我们可以通过调用glClear函数来清空屏幕的颜色缓冲,glClear它接受一个缓冲位(Buffer Bit)来指定要清空的缓冲,这里我们只关心颜色
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

            // 激活程序对象
            // 在glUseProgram函数调用之后，每个着色器调用和渲染调用都会使用这个程序对象（也就是之前写的着色器)了
            GLES30.glUseProgram(it)

            GLES30.glActiveTexture(GLES30.GL_TEXTURE0) // 在绑定纹理之前先激活纹理单元 纹理单元GL_TEXTURE0默认总是被激活
            // 绑定纹理
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0])

            // 顶点法绘制
            // 调用glDrawArrays方法来进行物体的绘制，
            // 此方法是按照传入渲染管线顶点本身的顺序及选用的绘制方式将顶点组织成图元进行绘制的，称为顶点法
            // 注意绘制方式了，第一个参数，不同的绘制方式，需要的坐标不同。
//            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 4)
//            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6)
            // GL_TRIANGLE_STRIP 很神奇的一个模式，这个实际测试结果和网络上好多说法都不一致，这个要特别注意。
            // 因为它绘制的特殊性，所以顶点的顺序决定它可能需要四个顶点，可能需要五个顶点，需要自己处理闭合
            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 4)

            // 索引法绘制
            // 调用glDrawElements 方法在绘制
            // 不但要将顶点序列传入渲染管线，还需要将索引序列传入管线。绘制时管线根据索引值序列中的索引一一从顶点序列中取出对应的顶点，并根据当前选用的绘制方式组织成图元进行绘制

        }

    }

}