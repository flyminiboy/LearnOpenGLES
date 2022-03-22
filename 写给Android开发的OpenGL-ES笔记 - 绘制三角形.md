## 写给Android开发的OpenGL-ES笔记 - 绘制三角形


这篇文章的最终目的是要在屏幕上绘制一个三角形出来。

### 理论基础

基于我的一贯风格。我们还是先说几个概念，先把概念默默的在心里面有个印象，不要后面在看见的时候，无数个草泥马狂奔。

	顶点数组对象：Vertex Array Object，VAO
	顶点缓冲对象：Vertex Buffer Object，VBO
	索引缓冲对象：Element Buffer Object，EBO或Index Buffer Object，IBO
	
盗图一张，下面这幅图说明了**图形渲染管线**的每个阶段的抽象展示。**要注意蓝色部分代表的是我们可以注入自定义的着色器的部分**
	
![](https://mmbiz.qpic.cn/mmbiz_png/ibExRe3rl9wdoIw3vWX7VGAbxlBibEdgiaCcn5EiconkxMfsyd9gjj06FCiamfmibH3uaa2nicprE1kDUErTr4Yp6RnvQ/0?wx_fmt=png)

在正式写代码之前，我们先来描述一下我们的工作流程。

首先，我们以**数组**的形式传递3个3D/2D坐标作为图形渲染管线的**输入**，用来表示一个三角形，这个数组叫做**顶点数据(Vertex Data)**

然后，**顶点着色器(Vertex Shader)**，它把一个单独的顶点作为输入，内部主要是做些坐标的转换。

再经过 **图元装配(Primitive Assembly)** ，**几何着色器(Geometry Shader)** ，**光栅化阶段(Rasterization Stage)** 最终生成供**片段着色器(Fragment Shader)**使用的**片段(Fragment)（OpenGL中的一个片段是OpenGL渲染一个像素所需的所有数据。）**

上面三步操作我们无法手动控制，所以不再做更多的介绍。

然后**片段着色器** 计算一个像素的最终颜色。

最后最终的对象将会被传到最后一个阶段，我们叫做**Alpha测试和混合(Blending)**阶段，这一步我们也无法手动控制，所以不再做更多介绍。

盗图一张

![](https://mmbiz.qpic.cn/mmbiz_png/ibExRe3rl9wdoIw3vWX7VGAbxlBibEdgiaCjxjmsG9QEUL6H7FNQj17eX2MP7ftHgXXXwdPLrwREwC4jLKR3Y4gvw/0?wx_fmt=png)

左边是OpenGL坐标，右边是Android屏幕的映射。

一旦我们的**顶点坐标**已经在**顶点着色器**中处理过，它们就应该是**标准化设备坐标**了，标准化设备坐标是一个**x、y和z值在-1.0到1.0**的一小段空间。（从现在开始我们依然会说3D坐标，但是我们实际只做了一个2D的图形，所以我们默认忽略z轴）

### 编写代码

好了下面我们正式开始我们这一系列文章的代码之旅了。

其实**OpenGL ES**的大部分代码都是一些模板代码是固定的格式。

所以我们有时候要先有**don't ask why，just do it**的觉悟。

我们现在这个版本是基于 `GLSurfaceView` 和`GLSurfaceView.Renderer`创建视图容器。

`GLSurfaceView` 提供了用于管理 **EGL 上下文**、线程间通信以及与 Activity 生命周期的交互的辅助程序类，所以就目前而言，他可以帮助我们简化我们的开发流程，降低开发难度。

#### 声明 OpenGL 要求

对于 OpenGL ES 3.0：

```
<!-- Tell the system this app requires OpenGL ES 3.0. -->
<uses-feature android:glEsVersion="0x00030000" android:required="true" />
    
```

首先我们要定义好**顶点着色器**和**片段着色器**。有俩种方式，

1. 直接将顶点着色器的源代码硬编码在代码文件顶部的字符串中。
2. main/res/raw 下面新建俩个.glsl 文件。

这里我们采用第二种方式

**triangle\_vertex\_shader.glsl** 顶点着色器

```
#version 300 es
layout (location = 0) in vec4 vPosition;

void main() {
    gl_Position = vPosition; // gl_Position 内建变量，是一个vec4类型
}
```

**triangle\_fragment\_shader.glsl** 片段着色器

```
#version 300 es
precision mediump float;

out vec4 outColor;

void main() {
    outColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);
}
```

为了能够让**OpenGL**使用着色器，我们必须在运行时动态编译它的源代码

```
fun loadShader(shaderType: Int, shaderSource: String) =

    // 根据着色器类型创建着色器对象
    GLES30.glCreateShader(shaderType).also { shader ->
        // 将着色器源码附加到着色器上
        GLES30.glShaderSource(shader, shaderSource)
        GLES30.glCompileShader(shader)
        // 检测是否编译成功
        val compiled = intArrayOf(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == GLES30.GL_FALSE) {
            val error = GLES30.glGetShaderInfoLog(shader)
            logE("glCompileShader info [ $error ]")
            return GLES30.GL_FALSE
        }
    }
```

### 着色器程序

**着色器程序对象(Shader Program Object)**是多个着色器合并之后并最终链接完成的版本

要使用刚才编译的着色器我们必须把它们链接(Link)为一个着色器程序对象，然后在渲染对象的时候激活这个着色器程序。

```
fun createAndLinkProgrm(context: Context, vertexShader: Int, fragmentShader: Int) =
    // 构建着色器程序对象
    GLES30.glCreateProgram().also { program ->

        // 将着色器附加到着色器程序
        GLES30.glAttachShader(program, vertexShader)
        GLES30.glAttachShader(program, fragmentShader)
        // 链接程序
        GLES30.glLinkProgram(program)
        // 检测链接状态
        val linked = intArrayOf(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linked, 0)
        if (linked[0] == GLES30.GL_FALSE) {
            val error = GLES30.glGetProgramInfoLog(program)
            logE("glCompileShader info [ $error ]")
            return GLES30.GL_FALSE
        }

        // 在把着色器对象链接到程序对象以后，记得删除着色器对象，我们不再需要它们了
        GLES30.glDeleteShader(vertexShader)
        GLES30.glDeleteShader(fragmentShader)
    }
```

### 顶点输入

```
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
```

注意这里关于顶点的定义，我们直接采用了2D的标准，只包括`x和y`，并么有声明`z`，这涉及到我们在 **链接顶点属性** 时候的一些参数设置。

### 链接顶点属性

继续盗图一张。

![](https://mmbiz.qpic.cn/mmbiz_png/ibExRe3rl9wdoIw3vWX7VGAbxlBibEdgiaCHXXy5r3vXniczqeC9TGfjG6I2cJ6Wo9zbORA6dVF19MVpgbtpicIiaPBQ/0?wx_fmt=png)

我们必须手动指定输入数据的哪一个部分对应顶点着色器的哪一个顶点属性，所以，我们必须在渲染前指定OpenGL该如何解释顶点数据

我们的顶点缓冲数据最终会被解析为上图这样子。（注意图里面是以3D坐标说明的，即每个顶点包括`x,y,z`）

```
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
```

### 激活的着色器，绘制图元

好了现在开始我们最激动人心的一刻了

```
// 激活程序对象
// 在glUseProgram函数调用之后，每个着色器调用和渲染调用都会使用这个程序对象（也就是之前写的着色器)了
GLES30.glUseProgram(it)
// 使用当前激活的着色器，之前定义的顶点属性配置，和VBO的顶点数据（通过VAO间接绑定）来绘制图元。
// 第一个参数是我们打算绘制的OpenGL图元的类型
// 指定了顶点数组的起始索引
// 指定我们打算绘制多少个顶点
GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
```