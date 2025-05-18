cmake_minimum_required(VERSION 3.0.0 FATAL_ERROR)
cmake_policy(SET CMP0057 NEW)

project(lmedia)

add_subdirectory(taglib)

include_directories("${CMAKE_CURRENT_SOURCE_DIR}")

# 扫描taglib目录下的所有文件，并将它们的路径存储在FILES变量中
file(GLOB_RECURSE FILES "${CMAKE_CURRENT_SOURCE_DIR}/taglib/*")

# 遍历FILES中的所有文件，提取出它们的目录，并将这些目录使用include_directories导入搜索路径中
foreach (FILE ${FILES})
    get_filename_component(DIR ${FILE} PATH)

    if (IS_DIRECTORY ${DIR})
        include_directories(${DIR})
    endif ()
endforeach ()

add_library(
        taglib SHARED
        taglibWrapper.cpp
)

find_library(log-lib log)
target_link_libraries(taglib tag ${log-lib})