package com.example.jasper.AppBackend.Xmpp;

import android.util.Log;

import com.example.jasper.AppBackend.Interfaces.XMPPCore;
import com.example.jasper.Models.Contact;
import com.example.jasper.Models.MessageModel;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class XmppCore implements XMPPCore {
    private static XmppCore instance;
    private AbstractXMPPConnection mConnection;

    public XmppCore(){
        if(XMPPConnection.mConnection != null)
            mConnection = XMPPConnection.mConnection;
        if (!XMPPConnection.mConnection.isConnected()){
            try {
                mConnection.connect();
                mConnection.login();
            }
            catch (Exception e){
                Log.e("CoreMessages","Error establishing Connection "+e.toString());
            }
        }
    }

    public AbstractXMPPConnection getXmppConnection(){
        return mConnection;
    }

    public static XmppCore getInstance(){
        if (instance == null){
            instance = new XmppCore();
        }
        return instance;
    }

    @Override
    public List<Contact> getContactsListForCurrentUser() {
        ArrayList<Contact> chatList = new ArrayList<Contact>();
        if (mConnection != null) {
            Roster roster = Roster.getInstanceFor(mConnection);
            Collection<RosterEntry> entries = roster.getEntries();
            for (RosterEntry entry : entries) {
                chatList.add(new Contact(entry.getName(),0));
            }

            roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
//                try {
//                    roster.createEntry(JidCreate.bareFrom("saif3@saifullah.chat"), "saif3", null);
//                }
//                catch (Exception e){
//
//                }
        }
        return chatList;
    }

    @Override
    public boolean sendMessage(String messageToSend, String entityBareId) {
        EntityBareJid jid = null;
        try {
            jid = JidCreate.entityBareFrom(entityBareId);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            return false;
        }
        if(XMPPConnection.mConnection != null) {
            ChatManager chatManager = ChatManager.getInstanceFor(mConnection);
            Chat chat = chatManager.chatWith(jid);
            org.jivesoftware.smack.packet.Message newMessage = new org.jivesoftware.smack.packet.Message();
            newMessage.setBody(messageToSend);
            try {
                chat.send(newMessage);
                return true;
            } catch (SmackException.NotConnectedException e) {
                Log.i("messageT","SmackNo: "+ e.toString());
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                Log.i("messageT","Interrupted: "+ e.toString());
                e.printStackTrace();
                return false;
            }
        }
        else{
            Log.i("messageT","Connection was null");
            return false;
        }
    }

    @Override
    public void reLogin(){
        try {
            mConnection.connect();
            mConnection.login();
        }
        catch (Exception e){

        }
    }

    @Override
    public void signup(String name, String username, String password, String confirmpassword) {

    }


}
