package com.example.jasper.AppBackend.Interfaces;

import com.example.jasper.Models.Contact;

import java.util.List;

public interface XMPPCore{
    public List<Contact> getContactsListForCurrentUser();
    public boolean sendMessage(String messageToSend,String entityBareId);
    public void reLogin();
    public void sendFile(final String username, final String pathName, String desc, final String res, CustomFileTransferListener listener);
}
