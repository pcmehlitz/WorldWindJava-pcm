/*
 * Copyright (C) 2019 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.retrieve;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.cache.FileStoreSource;
import gov.nasa.worldwind.event.Message;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * pcm - a rudimentary retriever for FileStore backed data
 * used to unify processing in case data retrieved from network is stored/cached in the file system
 *
 * FIXME - should include time handling
 */
public class FileStoreRetriever implements Retriever {
    FileStoreSource fsSource;
    RetrievalPostProcessor postProcessor;
    ByteBuffer buffer = null;

    public FileStoreRetriever (FileStoreSource fsSource, RetrievalPostProcessor postProcessor) {
        this.fsSource = fsSource;
        this.postProcessor = postProcessor;
    }

    public Retriever call() throws Exception {
        postProcessor.run(this);
        return this;
    }

    @Override
    public ByteBuffer getBuffer() {
        // might be called several times - avoid re-reading the file
        if (buffer == null) {
            buffer = fsSource.getFileContentsAsByteBuffer();
        } else {
            buffer.rewind();
        }
        return buffer;
    }

    @Override
    public int getContentLength() {
        File f = fsSource.getFile();
        if (f != null) {
            return (int) f.length();  // FIXME should be a long
        } else {
            return 0;
        }
    }

    @Override
    public int getContentLengthRead() {
        return 0;
    }

    @Override
    public String getName() {
        return fsSource.getFileName();
    }

    @Override
    public String getState() {
        if (getContentLength() > 0) {
            return RETRIEVER_STATE_SUCCESSFUL;
        } else {
            return RETRIEVER_STATE_ERROR;
        }
    }

    @Override
    public String getContentType() {  // FIXME - this should not be here
        String fname = fsSource.getFileName();
        if (fname.endsWith(".xml")) {
            return "application/xml";
        } else if (fname.endsWith(".json")) {
            return "application/json";
        } else {
            return "application/octet-stream";
        }
    }

    @Override
    public long getExpirationTime() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getSubmitTime() {
        return 0;
    }

    @Override
    public void setSubmitTime(long submitTime) {

    }

    @Override
    public long getBeginTime() {
        return 0;
    }

    @Override
    public void setBeginTime(long beginTime) {

    }

    @Override
    public long getEndTime() {
        return 0;
    }

    @Override
    public void setEndTime(long endTime) {

    }

    @Override
    public int getConnectTimeout() {
        return 0;
    }

    @Override
    public int getReadTimeout() {
        return 0;
    }

    @Override
    public void setReadTimeout(int readTimeout) {

    }

    @Override
    public void setConnectTimeout(int connectTimeout) {

    }

    @Override
    public int getStaleRequestLimit() {
        return 0;
    }

    @Override
    public void setStaleRequestLimit(int staleRequestLimit) {

    }

    @Override
    public Object setValue(String key, Object value) {
        return null;
    }

    @Override
    public AVList setValues(AVList avList) {
        return null;
    }

    @Override
    public Object getValue(String key) {
        return null;
    }

    @Override
    public Collection<Object> getValues() {
        return null;
    }

    @Override
    public String getStringValue(String key) {
        return null;
    }

    @Override
    public Set<Map.Entry<String, Object>> getEntries() {
        return null;
    }

    @Override
    public boolean hasKey(String key) {
        return false;
    }

    @Override
    public Object removeKey(String key) {
        return null;
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {

    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {

    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {

    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {

    }

    @Override
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {

    }

    @Override
    public void firePropertyChange(PropertyChangeEvent propertyChangeEvent) {

    }

    @Override
    public AVList copy() {
        return null;
    }

    @Override
    public AVList clearList() {
        return null;
    }

    @Override
    public void onMessage(Message msg) {

    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

    }
}
