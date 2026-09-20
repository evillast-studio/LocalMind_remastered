cmake_minimum_required(VERSION 3.22.1)

project("localmind")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Solo arm64-v8a (lo unico que se empaqueta). Si por algun motivo el ABI llega
# distinto, se avisa en vez de compilar algo que no va a usarse.
if(ANDROID AND NOT ANDROID_ABI STREQUAL "arm64-v8a")
    message(WARNING "LocalMind solo esta configurado para arm64-v8a; ABI actual: ${ANDROID_ABI}")
endif()

# Optimizacion para velocidad (el Os de gradle se ignora aqui: -O3 va al final).
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O3 -DNDEBUG -ffunction-sections -fdata-sections")
set(CMAKE_C_FLAGS_RELEASE   "${CMAKE_C_FLAGS_RELEASE} -O3 -DNDEBUG -ffunction-sections -fdata-sections")

# ---- llama.cpp ----
set(LLAMA_BUILD_TESTS    OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER   OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_COMMON   OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TOOLS    OFF CACHE BOOL "" FORCE)
set(LLAMA_CURL           OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS    OFF CACHE BOOL "" FORCE)

# Backend de CPU estatico y UNA sola variante: compila mucho mas rapido y no hace
# falta cargar librerias .so extra en ejecucion. GGML_NATIVE OFF es obligatorio al
# cruzar-compilar (no se puede detectar la CPU del telefono desde el runner).
set(GGML_BACKEND_DL        OFF CACHE BOOL "" FORCE)
set(GGML_CPU_ALL_VARIANTS  OFF CACHE BOOL "" FORCE)
set(GGML_NATIVE            OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP            OFF CACHE BOOL "" FORCE)

# Sin GPU: la Mali-G51 del Kirin 710 no acelera estos modelos y Vulkan agrega
# tiempo de compilacion y dependencias. (El nombre viejo LLAMA_VULKAN ya no existe.)
set(GGML_VULKAN  OFF CACHE BOOL "" FORCE)
set(GGML_OPENCL  OFF CACHE BOOL "" FORCE)
set(GGML_METAL   OFF CACHE BOOL "" FORCE)
set(GGML_CUDA    OFF CACHE BOOL "" FORCE)

# Kirin 710 = Cortex-A73/A53 (ARMv8.0-A): sin dotprod ni i8mm. Se fija el conjunto
# de instrucciones seguro para ese SoC; asi no se generan instrucciones ilegales.
if(ANDROID_ABI STREQUAL "arm64-v8a")
    set(GGML_CPU_ARM_ARCH "armv8-a" CACHE STRING "" FORCE)
endif()

add_subdirectory(llama.cpp)

add_library(${CMAKE_PROJECT_NAME} SHARED
    jni_bridge.cpp
)

target_link_libraries(${CMAKE_PROJECT_NAME}
    android
    log
    llama
)

target_include_directories(${CMAKE_PROJECT_NAME} PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/src
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/include
    ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp/ggml/include
)

if(CMAKE_BUILD_TYPE STREQUAL "Release")
    add_custom_command(TARGET ${CMAKE_PROJECT_NAME} POST_BUILD
        COMMAND ${CMAKE_STRIP} --strip-unneeded $<TARGET_FILE:${CMAKE_PROJECT_NAME}>
    )
endif()
