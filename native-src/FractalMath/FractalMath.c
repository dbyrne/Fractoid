#include "byrne_fractal_NativeLib.h"

JNIEXPORT jstring JNICALL Java_byrne_fractal_NativeLib_getHelloWorld
(JNIEnv * env, jobject obj) {
  return (*env)->NewStringUTF(env, "Hello NDK 2!");
}
