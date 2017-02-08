LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

APP_ABI := all

LOCAL_MODULE    := libmonitor
LOCAL_SRC_FILES := monitor_e.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog
# so log 控制开关
#LOCAL_CFLAGS += -DALI_LOG
#error: format not a string literal and no format arguments [-Werror=format-security]
LOCAL_DISABLE_FORMAT_STRING_CHECKS := true
include $(BUILD_SHARED_LIBRARY)