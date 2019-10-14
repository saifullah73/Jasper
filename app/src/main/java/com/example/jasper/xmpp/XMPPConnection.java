package com.example.jasper.xmpp;

import com.example.jasper.Activities.Login;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class XMPPConnection {
    private static final String TAG = "XMPPConnection";
    public static AbstractXMPPConnection mConnection;


    public static void setConnection(final String username,final  String password, final String domain){

        new Thread(){
            @Override
            public void run() {
                InetAddress addr = null;
                try {
                    addr = InetAddress.getByName(domain);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                HostnameVerifier verifier = new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return false;
                    }
                };
                DomainBareJid serviceName = null;
                try {
                    serviceName = JidCreate.domainBareFrom(domain);
                } catch (XmppStringprepException e) {
                    e.printStackTrace();
                }
                XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                        .setUsernameAndPassword(username,password)
                        .setPort(5222)
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                        .setXmppDomain(serviceName)
                        .setHostnameVerifier(verifier)
                        .setHostAddress(addr)
                        .setDebuggerEnabled(true)
                        .build();
                mConnection = new XMPPTCPConnection(config);


                try {
                    mConnection.connect();
                    // all these proceedure also thrown error if you does not seperate this thread now we seperate thread create
                    mConnection.login();

                    if(mConnection.isAuthenticated() && mConnection.isConnected()){
                        //now send message and receive message code here
                        Login.getInstance().success();
                    }

                } catch (SmackException e) {
                    Login.getInstance().failure();
                    e.printStackTrace();
                } catch (IOException e) {
                    Login.getInstance().failure();
                    e.printStackTrace();
                } catch (XMPPException e) {
                    Login.getInstance().failure();
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Login.getInstance().failure();
                    e.printStackTrace();
                }
            }
        } .start();

    }
}
