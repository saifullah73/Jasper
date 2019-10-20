package com.example.jasper.AppBackend.Interfaces;

import com.example.jasper.Models.MessageModel;

import java.util.List;

public interface FetchCallbackListener {
    public void onSuccess(List<MessageModel> list);
}
