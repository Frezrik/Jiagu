//
// Created by zhouming on 2021/9/13.
//

#ifndef PLOG_H
#define PLOG_H

#include <android/log.h>

#define LOG_TAG "NDK_JIAGU"
#define LOGD(...) if(1) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#endif //PLOG_H
