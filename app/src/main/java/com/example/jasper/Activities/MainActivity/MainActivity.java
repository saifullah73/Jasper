package com.example.jasper.Activities.MainActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jasper.Activities.Chat.ChatActivity;
import com.example.jasper.Activities.Login;
import com.example.jasper.Activities.MainActivity.Fragments.ChatListFragment;
import com.example.jasper.Activities.MainActivity.Fragments.About;
import com.example.jasper.Adapters.ViewPagerAdapter;
import com.example.jasper.AppBackend.Xmpp.XMPPConnection;
import com.example.jasper.AppBackend.Xmpp.XmppCore;
import com.example.jasper.Constants;
import com.example.jasper.Models.MessageModel;
import com.example.jasper.Notifications.Notifications;
import com.example.jasper.R;
import com.example.jasper.Utils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.roster.SubscribeListener;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements About.OnFragmentInteractionListener{

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private static MainActivity instance;
    private Notifications notif;


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
            instance = new MainActivity();
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
        instance = this;
        notif = new Notifications(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!XmppCore.getInstance().getXmppConnection().isConnected()){
            XmppCore.getInstance().reLogin();
        }
        updateMissedChatCount();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        XmppCore.getInstance().logout();
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



    private String getUserNameFromJid(String jid){
        return jid.substring(0,jid.indexOf("@"));
    }

    public void setBadgeCount(@NonNull String tab, int count) {

        if (tabLayout != null) {
            if (tab.equals("chats")) {
                updateBadgeCount(tab, count);
            }
        }
    }

    private void updateBadgeCount(String tab, int count) {
        try {
            if (tab.equals("chats")) {
                TabLayout.Tab mytab = tabLayout.getTabAt(0);
                View selected = null;
                if (mytab != null) {
                    selected = mytab.getCustomView();
                }
                TextView badge = null;
                if (selected != null) {
                    badge = selected.findViewById(R.id.badge);
                }

                if (badge != null) {
                    if (count == 0) {

                        badge.setText("0");
                        badge.setVisibility(GONE);
                    } else {
                        if (count > 99) {
                            badge.setText("99+");
                        } else {

                            badge.setText(String.valueOf(count));
                        }
                        badge.setVisibility(View.VISIBLE);
                    }
                } else {
                }
            } else if (tab.equals("groups")) {

            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateMissedChatCount() {
        int count = 0;
        for(Integer i : Constants.unread.values()){
            count += i;
        }
        setBadgeCount("chats", count);
    }

    private void onNewMessage(String from,MessageModel mess){
        String username = getUserNameFromJid(from);
        if (ChatActivity.isVisible() && !ChatActivity.isInstanceNull() && ChatActivity.getInstance().getUsername().equals(getUserNameFromJid(from))){
            ChatActivity.getInstance().insertMessage(mess);
        }
        else{
            showNotification(from,mess.getMessages());
            if (Constants.unread.get(username) != null){
                int unreadCount = Constants.unread.get(username);
                Constants.unread.put(username,unreadCount+1);
            }
            else{
                Constants.unread.put(username,1);
            }
        }
        Constants.lastMessage.put(username,Long.valueOf(getTimestamp()));
        updateMissedChatCount();
        ChatListFragment.getInstance().refresh();
    }

    public String getTimestamp(){
        return String.valueOf(System.currentTimeMillis());
    }

    public void showNotification(String title, String message){
        String newMess;
        if (message.contains("##image##")){
            newMess = "*Image";
        } else if (message.contains("##location##")) {
            newMess = "*Location";
        } else if (message.contains("##File##")) {
            newMess = "*File";
        }
        else{
            newMess = message;
        }
        title = title.substring(0,title.indexOf("@"));
        notif.displayNotification(title,newMess);
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
                public void newIncomingMessage(final EntityBareJid from, final org.jivesoftware.smack.packet.Message message, final Chat chat) {
                    Log.i("MessageIncoming", "New message from " + from + ": " + message.getBody());
                    final MessageModel data = new MessageModel("received", message.getBody().toString(),ChatActivity.getInstance().getTimestamp(),getMessageType(message.getBody()));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onNewMessage(from.toString(),data);

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
                }
            });

            Roster roster = Roster.getInstanceFor(XmppCore.getInstance().getXmppConnection());
            roster.addSubscribeListener(new SubscribeListener(){
                @Override
                public SubscribeAnswer processSubscribe(Jid from, Presence subscribeRequest){
                    subscribeRequest.setType(Presence.Type.subscribed);
                    Presence subscribe = new Presence(Presence.Type.subscribe);
                    subscribe.setTo(from);
                    subscribeRequest.setTo(from);
                    try{
                    XmppCore.getInstance().getXmppConnection().sendStanza(subscribeRequest);
                        XmppCore.getInstance().getXmppConnection().sendStanza(subscribe);
                    }
                    catch (Exception e){

                    }

                    //auto approve all incoming requests and send a presence request back if needed
                    return SubscribeAnswer.Approve;
                }
            });


            roster.addRosterListener(new RosterListener() {
                @Override
                public void entriesAdded(Collection<Jid> addresses) {
                    ChatListFragment.getInstance().refresh();
                }

                @Override
                public void entriesUpdated(Collection<Jid> addresses) {
                    ChatListFragment.getInstance().refresh();
                }

                @Override
                public void entriesDeleted(Collection<Jid> addresses) {
                    ChatListFragment.getInstance().refresh();
                }

                @Override
                public void presenceChanged(Presence presence) {
                    String username = presence.getFrom().toString().substring(0,presence.getFrom().toString().indexOf("@"));
                    Constants.map.put(username,presence.getFrom().getResourceOrEmpty().toString());
                    Log.i("XmppCoreR", "User = "+ username + " Resource = "+presence.getFrom().getResourceOrEmpty().toString());
                }
            });

        }else {
            Log.i("XmppCore","connection was null");
        }
    }


    public boolean addNewUser(String username){
        Presence subscribe = new Presence(Presence.Type.subscribe);
        //I get deprecated inspection report on setTo(), but it still works
        subscribe.setTo(username+"@"+ Constants.domain);
        try{
            XmppCore.getInstance().getXmppConnection().sendStanza(subscribe);
            return true;
        }
        catch(SmackException e){
            return false;
        }
        catch(InterruptedException e){
            return false;
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
            title2.setText("About");
            tabLayout.getTabAt(1).setCustomView(tab2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(ChatListFragment.getInstance(), "");
        adapter.addFragment(About.getInstance(), "");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.logout:
                XmppCore.getInstance().logout();
                finish();
                System.exit(0);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }


}
