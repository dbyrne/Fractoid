LOCAL_PATH := ${call my-dir}

include $(CLEAR_VARS)

LOCAL_MODULE := FractalMath
LOCAL_SRC_FILES := FractalMath.c
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog

include $(BUILD_SHARED_LIBRARY)

