package com.example.jasper.AppBackend.Xmpp;

import android.util.Log;
import android.widget.Toast;

import com.example.jasper.Activities.Chat.ChatActivity;
import com.example.jasper.Activities.Signup;
import com.example.jasper.AppBackend.Interfaces.CustomFileTransferListener;
import com.example.jasper.AppBackend.Interfaces.XMPPCore;
import com.example.jasper.AppBackend.Presistance.DBHelper;
import com.example.jasper.Constants;
import com.example.jasper.Models.Contact;
import com.example.jasper.Utils;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.XmppStringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class XmppCore implements XMPPCore {
    private static final String TAG = "XmppCore";
    private static XmppCore instance;
    private AbstractXMPPConnection mConnection;

    public XmppCore() {
        if (XMPPConnection.mConnection != null)
            mConnection = XMPPConnection.mConnection;
        if (true) {
            try {
                mConnection.connect();
                mConnection.login();
            } catch (Exception e) {
                Log.e("CoreMessages", "Error establishing Connection " + e.toString());
            }
        }
    }

    public AbstractXMPPConnection getXmppConnection() {
        return mConnection;
    }

    public static XmppCore getInstance() {
        if (instance == null) {
            instance = new XmppCore();
        }
        return instance;
    }


    public void searchUser(String username) {
        UserSearchManager search = new UserSearchManager(mConnection);
        try {
            List<DomainBareJid> services = search.getSearchServices();
            for (DomainBareJid jid : services) {
                Log.i("XmppSearch", "Different Service ");
                Form searchForm = search.getSearchForm(jid);
                Form answerForm = searchForm.createAnswerForm();
                answerForm.setAnswer("Username", true);
                answerForm.setAnswer("user", username + "@" + Constants.domain);
                ReportedData data = search.getSearchResults(answerForm, jid);
                if (data.getRows() != null) {
                    for (ReportedData.Row row : data.getRows()) {
                        for (String value : row.getValues("jid")) {
                            Log.i("XmppSearch", " " + value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.i("XmppSearch", e.toString());
        }
    }

    @Override
    public List<Contact> getContactsListForCurrentUser() {
        ArrayList<Contact> chatList = new ArrayList<Contact>();
        if (mConnection != null) {
            Roster roster = Roster.getInstanceFor(mConnection);
            Collection<RosterEntry> entries = roster.getEntries();
            for (RosterEntry entry : entries) {
                String username = entry.getJid().toString().substring(0,entry.getJid().toString().indexOf("@"));
                chatList.add(new Contact(extractUserName(entry.getJid().toString()), 0));
                Log.i("DynamicContacts", "Added " + entry.getJid());
            }
        }
        return chatList;
    }

    public String getResourceOfUser(String jid){
        Roster roster = Roster.getInstanceFor(mConnection);
        try {
            Presence p =  roster.getPresence(JidCreate.bareFrom(jid));
            Log.i("XmppCoreR","got presence of "+jid+ " presence = "+ p.toString());
            return  p.getFrom().getResourceOrEmpty().toString();
        }
        catch (Exception e){
            Log.i("XmppCoreR", "Error getting presence of "+ jid);
            Log.i("XmppCoreR", e.toString());
            return null;
        }
    }



    private String extractUserName(String jid) {
        return jid.substring(0, jid.indexOf("@"));
    }


    public void initialzeRoasterListener() {
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
        if (XMPPConnection.mConnection != null) {
            ChatManager chatManager = ChatManager.getInstanceFor(mConnection);
            Chat chat = chatManager.chatWith(jid);
            org.jivesoftware.smack.packet.Message newMessage = new org.jivesoftware.smack.packet.Message();
            newMessage.setBody(messageToSend);
            try {
                boolean f = XmppCore.instance.mConnection.isConnected();
                Log.i("XmppCore","Send Message() isConnected = "+f);
                chat.send(newMessage);
                return true;
            } catch (SmackException.NotConnectedException e) {
                Log.i("messageT", "SmackNo: " + e.toString());
                e.printStackTrace();
                return false;
            } catch (InterruptedException e) {
                Log.i("messageT", "Interrupted: " + e.toString());
                e.printStackTrace();
                return false;
            }
        } else {
            Log.i("messageT", "Connection was null");
            return false;
        }
    }

    @Override
    public void reLogin() {
        try {
            mConnection.connect();
            mConnection.login();
        } catch (Exception e) {

        }
    }


    public void logout() {
        mConnection.disconnect();
    }

    @Override
    public void sendFile(final String username, final String pathName, String desc, final String res, CustomFileTransferListener listener) {
        Log.i("XmppCore", "File Sharing res Got " + res);
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
                    Log.i("XmppCore", "jid = " + i.toString());
                    OutgoingFileTransfer oft = manager.createOutgoingFileTransfer(i);
                    File f = new File(pathName);
                    Log.i("XmppCore", "file Size = " + f.length());
                    FileInputStream stream = new FileInputStream(f);
                    oft.sendStream(stream, f.getAbsolutePath(), f.length(), "A greeting");
                    outerloop:
                    while (!oft.isDone()) {
                        switch (oft.getStatus()) {
                            case error:
                                Log.i("XmppCore", "sendFile: " + oft.getError());
                                break outerloop;
                            default:
                                Log.i("XmppCore", "Filetransfer status: " + oft.getStatus() + ". Progress: " + oft.getProgress());
                                break;
                        }
                    }
                    ChatActivity.getInstance().sendFileMessage(pathName);
                    Log.i("XmppCore", "Filetransfer done " + oft.getStatus() + ". Progress: " + oft.getProgress() + ". Error " + oft.getError() + ". Exception" + oft.getException());
                } catch (Exception e) {
                    Log.i("XmppCore", "sendFile: " + e.toString());
                }
            }
        });
        thread.start();
    }

    public static void signup(final String name, final String username, final String password, final String domain) {
        new Thread() {
            @Override
            public void run() {
                InetAddress addr = null;
                try {
                    addr = InetAddress.getByName(domain);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Log.i("Signup" , "error in addr"+ e.toString());
                }
                HostnameVerifier verifier = new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return false;
                    }
                };
                DomainBareJid serviceName = null;
                try {
                    serviceName = JidCreate.domainBareFrom(Constants.domain);
                } catch (XmppStringprepException e) {
                    e.printStackTrace();
                    Log.i("Signup" , "error in servicename"+ e.toString());
                }
                XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
                        config.setPort(5222);
                        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
                        config.setXmppDomain(serviceName);
                        config.setHostnameVerifier(verifier);
                        config.setHostAddress(addr);
                        config.setDebuggerEnabled(true);
                        XMPPTCPConnection.setUseStreamManagementResumptiodDefault(true);
                        XMPPTCPConnection.setUseStreamManagementDefault(true);
                        config.setSendPresence(true);
                        config.setCompressionEnabled(false);
                AbstractXMPPConnection connection = new XMPPTCPConnection(config.build());
                try {
                    connection.connect();
                    Log.i("Signup", "Connected to " + connection.getHost());
                    AccountManager accountManager = AccountManager.getInstance(connection);
                    if(accountManager.supportsAccountCreation()) {
                        Map<String, String> attributes = new HashMap<>();
                        attributes.put("name", name);
                        accountManager.sensitiveOperationOverInsecureConnection(true);
                        accountManager.createAccount(Localpart.from(username), password,attributes);
                        Log.i("Signup","OKIDOKI");
                        Signup.getInstance().success();
                    }
                    else{
                        Log.i("Signup","NO support for account creation");
                    }
                } catch (SmackException | IOException | XMPPException | InterruptedException e) {
                    Log.e("Signupe", e.getMessage());
                    Signup.getInstance().failure();
                }
            }
        }.start();
    }
}
