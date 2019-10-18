package com.example.jasper.AppBackend.Interfaces;

import com.example.jasper.Models.Contact;

import java.util.List;

public interface XMPPCore{
    public List<Contact> getContactsListForCurrentUser();
    public boolean sendMessage(String messageToSend,String entityBareId);
    public void reLogin();
}
