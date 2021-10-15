package com.frezrik.jiagu.pack.util;

import com.frezrik.jiagu.pack.util.axml.meditor.core.FileProcesser;
import com.frezrik.jiagu.pack.util.axml.meditor.property.AttributeItem;
import com.frezrik.jiagu.pack.util.axml.meditor.property.ModificationProperty;
import com.frezrik.jiagu.pack.util.axml.meditor.utils.NodeValue;

import java.io.File;

import static com.frezrik.jiagu.pack.core.AppManager.PROXY_APPLICATION_NAME;

public class AXmlUtil {
    public static String updateManifest(String manifest) {
        ModificationProperty property = new ModificationProperty();

        property.addApplicationAttribute(new AttributeItem(NodeValue.Application.NAME, PROXY_APPLICATION_NAME));

        File file = new File(manifest);
        File temp = new File(manifest + "_bak");
        file.renameTo(temp);

        // 处理manifest文件方法
        String application = FileProcesser.processManifestFile(temp.getPath(), manifest, property);
        temp.delete();

        return application;
    }

}
