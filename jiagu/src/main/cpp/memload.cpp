//
// Created by Frezrik on 2021/9/18.
//

#include "memload.h"

#include <android/log.h>
#include <string.h>
#include <cstdlib>
#include <string>
#include <ostream>
#include <map>

#include "dex_header.h"
#include "utils/dlopen.h"
#include "utils/plog.h"

typedef void *(*org_artDexFileOpenMemory21)(const uint8_t *base, size_t size,
                                            const std::string &location, uint32_t location_checksum,
                                            void *mem_map, std::string *error_msg);

typedef void *(*org_artDexFileOpenMemory22)(const uint8_t *base, size_t size,
                                            const std::string &location, uint32_t location_checksum,
                                            void *mem_map, const void *oat_file,
                                            std::string *error_msg);

typedef std::unique_ptr<const void *> (*org_artDexFileOpenMemory23)(const uint8_t *base,
                                                                    size_t size,
                                                                    const std::string &location,
                                                                    uint32_t location_checksum,
                                                                    void *mem_map,
                                                                    const void *oat_dex_file,
                                                                    std::string *error_msg);

#define OpenMemory21 "_ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPS9_"
#define OpenMemory22 "_ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_7OatFileEPS9_"
#define OpenMemory23 "_ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_10OatDexFileEPS9_"

/**
 * 加载DEX
 * @param env
 * @param oApplication
 * @param stSrcDEXFp
 */
void *load(int sdk_int, void *artHandle, const char *base, size_t size) {
    std::string location = "Anonymous-DexFile";
    std::string err_msg;
    void *value;

    const auto *dex_header = reinterpret_cast<const Header *>(base);

    if (sdk_int == 21) {
        auto func21 = (org_artDexFileOpenMemory21) ndk_dlsym(artHandle, OpenMemory21);
        value = func21((const unsigned char *) base,
                       size,
                       location,
                       dex_header->checksum_,
                       nullptr,
                       &err_msg);
    } else if (sdk_int == 22) {
        auto func22 = (org_artDexFileOpenMemory22) ndk_dlsym(artHandle, OpenMemory22);
        value = func22((const unsigned char *) base,
                       size,
                       location,
                       dex_header->checksum_,
                       nullptr,
                       nullptr,
                       &err_msg);
    }

    if (!value) {
        LOGE("[-]call load failed");
        return nullptr;
    }

    return value;
}

/**
 * 加载DEX 用于android6.0 7.0 7.1
 * @param env
 * @param oApplication
 * @param stSrcDEXFp
 */
std::unique_ptr<const void *> load23(void *artHandle, const char *base, size_t size) {
    std::string location = "Anonymous-DexFile";
    std::string err_msg;
    std::unique_ptr<const void *> value;

    const auto *dex_header = reinterpret_cast<const Header *>(base);

    auto func23 = (org_artDexFileOpenMemory23) ndk_dlsym(artHandle, OpenMemory23);
    value = func23((const unsigned char *) base,
                     size,
                     location,
                     dex_header->checksum_,
                     nullptr,
                     nullptr,
                     &err_msg);

    if (!value) {
        LOGE("[-]call load23 failed");
        return nullptr;
    }

    return value;
}
