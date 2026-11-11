cmake_minimum_required(VERSION 3.22.1)
project("atreusnative")

add_library(atreusnative SHARED native-lib.cpp)
find_library(log-lib log)
target_link_libraries(atreusnative ${log-lib})
