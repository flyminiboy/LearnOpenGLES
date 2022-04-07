
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

# 指定版本
cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("native-lib")

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library( # 库名称
        native-lib

        # 动态库
        SHARED

        # 源文件相对路径
        native-lib.cpp)

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

target_link_libraries( # Specifies the target library.
        native-lib

        EGL
        GLESv3
        android

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})