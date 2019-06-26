/*
 * Copyright (C) 2019 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.cache;

import gov.nasa.worldwind.util.Logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

/**
 * pcm - utility class that can be used to obtain raw cache object data from a FileStore.
 * Note that we still might have to parse or post-process the raw data before it can be stored in a SessionCache, which
 * is the responsibility of the client.
 *
 * Instances of this class are thread-safe
 *
 * Used to avoid network access
 */
public class FileStoreSource {

    private Object lock = new Object();

    protected FileStore fileStore;
    protected String fName;

    public FileStoreSource(FileStore fileStore, String fname) {
        this.fileStore = fileStore;
        this.fName = fname;
    }

    public boolean exists() { return fileStore.containsFile(fName); }

    public String getFileName() {
        return fName;
    }

    public File getFile() {
        URL url = fileStore.findFile(fName, false);
        if (url != null) {
            try {
                return new File(url.toURI());
            } catch (URISyntaxException x) {
                Logging.logger().warning("invalid filesystem URI: " + url);
            }
        }
        return null;
    }

    public byte[] getFileContentsAsBytes() {
        synchronized (lock) {
            File f = getFile();
            if (f != null) {
                try {
                    return Files.readAllBytes(f.toPath());
                } catch (IOException iox) {
                    Logging.logger().warning("error reading cached file: " + iox);
                }
            }
        }
        return null;
    }

    public ByteBuffer getFileContentsAsByteBuffer() {
        byte[] contents = getFileContentsAsBytes();
        if (contents != null) {
            return ByteBuffer.wrap(contents);
        }
        return null;
    }

    public void storeFileContents (ByteBuffer buf) {
        try {
            synchronized (lock) {
                File f = fileStore.newFile(fName);
                FileChannel channel = new FileOutputStream(f, false).getChannel();
                channel.write(buf);
                channel.close();
            }
        } catch (IOException iox) {
            Logging.logger().warning("error writing cached file: " + iox);
        }
    }

    @Override
    public String toString() {
        return "FileStoreSource(" + fName + ')';
    }
}
