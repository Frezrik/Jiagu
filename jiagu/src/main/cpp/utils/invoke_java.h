//
// Created by zhouming on 2021/9/13.
//

#ifndef JIAGU_INVOKE_JAVA_H
#define JIAGU_INVOKE_JAVA_H

#include <jni.h>
void SetEnv(JNIEnv *env);
jobject NewClassInstance(char *className, char *paramCode, ...);
jvalue CallObjectMethod(jobject cls, char *methodName, char *paramCode, ...);
jvalue CallStaticMethod(char *className, char *methodName, char *paramCode, ...);
jvalue GetStaticField(char *className, char *fieldName, char *paramCode);
jvalue GetField(jobject object, char *fieldName, char *paramCode);

#endif //JIAGU_INVOKE_JAVA_H
