package com.example.jasper.AppBackend.Interfaces;

public interface CustomFileTransferListener {
    void onFailure();
    void onSuccess();
    int getProgress();
}
