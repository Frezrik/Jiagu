//
// Created by Frezrik on 2021/9/13.
//

#include "invoke_java.h"

#include "plog.h"

JNIEnv *envPtr;

void SetEnv(JNIEnv *env) {
    envPtr = env;
}

/**
 * 构造新 Java 对象
 * @param className 类名
 * @param paramCode 函数签名
 * @param ... 传给构造函数的参数
 * @return
 */
jobject NewClassInstance(char *className, char *paramCode, ...) {
    jobject value;
    jclass cls = envPtr->FindClass(className);
    jmethodID methodId = envPtr->GetMethodID(cls, "<init>", paramCode);
    if (envPtr->ExceptionCheck()) {
        LOGE("[-]NewClassInstance <init> failed");
        return value;
    }

    va_list args;
    va_start(args, paramCode);
    value = envPtr->NewObjectV(cls, methodId, args);
    va_end(args);

    return value;
}

/**
 * 从本地方法调用Java实例方法
 * @param object Java对象
 * @param methodName 方法名
 * @param paramCode 函数签名
 * @param ... 函数参数
 * @return
 */
jvalue CallObjectMethod(jobject object, char *methodName, char *paramCode, ...) {
    jvalue value;

    jclass cls = envPtr->GetObjectClass(object);
    jmethodID methodId = envPtr->GetMethodID(cls, methodName, paramCode);
    if (envPtr->ExceptionCheck()) {
        LOGE("[-]CallObjectMethod methodID failed");
        return value;
    }

    char *p = paramCode;
    // skip '()' to find out the return type
    while (*p != ')') {
        p++;
    }
    // skip ')'
    p++;

    va_list args;
    va_start(args, paramCode);
    switch (*p) {
        case 'V':
            envPtr->CallVoidMethodV(object, methodId, args);
            break;

        case '[':
        case 'L':
            value.l = envPtr->CallObjectMethodV(object, methodId, args);
            break;

        case 'Z':
            value.z = envPtr->CallBooleanMethodV(object, methodId, args);
            break;

        case 'B':
            value.b = envPtr->CallByteMethodV(object, methodId, args);
            break;

        case 'C':
            value.c = envPtr->CallCharMethodV(object, methodId, args);
            break;

        case 'S':
            value.s = envPtr->CallShortMethodV(object, methodId, args);
            break;

        case 'I':
            value.i = envPtr->CallIntMethodV(object, methodId, args);
            break;

        case 'J':
            value.j = envPtr->CallLongMethodV(object, methodId, args);
            break;

        case 'F':
            value.f = envPtr->CallFloatMethodV(object, methodId, args);
            break;

        case 'D':
            value.d = envPtr->CallDoubleMethodV(object, methodId, args);
            break;

        default:
            LOGE("CallObjectMethod paramCode = %s, illegal", paramCode);
    }
    va_end(args);

    return value;
}

/**
 * 调用Java对象的静态方法
 * @param className 类名
 * @param methodName 方法名
 * @param paramCode 函数签名
 * @param ... 函数参数
 * @return
 */
jvalue CallStaticMethod(char *className, char *methodName, char *paramCode, ...) {
    jvalue value;
    jclass cls = envPtr->FindClass(className);
    jmethodID methodId = envPtr->GetStaticMethodID(cls, methodName, paramCode);
    if (envPtr->ExceptionCheck()) {
        LOGE("[-]CallStaticMethod methodID failed");
        return value;
    }

    char *p = paramCode;
    // skip '()' to find out the return type
    while (*p != ')') {
        p++;
    }
    // skip ')'
    p++;

    va_list args;
    va_start(args, paramCode);
    switch (*p) {
        case 'V':
            envPtr->CallStaticVoidMethodV(cls, methodId, args);
            break;

        case '[':
        case 'L':
            value.l = envPtr->CallStaticObjectMethodV(cls, methodId, args);
            break;

        case 'Z':
            value.z = envPtr->CallStaticBooleanMethodV(cls, methodId, args);
            break;

        case 'B':
            value.b = envPtr->CallStaticByteMethodV(cls, methodId, args);
            break;

        case 'C':
            value.c = envPtr->CallStaticCharMethodV(cls, methodId, args);
            break;

        case 'S':
            value.s = envPtr->CallStaticShortMethodV(cls, methodId, args);
            break;

        case 'I':
            value.i = envPtr->CallStaticIntMethodV(cls, methodId, args);
            break;

        case 'J':
            value.j = envPtr->CallStaticLongMethodV(cls, methodId, args);
            break;

        case 'F':
            value.f = envPtr->CallStaticFloatMethodV(cls, methodId, args);
            break;

        case 'D':
            value.d = envPtr->CallStaticDoubleMethodV(cls, methodId, args);
            break;

        default:
            LOGE("CallStaticMethod paramCode = %s, illegal", paramCode);
    }
    va_end(args);

    return value;
}

/**
 * 获取Java对象的静态域的值
 * @param className 类名
 * @param fieldName 静态域名
 * @param paramCode 域签名
 * @return 静态域的值
 */
jvalue GetStaticField(char *className, char *fieldName, char *paramCode) {
    jvalue value;
    jclass cls = envPtr->FindClass(className);
    jfieldID fieldId = envPtr->GetStaticFieldID(cls, fieldName, paramCode);
    if (envPtr->ExceptionCheck()) {
        LOGE("[-]GetStaticField fieldID failed");
        return value;
    }

    char *p = paramCode;
    switch (*p) {
        case '[':
        case 'L':
            value.l = envPtr->GetStaticObjectField(cls, fieldId);
            break;

        case 'Z':
            value.z = envPtr->GetStaticBooleanField(cls, fieldId);
            break;

        case 'B':
            value.b = envPtr->GetStaticByteField(cls, fieldId);
            break;

        case 'C':
            value.c = envPtr->GetStaticCharField(cls, fieldId);
            break;

        case 'S':
            value.s = envPtr->GetStaticShortField(cls, fieldId);
            break;

        case 'I':
            value.i = envPtr->GetStaticIntField(cls, fieldId);
            break;

        case 'J':
            value.j = envPtr->GetStaticLongField(cls, fieldId);
            break;

        case 'F':
            value.f = envPtr->GetStaticFloatField(cls, fieldId);
            break;

        case 'D':
            value.d = envPtr->GetStaticDoubleField(cls, fieldId);
            break;

        default:
            LOGE("GetStaticField paramCode = %s, illegal", paramCode);
    }

    return value;
}

/**
 * 获取对象的实例域的值
 * @param object Java对象
 * @param fieldName 实例域名
 * @param paramCode 域签名
 * @return 实例域的值
 */
jvalue GetField(jobject object, char *fieldName, char *paramCode) {
    jvalue value;
    jclass cls = envPtr->GetObjectClass(object);
    jfieldID fieldId = envPtr->GetFieldID(cls, fieldName, paramCode);
    if (envPtr->ExceptionCheck()) {
        LOGE("[-]GetField fieldID failed");
        return value;
    }

    char *p = paramCode;
    switch (*p) {
        case '[':
        case 'L':
            value.l = envPtr->GetObjectField(object, fieldId);
            break;

        case 'Z':
            value.z = envPtr->GetBooleanField(object, fieldId);
            break;

        case 'B':
            value.b = envPtr->GetByteField(object, fieldId);
            break;

        case 'C':
            value.c = envPtr->GetCharField(object, fieldId);
            break;

        case 'S':
            value.s = envPtr->GetShortField(object, fieldId);
            break;

        case 'I':
            value.i = envPtr->GetIntField(object, fieldId);
            break;

        case 'J':
            value.j = envPtr->GetLongField(object, fieldId);
            break;

        case 'F':
            value.f = envPtr->GetFloatField(object, fieldId);
            break;

        case 'D':
            value.d = envPtr->GetDoubleField(object, fieldId);
            break;

        default:
            LOGE("GetField paramCode = %s, illegal", paramCode);
    }

    return value;
}
