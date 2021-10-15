//
// Created by Frezrik on 2021/9/18.
//

#ifndef JNIDEMO_MEMLOAD_H
#define JNIDEMO_MEMLOAD_H

#include <vector>

void* load(int sdk_int, void *artHandle, const char *base, size_t size);
std::unique_ptr<const void *> load23(void *artHandle, const char *base, size_t size);

#endif //JNIDEMO_MEMLOAD_H
