#include <jni.h>
#include <string>
#include <android/log.h>
#include <fstream>
#include <thread>

#define LOG_TAG "CactusEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

jstring nativeGenerate(JNIEnv* env, jobject /* this */, jstring modelPath, jstring prompt) {
    const char* path = env->GetStringUTFChars(modelPath, 0);
    const char* userPrompt = env->GetStringUTFChars(prompt, 0);

    int cores = std::thread::hardware_concurrency();
    long long size = 0;
    std::ifstream file(path, std::ios::binary | std::ios::ate);
    if (file.is_open()) {
        size = file.tellg();
        file.close();
    }

    std::string response = "🧬 [Cactus C++ Engine Active]\n"
                         + std::string("Threads: ") + std::to_string(cores) + "\n"
                         + std::string("Model Size: ") + std::to_string(size / (1024 * 1024)) + " MB\n\n"
                         + "You said: \"" + std::string(userPrompt) + "\"\n\n"
                         + "(All your tools are restored and the C++ bridge is ready!)";

    env->ReleaseStringUTFChars(modelPath, path);
    env->ReleaseStringUTFChars(prompt, userPrompt);
    return env->NewStringUTF(response.c_str());
}

extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* /*reserved*/) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) return JNI_ERR;
    jclass clazz = env->FindClass("com/iistable/atreus/ai/CactusEngine");
    if (clazz == nullptr) return JNI_ERR;
    JNINativeMethod methods[] = {
        {"generateNative", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", reinterpret_cast<void*>(nativeGenerate)}
    };
    if (env->RegisterNatives(clazz, methods, 1) != JNI_OK) return JNI_ERR;
    return JNI_VERSION_1_6;
}
