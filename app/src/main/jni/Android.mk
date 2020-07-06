LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := serial_port
LOCAL_LDLIBS := \
	-llog \

LOCAL_SRC_FILES := \
	H:\yyyProject\SerialPortTest\app\src\main\jni\Android.mk \
	H:\yyyProject\SerialPortTest\app\src\main\jni\SerialPort.c \
	H:\yyyProject\SerialPortTest\app\src\main\jni\util.c \


LOCAL_C_INCLUDES += H:\yyyProject\SerialPortTest\app\src\main\jni
LOCAL_C_INCLUDES += H:\yyyProject\SerialPortTest\app\src\release\jni

include $(BUILD_SHARED_LIBRARY)
