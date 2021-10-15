# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

project("jiagu")

add_library(
        jiagu
        SHARED
        jiagu.cpp
        memload.cpp
        utils/aes.c
        utils/dlopen.c
        utils/invoke_java.cpp)

include_directories(
        utils
)

target_link_libraries(
        jiagu
        log)