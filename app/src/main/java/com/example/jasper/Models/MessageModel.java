package com.example.jasper.Models;


public class MessageModel {
    private String messages,status;
    private String imageMsgStatus;
    private boolean isDecorated;
    private long date;
    private String msgType;
    private boolean expandable, isSender, isSelected;
    private int storageId;




    public MessageModel(String head,String msg, String timestamp){
        messages = msg;
        status = "sent";
        msgType = "text";
        date = Long.valueOf(timestamp);
        expandable = false;
        isDecorated = false;
        if(head.equals("received")){
            isSender = false;
        }
        else{
            isSender = true;
        }
        isSelected = false;
        imageMsgStatus = "uploaded";
    }

    public MessageModel(String head,String msg, String timestamp,String type){
        messages = msg;
        status = "sent";
        msgType = type;
        date = Long.valueOf(timestamp);
        expandable = false;
        isDecorated = false;
        if(head.equals("received")){
            isSender = false;
        }
        else{
            isSender = true;
        }
        isSelected = false;
        imageMsgStatus = "uploaded";
    }


    public MessageModel(String head,String msg, String timestamp,String type,String imgStatus){
        messages = msg;
        status = "sent";
        msgType = type;
        date = Long.valueOf(timestamp);
        expandable = false;
        isDecorated = false;
        if(head.equals("received")){
            isSender = false;
        }
        else{
            isSender = true;
        }
        isSelected = false;
        imageMsgStatus = imgStatus;
    }




    public String getImageMsgStatus() {
        return imageMsgStatus;
    }

    public boolean isDecorated() {
        return isDecorated;
    }

    public long getDate() {
        return date;
    }

    public int getStorageId() {
        return storageId;
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

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    public String getMessages(){
        return messages;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
