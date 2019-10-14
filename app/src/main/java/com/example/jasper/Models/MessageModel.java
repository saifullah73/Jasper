package com.example.jasper.Models;


public class MessageModel {
    private String heading,messages;
    private String imageMsgStatus;
    private boolean isDecorated;
    private long date;
    private String msgType;
    private boolean expandable, isSender, isSelected;

    public String getImageMsgStatus() {
        return imageMsgStatus;
    }

    public boolean isDecorated() {
        return isDecorated;
    }

    public long getDate() {
        return date;
    }

    public String getMsgType() {
        return msgType;
    }

    public boolean getExpandable() {
        return expandable;
    }

    public boolean getSelected() {
        return isSelected;
    }
    public boolean getSender() {
        return isSender;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public void setImageMsgStatus(String imageMsgStatus) {
        this.imageMsgStatus = imageMsgStatus;
    }

    public void setDecorated(boolean decorated) {
        isDecorated = decorated;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
    }

    public void setSender(boolean sender) {
        isSender = sender;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }



    public MessageModel(String head, String mess){
        heading = head;
        messages = mess;
    }
    public String getHeading(){
        return heading;
    }
    public String getMessages(){
        return messages;
    }
}
