package com.example.jasper.Activities.MainActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jasper.Activities.Chat.ChatActivity;
import com.example.jasper.Activities.Login;
import com.example.jasper.Activities.MainActivity.Fragments.ChatListFragment;
import com.example.jasper.Activities.MainActivity.Fragments.GroupsFragment;
import com.example.jasper.Activities.MainActivity.Fragments.RequestFragment;
import com.example.jasper.Adapters.ViewPagerAdapter;
import com.example.jasper.AppBackend.Presistance.DBHelper;
import com.example.jasper.AppBackend.Xmpp.XMPPConnection;
import com.example.jasper.AppBackend.Xmpp.XmppCore;
import com.example.jasper.Models.MessageModel;
import com.example.jasper.R;
import com.example.jasper.Utils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jxmpp.jid.EntityBareJid;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements GroupsFragment.OnFragmentInteractionListener, RequestFragment.OnFragmentInteractionListener{

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private static MainActivity instance;

    private TabLayout.OnTabSelectedListener OnTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            int c = tab.getPosition();
            adapter.SetOnSelectView(tabLayout, c, getApplicationContext());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            int c = tab.getPosition();
            adapter.SetUnSelectView(tabLayout, c, getApplicationContext());
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

    public static MainActivity getInstance(){
        if(instance == null){
            new MainActivity();
        }
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (XmppCore.getInstance().getXmppConnection() == null) {
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        }
        initComponents();
        initPermissions();
        initListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!XmppCore.getInstance().getXmppConnection().isConnected()){
            XmppCore.getInstance().reLogin();
        }
        XmppCore.getInstance().initialzeRoasterListener();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void initComponents(){
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager2);
        viewPager.setOffscreenPageLimit(2);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setUpCustomViewTab();
        tabLayout.addOnTabSelectedListener(OnTabSelectedListener);
        adapter.SetOnSelectView(tabLayout, viewPager.getCurrentItem(), getApplicationContext());
    }


    private void onNewMessage(MessageModel mess){
        if (ChatActivity.isVisible()){
            ChatActivity.getInstance().insertMessage(mess);
        }
        else{

        }
    }

    public String getMessageType(String mess){
        if(mess.contains("##image##")){
            return "image";
        }
        else if(mess.contains("##file##")){
            return "file";
        }
        else if(mess.contains("##location##")){
            return "location";
        }
        else{
            return "text";
        }
    }
    private void initListeners(){
        if(XmppCore.getInstance().getXmppConnection()!=null) {
            ChatManager chatManager = ChatManager.getInstanceFor(XMPPConnection.mConnection);
            chatManager.addListener(new IncomingChatMessageListener() {
                @Override
                public void newIncomingMessage(EntityBareJid from, final org.jivesoftware.smack.packet.Message message, Chat chat) {
                    Log.i("MessageIncoming", "New message from " + from + ": " + message.getBody());
                    final MessageModel data = new MessageModel("received", message.getBody().toString(),ChatActivity.getInstance().getTimestamp(),getMessageType(message.getBody()));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onNewMessage(data);
                        }
                    });
                }
            });
            FileTransferManager ftm1 = FileTransferManager.getInstanceFor(XmppCore.getInstance().getXmppConnection());
            ftm1.addFileTransferListener(new FileTransferListener() {
                @Override
                public void fileTransferRequest(FileTransferRequest request) {
                    IncomingFileTransfer ift = request.accept();
                    Log.i("XmppCore",request.getFileName());
                    Log.i("XmppCore",String.valueOf(request.getFileSize()));
                    Log.i("XmppCore","Got a file");
                    createDirectoryAndSaveFile(ift,Utils.getFileNameFromPath(request.getFileName()));
//                    if(Utils.isImage(request.getFileName())){
//                        final MessageModel data;
//                        data = new MessageModel("received","##image##"+request.getFileName(),ChatActivity.getInstance().getTimestamp(),"image");
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                onNewMessage(data);
//                            }
//                        });
//                    }
//                    else{
//                        final MessageModel data;
//                        data = new MessageModel("received","##file##"+request.getFileName(),ChatActivity.getInstance().getTimestamp(),"file");
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                onNewMessage(data);
//                            }
//                        });
//                    }
                }
            });
        }else {
            Log.i("XmppCore","connection was null");
        }
    }



    private void initPermissions(){
        checkAndRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,0);
        checkAndRequestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,1);
    }

    private void createDirectoryAndSaveFile(IncomingFileTransfer ift, String fileName) {
        File direct = new File(Environment.getExternalStorageDirectory() + "/Jasper");
        Log.i("XmppCore","1");
        if (!direct.exists()) {
            direct.mkdirs();
            Log.i("XmppCore","2");
        }
        File file = new File(direct, fileName);
        Log.i("XmppCore","3");
        if (file.exists()) {
            file.delete();
            Log.i("XmppCore","4");
        }
        try {
            InputStream is = ift.recieveFile();
            FileOutputStream os = new FileOutputStream(file);
            int nRead;
            byte[] buf = new byte[1024];
            Log.i("XmppCore","5");
            while ((nRead = is.read(buf,  0, buf.length)) != -1) {
                os.write(buf, 0, nRead);
            }
            os.flush();
            Log.i("XmppCore","6");
        }catch (SmackException | IOException | XMPPException.XMPPErrorException | InterruptedException e) {
            Log.i("XmppCore","Error : " + e.toString());
        }
    }


    private void onMessageRecieved(MessageModel data){
        //TODO: notify user
        //
    }


    private void setUpCustomViewTab() {
        try {
            RelativeLayout tab1 = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab_title, null);
            TextView title1 = tab1.findViewById(R.id.tab);
            TextView badge1 = tab1.findViewById(R.id.badge);
            badge1.setVisibility(GONE);
            title1.setText("Chats");
            tabLayout.getTabAt(0).setCustomView(tab1);

            RelativeLayout tab2 = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab_title, null);
            TextView title2 = tab2.findViewById(R.id.tab);
            TextView badge2 = tab2.findViewById(R.id.badge);
            badge2.setVisibility(GONE);
            title2.setText("Groups");
            tabLayout.getTabAt(1).setCustomView(tab2);

            RelativeLayout tab3 = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab_title, null);
            TextView title3 = tab3.findViewById(R.id.tab);
            TextView badge3 = tab3.findViewById(R.id.badge);
            title3.setText("Requests");
            tabLayout.getTabAt(2).setCustomView(tab3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(ChatListFragment.getInstance(), "");
        adapter.addFragment(GroupsFragment.getInstance(), "");
        adapter.addFragment(RequestFragment.getInstance(), "");
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void checkAndRequestPermission(String permission, int result) {
        int permissionGranted = getPackageManager().checkPermission(permission, getPackageName());
        if (permissionGranted != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
            ActivityCompat.requestPermissions(this, new String[]{permission}, result);
        }
    }


}
