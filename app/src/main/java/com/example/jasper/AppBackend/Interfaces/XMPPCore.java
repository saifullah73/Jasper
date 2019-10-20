package com.example.jasper.AppBackend.Interfaces;

import com.example.jasper.Models.Contact;

import java.util.List;

public interface XMPPCore{
    List<Contact> getContactsListForCurrentUser();
    boolean sendMessage(String messageToSend,String entityBareId);
    void reLogin();
    void sendFile(String jid, String filepath, String desc,String des, CustomFileTransferListener listener);
}
