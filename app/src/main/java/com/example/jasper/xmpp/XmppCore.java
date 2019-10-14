package com.example.jasper.xmpp;

import com.example.jasper.Interfaces.XMPPCore;

public class XmppCore implements XMPPCore {
    private static XmppCore instance;

    public static XmppCore getInstance(){
        if (instance == null){
            return new XmppCore();
        }
        return instance;
    }
}
