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

class MatrixRender(val surfaceView: MatrixSurfaceView) : GLSurfaceView.Renderer {

    var program: Int? = null

    val vertexCoords = floatArrayOf(
        -1.0f, -1.0f, // 左下
        1.0f, -1.0f, // 右下
        1.0f, 1.0f, // 右上
        -1.0f, 1.0f // 左上
    )

    // 修正纹理显示倒 旋转180度 上下倒
    val textureCoords = floatArrayOf(
        0.0f, 1.0f, // 左上
        1.0f, 1.0f, // 右上
        1.0f, 0.0f, // 右下
        0.0f, 0.0f // 左下
    )

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
    private var vPMatrixHandle: Int = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {


        program = with(surfaceView.context) {
            // 加载着色器
            // 编译着色器
            val vertexShader = loadShaderSource(this, R.raw.matrix_vertex_shader).run {
                loadShader(GLES30.GL_VERTEX_SHADER, this)
            }

            val fragmentShader = loadShaderSource(this, R.raw.matrix_fragment_shader).run {
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
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)

    }

    private val vPMatrix = FloatArray(16)

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // 设置视口
        GLES30.glViewport(0, 0, width, height)

        // 计算屏幕比列
        val ratio: Float = width.toFloat() / height.toFloat()

        // 直接利用 Matrix 来实现

        // 填充投影矩阵
        // 定义了 平截头体 - 由投影矩阵创建的观察箱(Viewing Box)被称为平截头体(Frustum)
        //
        // 7 8 参数 近平面 远平面
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    override fun onDrawFrame(gl: GL10?) {
        // 绘制
        program?.let {
            // 设置清空屏幕所用的颜色 当调用glClear函数，清除颜色缓冲之后，整个颜色缓冲都会被填充为glClearColor里所设置的颜色
            GLES30.glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
            // 我们可以通过调用glClear函数来清空屏幕的颜色缓冲,glClear它接受一个缓冲位(Buffer Bit)来指定要清空的缓冲,这里我们只关心颜色
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

            // 设置相机位置 填充相机视图变换矩阵
            // 3-5 参数 摄像机位置，眼睛观察的位置
            // 6-8 目标位置，一般是 0，0，0.物体中心点
            // 9-11 上向量
            Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 7f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
            // 将投影矩阵和相机视图矩阵进行计算
            // 将俩个 4 X 4 的矩阵进行相乘，结果保存在第三个矩阵中（第一个参数）。
            // 注意矩阵乘法不遵守交换律
            Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

            // 激活程序对象
            // 在glUseProgram函数调用之后，每个着色器调用和渲染调用都会使用这个程序对象（也就是之前写的着色器)了
            GLES30.glUseProgram(it)

            GLES30.glActiveTexture(GLES30.GL_TEXTURE0) // 在绑定纹理之前先激活纹理单元 纹理单元GL_TEXTURE0默认总是被激活
            // 绑定纹理
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0])

            // get handle to shape's transformation matrix
            vPMatrixHandle = GLES30.glGetUniformLocation(it, "uPMatrix")

            // Pass the projection and view transformation to the shader
            GLES30.glUniformMatrix4fv(vPMatrixHandle, 1, false, vPMatrix, 0)

            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN, 0, 4)

        }

    }

}