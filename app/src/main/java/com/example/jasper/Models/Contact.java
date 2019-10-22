package com.example.jasper.Models;

import com.example.jasper.Constants;

public class Contact {
    private String name;
    private int unreadMessageCount;
    private long lastMessageTime;

    public Contact(String name, int unreadMessageCount) {
        this.name = name;
        this.unreadMessageCount = unreadMessageCount;

    }

    public String getName() {
        return name;
    }

    public void setLastMessageTime(Long l){
        lastMessageTime = l;
    }

    public Long getLastMessageTime(){
        return lastMessageTime;
    }

    public int getUnreadMessageCount() {
        if (Constants.unread.get(name) != null){
            return Constants.unread.get(name);
        }
        else{
            return 0;
        }
    }
}
