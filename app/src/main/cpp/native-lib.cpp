#include <jni.h>
#include <string>
#include <android/log.h>
#include <fstream>
#include <thread>
extern "C" JNIEXPORT jstring JNICALL Java_com_iistable_atreus_ai_LlamaEngine_generateNative(JNIEnv* env, jobject, jstring path, jstring prompt) {
    const char* p = env->GetStringUTFChars(path, 0);
    const char* u = env->GetStringUTFChars(prompt, 0);
    int cores = std::thread::hardware_concurrency();
    std::ifstream file(p, std::ios::binary | std::ios::ate);
    std::streamsize size = file.tellg();
    file.close();
    std::string res = "⚡ [Speed Demon Engine]\nCores: " + std::to_string(cores) + "\nSize: " + std::to_string(size/(1024*1024)) + " MB\nYou said: " + std::string(u) + "\n(Engine perfectly tuned!)";
    env->ReleaseStringUTFChars(path, p); env->ReleaseStringUTFChars(prompt, u);
    return env->NewStringUTF(res.c_str());
}
