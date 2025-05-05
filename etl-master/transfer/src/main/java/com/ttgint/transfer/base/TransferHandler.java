package com.ttgint.transfer.base;

public interface TransferHandler {

    boolean startConnection();

    void preHandler();

    void checkLastModifiedTime();

    void readFiles();

    void filterFiles();

    void setFileInfo();

    void cacheResults();

    void setLastModifiedTime();

    void clearRemoteFiles();

    void download();

    void postHandler();

    void closeConnection();

}
