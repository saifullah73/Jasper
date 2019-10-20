package com.example.jasper.AppBackend.Xmpp;

import android.util.Log;

import com.example.jasper.AppBackend.Interfaces.CustomFileTransferListener;
import com.example.jasper.AppBackend.Interfaces.XMPPCore;
import com.example.jasper.AppBackend.Presistance.DBHelper;
import com.example.jasper.Constants;
import com.example.jasper.Models.Contact;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.XmppStringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class XmppCore implements XMPPCore {
    private static final String TAG = "XmppCore";
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
//                try {
//                    roster.createEntry(JidCreate.bareFrom("saif3@saifullah.chat"), "saif3", null);
//                }
//                catch (Exception e){
//
//                }
        }
        return chatList;
    }


    public void initialzeRoasterListener(){
        Roster roster = Roster.getInstanceFor(mConnection);
        roster.addRosterListener(new RosterListener() {
            @Override
            public void entriesAdded(Collection<Jid> addresses) {

            }

            @Override
            public void entriesUpdated(Collection<Jid> addresses) {

            }

            @Override
            public void entriesDeleted(Collection<Jid> addresses) {

            }

            @Override
            public void presenceChanged(Presence presence) {
                Log.i("XmppCore", "Presence Changed");
                Constants.resource = presence.getFrom().getResourceOrEmpty().toString();
            }
        });
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
    public void sendFile(final String username, final String pathName, String desc, final String res, CustomFileTransferListener listener) {
//        if (!mConnection.isConnected()){
//            Log.i("XmppCore", "Connection was not established");
//            try {
//                Log.i("XmppCore", "Trying to reconnect");
//                mConnection.connect();
//                mConnection.login();
//            }
//            catch (Exception e){
//                Log.i("XmppCore", "Error while reconnecting:" + e.toString());
//            }
//        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FileTransferManager manager = FileTransferManager.getInstanceFor(mConnection);
                try {
                    EntityFullJid i = JidCreate.entityFullFrom(XmppStringUtils.completeJidFrom(username, Constants.domain, res));
                    Log.i("XmppCore","jid = "+ i.toString());
                    OutgoingFileTransfer oft = manager.createOutgoingFileTransfer(i);
                    File f = new File(pathName);
                    Log.i("XmppCore", "file Size = "+ f.length());
                    FileInputStream stream = new FileInputStream(f);
                    oft.sendStream(stream, f.getAbsolutePath(),f.length(), "A greeting");
                    outerloop:
                    while (!oft.isDone()) {
                        switch (oft.getStatus()) {
                            case error:
                                Log.i("XmppCore", "sendFile: "+ oft.getError());
                                break outerloop;
                            default:
                                Log.i("XmppCore","Filetransfer status: " + oft.getStatus() + ". Progress: " + oft.getProgress());
                                break;
                        }
                    }
                    Log.i("XmppCore","Filetransfer done " + oft.getStatus() + ". Progress: " + oft.getProgress() + ". Error " + oft.getError() + ". Exception" + oft.getException());
                }
                catch (Exception e){
                    Log.i("XmppCore", "sendFile: "+ e.toString());
                }
            }
        });
        thread.start();
    }
}
