LOCAL_PATH := ${call my-dir}

include $(CLEAR_VARS)

LOCAL_MODULE := FractalMath
LOCAL_SRC_FILES := FractalMath.c

include $(BUILD_SHARED_LIBRARY)

