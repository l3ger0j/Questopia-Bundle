project(ndkqsp)

include_directories(../onig)

if ("${CMAKE_C_COMPILER_ID}" STREQUAL "GNU")
    set(PROJECT_COMPILER_FLAGS "-Wall")
    set(PROJECT_LINKER_FLAGS "")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Wno-char-subscripts -Wno-unused-variable")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra")
    set(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS_DEBUG} -g -D_DEBUG -DDEBUG")
    set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -g -D_DEBUG -DDEBUG")
    set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -Ofast -DNDEBUG")
    set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -Ofast -DNDEBUG")
elseif ("${CMAKE_C_COMPILER_ID}" STREQUAL "MSVC")
    set(PROJECT_COMPILER_FLAGS "/W4")
    set(PROJECT_LINKER_FLAGS "")
    set(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS_DEBUG} /D_DEBUG /DDEBUG")
    set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} /D_DEBUG /DDEBUG")
    set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} /O2 /DNDEBUG")
    set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} /O2 /DNDEBUG")
    set(CMAKE_MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
elseif ("${CMAKE_C_COMPILER_ID}" STREQUAL "Clang" OR "${CMAKE_C_COMPILER_ID}" STREQUAL "AppleClang")
    set(PROJECT_COMPILER_FLAGS "")
    set(PROJECT_LINKER_FLAGS "")
    set(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS_DEBUG} -g -D_DEBUG -DDEBUG")
    set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -g -D_DEBUG -DDEBUG")
    set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -Ofast -DNDEBUG")
    set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -Ofast -DNDEBUG")
else()
    message(FATAL_ERROR "Compiler is not supported")
endif()

set(CMAKE_POSITION_INDEPENDENT_CODE ON)
set(CMAKE_VISIBILITY_INLINES_HIDDEN ON)

set(QSP_SOURCES
        src/actions.c
        src/callbacks.c
        src/codetools.c
        src/coding.c
        src/common.c
        src/errors.c
        src/game.c
        src/locations.c
        src/mathops.c
        src/memwatch.c
        src/menu.c
        src/objects.c
        src/playlist.c
        src/statements.c
        src/text.c
        src/time.c
        src/towlower.c
        src/towupper.c
        src/variables.c
        src/variant.c
        src/bindings/android/android_callbacks.c
        src/bindings/android/android_coding.c
        src/bindings/android/android_control.c
)
add_library(ndkqsp SHARED ${QSP_SOURCES})
find_library(log-lib log)
target_compile_options(ndkqsp PRIVATE ${PROJECT_COMPILER_FLAGS})
target_link_options(ndkqsp PRIVATE ${PROJECT_LINKER_FLAGS})
target_link_libraries(ndkqsp onig ${log-lib})
