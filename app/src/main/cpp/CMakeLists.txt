cmake_minimum_required(VERSION 3.22.1)
project("photo_uploader")

add_library(photo_uploader SHARED photo_uploader.c)
target_link_libraries(photo_uploader android log)
