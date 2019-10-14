package com.example.jasper.Models;

public class Contact {
    private String name;
    private int unreadMessageCount;

    public Contact(String name, int unreadMessageCount) {
        this.name = name;
        this.unreadMessageCount = unreadMessageCount;
    }

    public String getName() {
        return name;
    }

    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }
}
