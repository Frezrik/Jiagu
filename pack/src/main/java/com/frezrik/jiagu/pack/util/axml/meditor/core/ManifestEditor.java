package com.frezrik.jiagu.pack.util.axml.meditor.core;


import com.frezrik.jiagu.pack.util.axml.AxmlReader;
import com.frezrik.jiagu.pack.util.axml.AxmlVisitor;
import com.frezrik.jiagu.pack.util.axml.AxmlWriter;
import com.frezrik.jiagu.pack.util.axml.NodeVisitor;
import com.frezrik.jiagu.pack.util.axml.meditor.property.ModificationProperty;
import com.frezrik.jiagu.pack.util.axml.meditor.utils.Log;
import com.frezrik.jiagu.pack.util.axml.meditor.utils.Utils;
import com.frezrik.jiagu.pack.util.axml.meditor.visitor.ManifestTagVisitor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ManifestEditor {

    private InputStream inputStream;
    private OutputStream outputStream;

    // can not be null
    private ModificationProperty properties;

    private boolean needClosedStream = false;

    public ManifestEditor(String srcManifestFilePath, String dstManifestFilePath,
                          ModificationProperty properties) {
        this.properties = properties;
        try {
            inputStream = new FileInputStream(srcManifestFilePath);
            outputStream = new FileOutputStream(dstManifestFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        needClosedStream = true;
    }

    public ManifestEditor(InputStream inputStream, OutputStream outputStream,
                          ModificationProperty properties) {
        this.properties = properties;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public String processManifest() {
        if (inputStream == null || outputStream == null || properties == null) {
            Log.i(" processManifest failed , inputStream = " + inputStream
                    + " outputStream=" + outputStream + " properties = " + properties);
            return "";
        }
//        byte[] bytes = Utils.getBytesFromFile(srcManifestFilePath);
        byte[] bytes = Utils.getBytesFromInputStream(inputStream);

        AxmlReader reader = new AxmlReader(bytes);
        AxmlWriter writer = new AxmlWriter();

        try {
            reader.accept(new AxmlVisitor(writer) {
                @Override
                public NodeVisitor child(String ns, String name) {
                    NodeVisitor child = super.child(ns, name);
                    return new ManifestTagVisitor(child, properties);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            byte[] modified = writer.toByteArray();
            outputStream.write(modified);
//            Utils.writeBytesToFile(modified, dstManifestFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (needClosedStream) {
                Utils.close(inputStream);
                Utils.close(outputStream);
            }
        }
        return reader.getApplicationName();
    }
}
