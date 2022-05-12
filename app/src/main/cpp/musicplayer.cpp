#include <jni.h>
#include "android/bitmap.h"

extern "C"
JNIEXPORT jint JNICALL
Java_com_drkryz_scutfy_Services_MusicService_convertString(JNIEnv *env, jclass clazz,
                                                           jstring text) {
    jclass Iinteger = env->FindClass("java/lang/Integer");
    jmethodID parser = env->GetStaticMethodID(Iinteger, "parseInt", "(Ljava/lang/String;)I");
    return env->CallStaticIntMethod(Iinteger, parser, text);
}


