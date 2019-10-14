package com.example.jasper.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.jasper.Adapters.Adapter;
import com.example.jasper.Models.MessageModel;
import com.example.jasper.R;
import com.example.jasper.xmpp.XMPPConnection;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private Adapter mAdapter;
    private ArrayList<MessageModel> mMessagesData = new ArrayList<>();
    public static final String TAG = MainActivity.class.getSimpleName();
    private EditText sendMessageEt;
    private Button sendBtn;
    private String username;
    private String domainName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = getIntent();
        if (i!=null){
            username = i.getStringExtra("username");
        }
        domainName = getString(R.string.domainName);

        mRecyclerView = findViewById(R.id.rv);
        mAdapter = new Adapter(mMessagesData);
        sendMessageEt = findViewById(R.id.sendMessageEt);
        sendBtn = findViewById(R.id.send);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        DividerItemDecoration decoration = new DividerItemDecoration(this,manager.getOrientation());

        mRecyclerView.addItemDecoration(decoration);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);

        if(XMPPConnection.mConnection !=null) {
            ChatManager chatManager = ChatManager.getInstanceFor(XMPPConnection.mConnection);
            chatManager.addListener(new IncomingChatMessageListener() {
                @Override
                public void newIncomingMessage(EntityBareJid from, org.jivesoftware.smack.packet.Message message, Chat chat) {
                    Log.e(TAG, "New message from " + from + ": " + message.getBody());
                    MessageModel data = new MessageModel("received", message.getBody().toString());
                    mMessagesData.add(data);

                    //now update recyler view
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter = new Adapter(mMessagesData);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    });
                }
            });
        }
        else{
            Intent i1 = new Intent(MainActivity.this, Login.class);
            startActivity(i);
            finish();
        }

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageSend = sendMessageEt.getText().toString();
                if(messageSend.length() > 0 ){


                    sendMessage(messageSend,username+"@"+domainName);
                }
            }
        });

    }

    private void sendMessage(String messageSend, String entityBareId) {

        EntityBareJid jid = null;
        try {
            jid = JidCreate.entityBareFrom(entityBareId);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
            Log.i("messageT","Smack: "+ e.toString());
        }

        Log.i("messageT","jid: "+ jid.toString());

        if(XMPPConnection.mConnection != null) {

            ChatManager chatManager = ChatManager.getInstanceFor(XMPPConnection.mConnection);
            Chat chat = chatManager.chatWith(jid);
            org.jivesoftware.smack.packet.Message newMessage = new org.jivesoftware.smack.packet.Message();
            newMessage.setBody(messageSend);

            try {
                chat.send(newMessage);
                MessageModel data = new MessageModel("send",messageSend);
                mMessagesData.add(data);
                mAdapter = new Adapter(mMessagesData);
                mRecyclerView.setAdapter(mAdapter);



            } catch (SmackException.NotConnectedException e) {
                Log.i("messageT","SmackNo: "+ e.toString());
                e.printStackTrace();
            } catch (InterruptedException e) {
                Log.i("messageT","Interrupted: "+ e.toString());
                e.printStackTrace();
            }
        }
        else{
            Log.i("messageT","Connection was null");
        }
    }
}
