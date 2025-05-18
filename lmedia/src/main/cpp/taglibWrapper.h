#ifndef TAGLIB_WRAPPER_H
#define TAGLIB_WRAPPER_H

#include <jni.h>
#include <string>
#include <android/log.h>
#include <fileref.h>
#include <tpropertymap.h>
#include <tfilestream.h>
#include <sys/stat.h>

#define LOG_TAG "LMEDIA_TAGLIB"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static const std::string EMPTY_STR;

#endif //TAGLIB_WRAPPER_H