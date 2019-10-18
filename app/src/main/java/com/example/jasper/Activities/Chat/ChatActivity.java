package com.example.jasper.Activities.Chat;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.content.*;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jasper.Adapters.ChatMessageAdapter;
import com.example.jasper.AppBackend.Xmpp.XmppCore;
import com.example.jasper.Models.MessageModel;
import com.example.jasper.R;
import com.example.jasper.AppBackend.Xmpp.XMPPConnection;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import at.markushi.ui.CircleButton;


//import id.zelory.compressor.Compressor;
//import okhttp3.MediaType;
//import okhttp3.MultipartBody;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;

import static android.view.View.GONE;

public class ChatActivity extends AppCompatActivity implements ChatMessageAdapter.ChangeToolbarMenuItemsListener {
    private static final String TAG = "ChatActivity";
    private static final int ADD_PHOTO = 1337;
    private enum MessageType {LOCATION, IMAGE, TEXT}
    private static ChatActivity instance;
    //Views
    private Toolbar toolbar;
    private AppCompatImageButton insert, sendSms;
    private AppCompatEditText editMsg;
    private LinearLayout extra;
    private RecyclerView recyclerView;
    private List<MessageModel> mMessages;
    private ChatMessageAdapter mAdapter;
    private CircleButton openCamera, sendLocaiton, openGallery, sendDocument;
    private Menu menu;
    private AppCompatImageView scrollToBottom;
    private static boolean isVisibile = false;
    private boolean isAtBottom;

    //vars
    String domainName;

    //  To send image
    private Uri imageToUploadUri;
    private String fileSharedUri;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Log.i("NewImp", "here");
        initViews();
        initListeners();
        hideKeyboard();
        initRecyclerView();
        instance = this;
        Intent i = getIntent();
        if (i != null) {
            username = i.getStringExtra("username");
            initChatRoom(username);
        }
        else {
            username = "User";
            initChatRoom(username);
        }
        domainName = getString(R.string.domainName);

        mAdapter.setInterface(this);


    }

    @Override
    public void ChangeToolBarMenuItems(int selectedMsgs, boolean onlyTextMsgSelected) {
        if (menu != null) {
            changeToolbarIcons(selectedMsgs, onlyTextMsgSelected);
        }
    }

    private void initListeners(){
        if(XmppCore.getInstance().getXmppConnection()!=null) {
            ChatManager chatManager = ChatManager.getInstanceFor(XMPPConnection.mConnection);
            chatManager.addListener(new IncomingChatMessageListener() {
                @Override
                public void newIncomingMessage(EntityBareJid from, org.jivesoftware.smack.packet.Message message, Chat chat) {
                    Log.e(TAG, "New message from " + from + ": " + message.getBody());
                    final MessageModel data = new MessageModel("received", message.getBody().toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            insertMessage(data);
                        }
                    });
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.chat_activity_menu, menu);
        return true;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = this.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(getApplicationContext());
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        view.clearFocus();
    }

    private void showDeleteConfirmationDialog(final List<MessageModel> list) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (list.size() > 0 && list.size() == 1) {
            builder.setMessage("Delete message?");
        } else {
            builder.setMessage("Delete " + list.size() + " messages?");
        }

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteSelectedMessages(list);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void changeToolbarIcons(int selectedMsgCount, boolean onlyTextMsgSelected) {
        try {
            if (menu != null) {
                showChatActivityLogs("Modifying Menu items");
                MenuItem item = menu.findItem(R.id.show_selected_msg_detail);
                MenuItem item2 = menu.findItem(R.id.delete_one_message);
                MenuItem item3 = menu.findItem(R.id.save_message);
                if (selectedMsgCount > 0) {
                    if (selectedMsgCount == 1) {
                        if (onlyTextMsgSelected) {
                            item3.setVisible(true);
                        } else {
                            item3.setVisible(false);
                        }
                        item.setVisible(true);
                        item2.setVisible(true);
                    } else {
                        item3.setVisible(false);
                        item.setVisible(false);
                        item2.setVisible(true);
                    }
                    changeBackIcon(true);
                    Objects.requireNonNull(getSupportActionBar()).setTitle("" + selectedMsgCount);
                } else {
                    item.setVisible(false);
                    item2.setVisible(false);
                    item3.setVisible(false);
                    changeBackIcon(false);
                    Objects.requireNonNull(getSupportActionBar()).setTitle(username);
                }

            }

        } catch (Exception e) {

        }
    }

    private void changeBackIcon(boolean change) {
        if (change) {
            final Drawable backbtn = ContextCompat.getDrawable(this, R.drawable.ic_close_white_24dp);
            if (backbtn != null) {
                backbtn.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setHomeAsUpIndicator(backbtn);
            }
        } else {
            final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
            if (upArrow != null) {
                upArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setHomeAsUpIndicator(upArrow);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.show_selected_msg_detail:
                showMessageDetailDialog(mAdapter.getDeletedAbleList().get(0));
                break;
            case R.id.delete_one_message:
                showDeleteConfirmationDialog(mAdapter.getDeletedAbleList());
                break;
            case R.id.save_message:
                copyTextMessageToClipboard(mAdapter.getDeletedAbleList().get(0));
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    private void showMessageDetailDialog(MessageModel message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.message_detail_dialog_layout, null);
        TextView msgDate = dialogView.findViewById(R.id.message_dialog_date);
        TextView msgStatus = dialogView.findViewById(R.id.message_dialog_status);
        msgDate.setText(ChatActivity.getInstance().timestampToHumanDate(message.getDate()));
        msgStatus.setText(message.getStatus());
        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (mAdapter.isDeleteModeOn()) {
            mAdapter.putDeleteModeOff();
        } else {
            super.onBackPressed();
        }
    }

    private void deleteSelectedMessages(List<MessageModel> messages) {
        for (int i = 0; i < messages.size(); i++) {
            mMessages.remove(messages.get(i));
            updateAdapter();
        }
        mAdapter.putDeleteModeOff();
    }

    private void copyTextMessageToClipboard(MessageModel msg) {
        String msgText = null;
        if (msg != null) {
            msgText = msg.getMessages();
        }

        if (msgText != null) {
            ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = android.content.ClipData.newPlainText("MessageText", msgText);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
            Toast.makeText(this, msgText, Toast.LENGTH_SHORT).show();
        }
        mAdapter.putDeleteModeOff();
    }

    public void deleteMessage(MessageModel message, int i) {
       //chatRoom.deleteMessage(message.getParentMessage());
        mMessages.remove(message);
        updateAdapter();
    }


    public static ChatActivity getInstance() {
        if (instance == null)
            instance = new ChatActivity();
        return instance;
    }



    private void updateAdapter() {
        if (mAdapter.getItemCount() != 0) {
            recyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            hideScrollToBottomBtn();
            mAdapter.notifyDataSetChanged();
        }
    }

    private void insertIncominMessage(){
//        SygnalAddress from = cr.getPeerAddress();
//        if (from.asStringUriOnly().equals(sipUri)) {
//            cr.markAsRead();
//            MainActivity.getInstance().updateMissedChatCount();
//            String type = getMessageType(message);
//            if (!message.getText().contains("#image#memory*-*")) {
//                insertMessage(ChatMessage.toChatMessage(message, type, false));
//            } else {
//                chatRoom.deleteMessage(message);
//                chatRoom.markAsRead();
//            }
//            String externalBodyUrl = message.getExternalBodyUrl();
//            SygnalContent fileTransferContent = message.getFileTransferInformation();
//            if (externalBodyUrl != null || fileTransferContent != null) {
//                MainActivity.getInstance().checkAndRequestWriteExternalStoragePermission();
//            }
//        }
    }
    private void insertMessage(MessageModel message) {
        scrollToBottom.setVisibility(GONE);
        mMessages.add(message);
        updateAdapter();
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerview);
        mMessages = new ArrayList<>();
        mAdapter = new ChatMessageAdapter(mMessages);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        manager.setStackFromEnd(true);
        mAdapter.setHasStableIds(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(30);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setAdapter(mAdapter);

        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @SuppressLint("SyntheticAccessor")
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (extra.getVisibility() == View.VISIBLE) {
                    extra.setVisibility(View.GONE);
                }
                if (!recyclerView.canScrollVertically(1)) {
                    hideScrollToBottomBtn();
                    isAtBottom = true;
                } else {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        scrollToBottom.setVisibility(View.VISIBLE);
                    }
                    isAtBottom = false;
                }
            }
        });

        //Assigns observer to adapter and LayoutManager to RecyclerView
        recyclerView.getRecycledViewPool().setMaxRecycledViews(ChatMessageAdapter.SENT_MESSAGE_TYPE, 0);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(ChatMessageAdapter.RECEIVED_MESSAGE_TYPE, 0);
        recyclerView.setLayoutManager(manager);
        mAdapter.notifyDataSetChanged();
    }

    public String timestampToHumanDate(long timestamp) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timestamp);

            SimpleDateFormat dateFormat;
            if (isToday(cal)) {
                dateFormat = new SimpleDateFormat(this.getResources().getString(R.string.today_date_format));
            } else {
                dateFormat = new SimpleDateFormat(this.getResources().getString(R.string.messages_date_format));
            }

            return dateFormat.format(cal.getTime());
        } catch (NumberFormatException nfe) {
            return String.valueOf(timestamp);
        }
    }

    private boolean isToday(Calendar cal) {
        return isSameDay(cal, Calendar.getInstance());
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            return false;
        }

        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }



    private String getMessageType(MessageModel message){
//        String text =  message.getText();
//        if (text.contains("data*latlon#")){
//            return "location";
//        }
//        else if (text.contains("#image#memory*-*")){
//            return "image";
//        }
//        else if (text.contains("#image#server*-*")){
//            return "image";
//        }
//        else {
//            return "text";
//        }
        return "text";
    }

    public void showChatActivityLogs(String msg) {
        Log.d("ChatActivityLogs", "showAdapterLogs: " + msg);
    }

    public void hideScrollToBottomBtn() {
        scrollToBottom.setVisibility(View.INVISIBLE);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        insert = findViewById(R.id.insert_extra_btn);
        extra = findViewById(R.id.extra_layout);
        sendSms = findViewById(R.id.send_sms_btn);
        editMsg = findViewById(R.id.enter_msg_edittext);
        openCamera = findViewById(R.id.open_camera);
        sendLocaiton = findViewById(R.id.send_location_btn);
        openGallery = findViewById(R.id.open_gallery_btn);
        scrollToBottom = findViewById(R.id.scroll_to_bottom_btn);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (extra.getVisibility() == View.INVISIBLE) {
                    extra.setVisibility(View.VISIBLE);
                } else {
                    extra.setVisibility(View.INVISIBLE);
                }
            }
        });

        scrollToBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                hideScrollToBottomBtn();
            }
        });


        editMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().isEmpty()) {
                    DrawableCompat.setTint(
                            DrawableCompat.wrap(sendSms.getDrawable()),
                            ContextCompat.getColor(getApplicationContext(), R.color.chatActivitysendIconButtonColorNotActive)
                    );
                    sendSms.setEnabled(false);
                } else {
                    DrawableCompat.setTint(
                            DrawableCompat.wrap(sendSms.getDrawable()),
                            ContextCompat.getColor(getApplicationContext(), R.color.chatActivitysendButtonIconColorActive)
                    );
                    sendSms.setEnabled(true);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().isEmpty()) {
                    DrawableCompat.setTint(
                            DrawableCompat.wrap(sendSms.getDrawable()),
                            ContextCompat.getColor(getApplicationContext(), R.color.chatActivitysendIconButtonColorNotActive)
                    );
                    sendSms.setEnabled(false);
                } else {
                    DrawableCompat.setTint(
                            DrawableCompat.wrap(sendSms.getDrawable()),
                            ContextCompat.getColor(getApplicationContext(), R.color.chatActivitysendButtonIconColorActive)
                    );
                    sendSms.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().isEmpty()) {
                    DrawableCompat.setTint(
                            DrawableCompat.wrap(sendSms.getDrawable()),
                            ContextCompat.getColor(getApplicationContext(), R.color.chatActivitysendIconButtonColorNotActive)
                    );
                    sendSms.setEnabled(false);
                } else {
                    DrawableCompat.setTint(
                            DrawableCompat.wrap(sendSms.getDrawable()),
                            ContextCompat.getColor(getApplicationContext(), R.color.chatActivitysendButtonIconColorActive)
                    );
                    sendSms.setEnabled(true);
                }
            }
        });

        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChatActivityLogs("OpenGallery Button Clicked.");
                pickImage();
                extra.setVisibility(View.GONE);
            }
        });

        sendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
//                mAdapter.notifyDataSetChanged();
                if (extra.getVisibility() == View.VISIBLE) {
                    extra.setVisibility(View.INVISIBLE);
                }
                String msg = editMsg.getText().toString().trim();
                if (!msg.isEmpty()) {
                    sendTextMessage();
                }
                updateAdapter();
                extra.setVisibility(View.GONE);
            }
        });

        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (extra.getVisibility() == View.VISIBLE) {
                    extra.setVisibility(View.INVISIBLE);
                }
                String msg = editMsg.getText().toString().trim();
                updateAdapter();
                extra.setVisibility(View.GONE);
            }
        });

        sendLocaiton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareLocation();
                extra.setVisibility(View.GONE);
            }
        });

    }


    public void initChatRoom(String userName) {
        //TODO: update MissedChatMessage, mark as read, update
        displayChatHeader(userName);
        displayMessageList();
    }


    private void displayMessageList() {
        if (mAdapter != null) {
            refreshHistory();
        } else {
            mMessages = new ArrayList<>();
            mAdapter = new ChatMessageAdapter(mMessages);
        }
        recyclerView.setAdapter(mAdapter);
        recyclerView.setVisibility(View.VISIBLE);
    }


    private void displayChatHeader(String username) {
        getSupportActionBar().setTitle(username);
    }



    private void redrawMessageList() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }


    private void refreshAndMarkAsRead() {
        refreshHistory();
        //chatRoom.markAsRead();
    }

    public void refreshHistory() {
//        if (mMessages == null || chatRoom == null) return;
//        mMessages.clear();
//        SygnalChatMessage[] messages = chatRoom.getHistory();
//        List<MessageModel> messagesList = MessageModel.toChatMessage(messages);
//        mMessages.addAll(messagesList);
//        mAdapter.notifyDataSetChanged();
    }


    private void sendTextMessage() {
        sendTextMessage(editMsg.getText().toString(),MessageType.TEXT,username+"@"+domainName);
        editMsg.setText("");
        refreshAndMarkAsRead();
    }

    private void sendTextMessage(String messageToSend,MessageType type, String entityBareId) {
        String msgType;
        switch (type){
            case TEXT:
                msgType = "text";
                break;
            case IMAGE:
                msgType = "image";
                break;
            case LOCATION:
                msgType = "location";
                break;
            default:
                msgType = "text";
        }

        if (XmppCore.getInstance().sendMessage(messageToSend,entityBareId)){
            MessageModel data = new MessageModel("send",messageToSend);
            insertMessage(new MessageModel("send",messageToSend));
        }
        else{
            Toast.makeText(getApplicationContext(),"Error sending message",Toast.LENGTH_SHORT);
        }
//        String msgType;
//        switch (type){
//            case TEXT:
//                msgType = "text";
//                break;
//            case IMAGE:
//                msgType = "image";
//                break;
//            case LOCATION:
//                msgType = "location";
//                break;
//            default:
//                msgType = "text";
//        }
//        SygnalCore lc = SygnalManager.getLcIfManagerNotDestroyedOrNull();
//        boolean isNetworkReachable = lc != null && lc.isNetworkReachable();
//        SygnalAddress lAddress = null;
//
//        if (chatRoom != null && messageToSend != null && messageToSend.length() > 0 && isNetworkReachable) {
//            SygnalChatMessage message = chatRoom.createSygnalChatMessage(messageToSend);
//            chatRoom.sendChatMessage(message);
//            lAddress = chatRoom.getPeerAddress();
//
//            if (MainActivity.isInstanciated()) {
//                ChatListFragment.getInstance().refresh();
//            }
//
//            message.setListener(SygnalManager.getInstance());
//            insertMessage(MessageModel.toChatMessage(message, msgType, false));
//
//            return message;
//
//        } else if (!isNetworkReachable && MainActivity.isInstanciated()) {
//            Toast.makeText(this, getString(R.string.error_network_unreachable), Toast.LENGTH_SHORT).show();
//            return null;
//        }
//        return null;
    }



    public void resendMessage(MessageModel message) {
//        message.getParentMessage().reSend();
//        refreshAndMarkAsRead();
    }


    public Bitmap getLocationImage(String coordinates) {
//        LocationManagerClass lc = new LocationManagerClass(this);
//        Bitmap image = lc.getImageFromLocation(coordinates);
//        return image;
        return null;
    }





    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        recyclerView.setAdapter(null);
        if (mMessages != null) {
            mMessages.clear();
        }
//        if (defaultBitmap != null) {
//            defaultBitmap.recycle();
//            defaultBitmap = null;
//        }
        super.onDestroy();
    }

    @SuppressLint("UseSparseArrays")
    @Override
    public void onResume() {
        super.onResume();
        isVisibile = true;
    }

    @Override
    protected void onStop() {
        isVisibile = false;
        super.onStop();
    }

    public static boolean isVisible(){
        return isVisibile;
    }


    // TODO:  Method to send Image
    private void pickImage() {
        showChatActivityLogs("Entering pickImage()");
        List<Intent> cameraIntents = new ArrayList<Intent>();
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory(), getString(R.string.temp_photo_name_with_date).replace("%s", System.currentTimeMillis() + ".jpeg"));
        imageToUploadUri = Uri.fromFile(file);
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageToUploadUri);
        cameraIntents.add(captureIntent);

        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_PICK);

        Intent fileIntent = new Intent();
        fileIntent.setType("*/*");
        fileIntent.setAction(Intent.ACTION_GET_CONTENT);
        cameraIntents.add(fileIntent);

        Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.image_picker_title));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
        startActivityForResult(chooserIntent, ADD_PHOTO);
    }

//    private void sendImageMessage(String path, int imageSize) {
//        if(path.contains("file://")) {
//            path = path.split("file:///", 2)[1];
//        } else if (path.contains("content://")){
//            path = SygnalUtils.getFilePath(this.getApplicationContext(), Uri.parse(path));
//        }
//        if(path != null && path.contains("%20")) {
//            path = path.replace("%20", "-");
//        }
//        SygnalCore lc = SygnalManager.getLcIfManagerNotDestroyedOrNull();
//        boolean isNetworkReachable = lc != null && lc.isNetworkReachable();
////        if(chatRoom == null) {
////            String address = searchContactField.getText().toString();
////            if (address != null && !address.equals("")) {
////                initChatRoom(address);
////            }
////        }
//        if (chatRoom != null && path != null && path.length() > 0 && isNetworkReachable) {
//            try {
//                Bitmap bm = BitmapFactory.decodeFile(path);
//
//                if (bm == null && path.contains("NONE")) {
//                    Uri uri = Uri.parse(path);
//                    InputStream is = null;
//                    if (uri.getAuthority() != null) {
//                        try {
//                            is = this.getContentResolver().openInputStream(uri);
//                            bm = BitmapFactory.decodeStream(is);
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        } finally {
//                            try {
//                                if (is != null) {
//                                    is.close();
//                                }
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }
//
//                if (bm != null) {
//                    FileUploadPrepareTask task = new FileUploadPrepareTask(this, path, imageSize);
//                    task.execute(bm);
//                } else {
//                    com.rapidev.sygnal.mediastream.Log.e("Error, bitmap factory can't read " + path);
//                }
//            } catch (RuntimeException e) {
//                com.rapidev.sygnal.mediastream.Log.e("Error, not enough memory to create the bitmap");
//            }
//            fileSharedUri = null;
//        } else if (!isNetworkReachable && MainActivity.isInstanciated()) {
//            // Show Toast
//        }
//    }

    //used everywhere
    //using string message, getImage
//    private void sendImageMessageOnServer(String path, int imageSize) {
//        showChatActivityLogs("Entering sendImageMessageOnServer()");
//        if(path.contains("file://")) {
//            path = path.split("file:///", 2)[1];
//        } else if (path.contains("content://")){
//            path = SygnalUtils.getFilePath(this.getApplicationContext(), Uri.parse(path));
//        }
//        if(path != null && path.contains("%20")) {
//            path = path.replace("%20", "-");
//        }
//        SygnalCore lc = SygnalManager.getLcIfManagerNotDestroyedOrNull();
//        boolean isNetworkReachable = lc != null && lc.isNetworkReachable();
////        if(newChatConversation && chatRoom == null) {
////            String address = searchContactField.getText().toString();
////            if (address != null && !address.equals("")) {
////                initChatRoom(address);
////            }
////        }
//        if (chatRoom != null && path != null && path.length() > 0 && isNetworkReachable) {
//            try {
//                Bitmap bm = BitmapFactory.decodeFile(path);
//                if (bm == null && path.contains("NONE")) {
//                    Uri uri = Uri.parse(path);
//                    InputStream is = null;
//                    if (uri.getAuthority() != null) {
//                        try {
//                            is = this.getContentResolver().openInputStream(uri);
//                            bm = BitmapFactory.decodeStream(is);
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        } finally {
//                            try {
//                                if (is != null) {
//                                    is.close();
//                                }
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }
//
//                if (bm != null) {
//                    String pathTOMessage = "#image#memory*-*"+path;
//                    SygnalChatMessage message = sendTextMessage(pathTOMessage,MessageType.IMAGE);
//
//                    new getImage().execute(new ImageUploadModel(bm,message,path));
//                    return;
//                } else {
//                    com.rapidev.sygnal.mediastream.Log.e("Error, bitmap factory can't read " + path);
//                }
//            } catch (RuntimeException e) {
//                com.rapidev.sygnal.mediastream.Log.e("Error, not enough memory to create the bitmap");
//            }
//            fileSharedUri = null;
//        } else if (!isNetworkReachable && MainActivity.isInstanciated()) {
//            Toast.makeText(this, getString(R.string.error_network_unreachable), Toast.LENGTH_LONG).show();
//        }
//    }
    //to upload image used by asyncTasks
//    private String uploadImage(Bitmap image, final SygnalChatMessage message, String filePath) {
//        showChatActivityLogs("Uploading Image with path: " + filePath);
//        android.util.Log.i("imageUploadTest", "uploadingImage");
//        MediaType MEDIA_TYPE_PNG = MediaType.parse("image/jpeg");
//
//        OkHttpClient client = new OkHttpClient();
//        try {
//			/*
//			File f = new File(getActivity().getCacheDir(), "image.jpeg");
//			f.createNewFile();
//			Bitmap bitmap = image;
//
//
//
//			//bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			bitmap.compress(Bitmap.CompressFormat.JPEG, 100 , bos);
//			image.recycle();
//
//			byte[] bitmapdata = bos.toByteArray();
//			FileOutputStream fos = new FileOutputStream(f);
//			fos.write(bitmapdata);
//
//			//fos.write(bitmap);
//			ExifInterface a = new ExifInterface(image.getU)
//			if(f.exists())
//			{
//				System.out.print("working");
//			}
//			*/
//            File f = new File(filePath);
//            Compressor compressedImageBitmapdata = new Compressor(this);
//            compressedImageBitmapdata.setQuality(20);
//            File compressedImageBitmap = compressedImageBitmapdata.compressToFile(f);
//            android.util.Log.i("imageUploadTest", "tille here");
//            //compressedImageBitmap = (f);
//
//            //File compressedImageBitmap = f;
//            //compressedImageBitmap.compress(Bitmap.CompressFormat.PNG, 20 /*ignored for PNG*/, bos);
//
//
//            //FileOutputStream fos = new FileOutputStream(f);
//            //fos.write(bitmapdata);
//
//
//			/*
//			ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
//			image.copyPixelsToBuffer(buffer);
//			byte[] array = buffer.array(); //Get the underlying array containing the data.
//			*/
//            showChatActivityLogs("Sending file url: " + compressedImageBitmap.getPath());
//            RequestBody requestBody = new MultipartBody.Builder()
//                    .setType(MultipartBody.FORM)
//                    .addFormDataPart("image", "image.jpeg", RequestBody.create(MEDIA_TYPE_PNG, compressedImageBitmap))
//                    .build();
//			/*
//			Request request = new Request.Builder()
//					.header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
//					.url("http://159.8.160.5:10010/imageUpload")
//					.post(requestBody)
//					.build();
//			*/
//            Request request = null;
//            try {
//                String domain = Constants.domain;
//                if(domain !=null)
//                {
//
//                    request = new Request.Builder()
//                            .url("http://" + domain + ":10010/imageUpload")
//                            .post(requestBody)
//                            .build();
//                    android.util.Log.i("imageUploadTest", "domain wasn't null");
//                }
//            }catch (Exception i) {
//
//                request = new Request.Builder()
//                        .url("http://119.81.205.4:10010/imageUpload")
//                        .post(requestBody)
//                        .build();
//                android.util.Log.i("imageUploadTest", "error 1");
//            }
//
//            showChatActivityLogs("Request body is: " + requestBody.toString());
//
//            Response response = null;
//            if (request != null) {
//                showChatActivityLogs("Requesting URL: " + request.url());
//                response = client.newCall(request).execute();
//                android.util.Log.i("imageUploadTest", "request wasn't  null");
//            } else {
//                android.util.Log.i("imageUploadTest", "request null");
//            }
//
//            showChatActivityLogs("Response is: " + response.body().toString());
//
//
//            if (response != null && !response.isSuccessful()) {
//                showChatActivityLogs("Unsuccessful");
//                throw new IOException("Unexpected code " + response);
//            }
//
//            android.util.Log.i("imageUploadTest", "past it");
//            String jsonData = null;
//            if (response.body() != null) {
//                jsonData = response.body().string();
//            }
//            JSONObject Jobject = new JSONObject(jsonData);
//            final String address = Jobject.getString("url");
//            this.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    sendTextMessage("#image#server*-*"+address,MessageType.IMAGE);
//                    chatRoom.deleteMessage(message);
//                    android.util.Log.i("imageUploadTest", "sending path " + address);
//                    refreshAndMarkAsRead();
//                }
//            });
//
//            //System.out.println(response.body().string());
//            if ( f.exists()) {
//                if (f.delete()) {
//                    System.out.println("file Deleted :" + f.getPath());
//                } else {
//                    System.out.println("file not Deleted :" + f.getPath());
//                }
//            }
//        }catch (Exception i)
//        {
//            System.out.print(i.getMessage());
//            android.util.Log.i("imageUploadTest", "error 0");
//            android.util.Log.i("imageUploadTest", i.toString());
//        }
 //       return "";

 //   }
    //used in sendImageMessageOnServer
//    class getImage extends AsyncTask<ImageUploadModel, Void, Void> {
//        private Exception exception;
//
//        protected Void doInBackground(ImageUploadModel... image) {
//            ChatActivity.getInstance().showChatActivityLogs("Background Task Entered...");
//            try {
//                uploadImage(image[0].image,image[0].message,image[0].imagePath);
//                return null;
//            } catch (Exception e) {
//                this.exception = e;
//                return null;
//            } finally {
//            }
//        }
//
//        protected void onPostExecute(Void feed) {
//            // TODO: check this.exception
//            // TODO: do something with the feed
//        }
//    }
    //used in onBindViewHolder
//    class uploadImageUsingPath extends AsyncTask<ImageUploadModel, Void, Void> {

//        private Exception exception;
//
//        protected Void doInBackground(ImageUploadModel... image) {
//            try {
//                Bitmap bitmap1 = BitmapFactory.decodeFile(image[0].imagePath);
//                ImageUploadModel imageUpload = new ImageUploadModel(bitmap1,image[0].message,image[0].imagePath);
//                uploadImage(imageUpload.image,imageUpload.message,imageUpload.imagePath);
//                return null;
//            } catch (Exception e) {
//                this.exception = e;
//
//                return null;
//            } finally {
//
//            }
//
//        }
//
//        protected void onPostExecute(Void feed) {
//            // TODO: check this.exception
//            // TODO: do something with the feed
//        }
//    }
//    private void sendFileSharingMessage(String path, int size ) {
//        if (path.contains("file://")) {
//            path = path.split("file:///", 2)[1];
//        } else if (path.contains("com.android.contacts/contacts/")) {
//            path = getCVSPathFromLookupUri(path).toString();
//        } else if (path.contains("vcard") || path.contains("vcf")) {
//            if (SygnalUtils.createCvsFromString(MainActivity.getInstance().getCVSPathFromOtherUri(path)) != null) {
//                path = (SygnalUtils.createCvsFromString(MainActivity.getInstance().getCVSPathFromOtherUri(path))).toString();
//            }
//        } else if (path.contains("content://")){
//            path = SygnalUtils.getFilePath(this.getApplicationContext(), Uri.parse(path));
//        }
//        if(path != null && path.contains("%20")) {
//            path = path.replace("%20", "-");
//        }
//        SygnalCore lc = SygnalManager.getLcIfManagerNotDestroyedOrNull();
//        boolean isNetworkReachable = lc != null && lc.isNetworkReachable();
////        if (newChatConversation && chatRoom == null) {
////            String address = searchContactField.getText().toString();
////            if (address != null && !address.equals("")) {
////                initChatRoom(address);
////            }
////        }
//
//        if (chatRoom != null && path != null && path.length() > 0 && isNetworkReachable) {
//            try {
//                File fileShared = new File(path);
//                if (fileShared != null) {
//                    FileSharingUploadPrepareTask task = new FileSharingUploadPrepareTask(this, path, size);
//                    task.execute(fileShared);
//                } else {
//                    com.rapidev.sygnal.mediastream.Log.e("Error, fileShared can't be read " + path);
//                }
//            } catch (RuntimeException e) {
//                com.rapidev.sygnal.mediastream.Log.e("Error, not enough memory to create the fileShared");
//            }
//            fileSharedUri = null;
//        } else if (!isNetworkReachable && MainActivity.isInstanciated()) {
//            // Show Toast
//        }
//    }

    //used by sendImageMessage
//    class FileUploadPrepareTask extends AsyncTask<Bitmap, Void, byte[]> {
//        private String path;
//        private ProgressDialog progressDialog;
//
//        public FileUploadPrepareTask(Context context, String fileToUploadPath, int size) {
//            path = fileToUploadPath;
//
//            progressDialog = new ProgressDialog(context);
//            progressDialog.setIndeterminate(true);
//            progressDialog.setMessage(getString(R.string.processing_image));
//            progressDialog.show();
//        }
//
//        @Override
//        protected byte[] doInBackground(Bitmap... params) {
//            Bitmap bm = params[0];
//            Bitmap bm_tmp = null;
//
//            if (bm.getWidth() >= bm.getHeight() && bm.getWidth() > SIZE_MAX) {
//                bm_tmp = Bitmap.createScaledBitmap(bm, SIZE_MAX, (SIZE_MAX * bm.getHeight()) / bm.getWidth(), false);
//
//            } else if (bm.getHeight() >= bm.getWidth() && bm.getHeight() > SIZE_MAX) {
//                bm_tmp = Bitmap.createScaledBitmap(bm, (SIZE_MAX * bm.getWidth()) / bm.getHeight(), SIZE_MAX, false);
//            }
//
//            if (bm_tmp != null) {
//                bm.recycle();
//                bm = bm_tmp;
//            }
//
//            // Rotate the bitmap if possible/needed, using EXIF data
//            try {
//                if (path != null) {
//                    ExifInterface exif = new ExifInterface(path);
//                    int pictureOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
//                    Matrix matrix = new Matrix();
//                    if (pictureOrientation == 6) {
//                        matrix.postRotate(90);
//                    } else if (pictureOrientation == 3) {
//                        matrix.postRotate(180);
//                    } else if (pictureOrientation == 8) {
//                        matrix.postRotate(270);
//                    }
//                    bm_tmp = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
//                }
//            } catch (Exception e) {
//                com.rapidev.sygnal.mediastream.Log.e(e);
//            }
//
//            if (bm_tmp != null) {
//                if (bm_tmp != bm) {
//                    bm.recycle();
//                    bm = bm_tmp;
//                } else {
//                    bm_tmp = null;
//                }
//            }
//
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            String extension = SygnalUtils.getExtensionFromFileName(path);
//            if (extension != null && extension.toLowerCase(Locale.getDefault()).equals("png")) {
//                bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
//            } else {
//                bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//            }
//
//            if (bm_tmp != null) {
//                bm_tmp.recycle();
//                bm_tmp = null;
//            }
//            bm.recycle();
//            bm = null;
//
//            return stream.toByteArray();
//        }
//
//        @Override
//        protected void onPostExecute(byte[] result) {
//            if (progressDialog != null && progressDialog.isShowing()) {
//                progressDialog.dismiss();
//            }
//            String fileName = path.substring(path.lastIndexOf("/") + 1);
//            String extension = SygnalUtils.getExtensionFromFileName(fileName);
//            SygnalContent content = SygnalCoreFactory.instance().createSygnalContent("image", extension, result, null);
//            content.setName(fileName);
//            SygnalChatMessage message = chatRoom.createFileTransferMessage(content);
//            message.setListener(SygnalManager.getInstance());
//            message.setAppData(path);
//
//            SygnalManager.getInstance().setUploadPendingFileMessage(message);
//            SygnalManager.getInstance().setUploadingImage(result);
//
//            chatRoom.sendChatMessage(message);
//            insertMessage(MessageModel.toChatMessage(message, "image", true));
//        }
//    }
//    //used by sendFileSharingMessage
//    class FileSharingUploadPrepareTask extends AsyncTask<File, Void, byte[]> {
//        private String path;
//        private ProgressDialog progressDialog;
//
//        public FileSharingUploadPrepareTask(Context context, String fileToUploadPath, int size) {
//            path = fileToUploadPath;
//
//            progressDialog = new ProgressDialog(context);
//            progressDialog.setIndeterminate(true);
//            progressDialog.setMessage(getString(R.string.processing_image));
//            progressDialog.show();
//        }
//
//        @Override
//        protected byte[] doInBackground(File... params) {
//            File file = params[0];
//
//            byte[] byteArray = new byte[(int) file.length()];
//            FileInputStream fis = null;
//            try {
//                fis = new FileInputStream(file);
//                fis.read(byteArray); //read file into bytes[]
//                fis.close();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return byteArray;
//        }
//
//        @Override
//        protected void onPostExecute(byte[] result) {
//            if (progressDialog != null && progressDialog.isShowing()) {
//                progressDialog.dismiss();
//            }
//
//            String fileName = path.substring(path.lastIndexOf("/") + 1);
//            String extension = SygnalUtils.getExtensionFromFileName(fileName);
//            SygnalContent content = SygnalCoreFactory.instance().createSygnalContent("file", extension, result, null);
//            content.setName(fileName);
//
//            SygnalChatMessage message = chatRoom.createFileTransferMessage(content);
//            message.setListener(SygnalManager.getInstance());
//            message.setAppData(path);
//
//            SygnalManager.getInstance().setUploadPendingFileMessage(message);
//            SygnalManager.getInstance().setUploadingImage(result);
//
//            chatRoom.sendChatMessage(message);
//            insertMessage(MessageModel.toChatMessage(message, "image", true));
//        }
//    }

    public File loadImageInMemory(String url) {
        final String appDirectoryName = "Sygnal";
        String[] filename = url.split("/file/");
        if(filename.length > 1) {
            String filenamedata = filename[1];
            File imageRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES+"/Sygnal/");
            //String path = Environment.getExternalStorageDirectory().toString();
            File dir = new File(imageRoot, filenamedata);
            if (dir.exists()) {
                return dir;
            }
        }
        return null;
    }
    public boolean saveImageInBitmap(String url,Bitmap image){

        String[] filename = url.split("/file/");
        if(filename.length > 1)
        {
            String filenamedata = filename[1];

            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES+"/Sygnal/"); //Creates app specific folder
            if(!path.exists())
                path.mkdirs();


            File imageFile = new File(path, filenamedata); // Imagename.png

            try{
                if(path.canWrite())
                {
                    if(!imageFile.exists())
                        imageFile.createNewFile();
                }

                FileOutputStream out = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.PNG, 100, out); // Compress Image
                out.flush();
                out.close();

                MediaScannerConnection.scanFile(this,new String[] { imageFile.getAbsolutePath() }, null,new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {

                    }
                });
            } catch(Exception e) {
                System.out.print(e.getMessage());
            }



			/*
			try {
				if(!file.exists())
					file.
				FileOutputStream out = new FileOutputStream(imagePath);

				image.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
				// PNG is a lossless format, the compression factor (100) is ignored

			} catch (IOException e) {
				e.printStackTrace();
			}
			*/
            return true;

        }
        else
        {
            return false;
        }



    }

    public void ShareLocation(){
//        LocationManagerClass Loc = new LocationManagerClass(this);
//        Location location = Loc.getLocation();
//        // Making Protocol to send the currentLocation
//        if(location == null) {
//            Toast.makeText(this,"Activate Location Setting",Toast.LENGTH_SHORT).show();
//            return;
//        }
//        String Protocol = "data*latlon#"+location.getLatitude()+","+location.getLongitude();
//        sendTextMessage(Protocol,MessageType.LOCATION);
    }

    public Uri getCVSPathFromLookupUri(Uri contentUri) {
        String content = contentUri.getPath();
        return getCVSPathFromLookupUri(Uri.parse(content));
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        if (cursor != null && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String result = cursor.getString(column_index);
            cursor.close();
            return result;
        }
        return null;
    }


    //    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        showChatActivityLogs("Entering onActivityResult()");
//        if (data != null) {
//            if (requestCode == ADD_PHOTO && resultCode == Activity.RESULT_OK) {
//                String fileToUploadPath = null;
//                if (data != null && data.getData() != null) {
//                    if (data.getData().toString().contains("com.android.contacts/contacts/")) {
//                        if (getCVSPathFromLookupUri(data.getData()) != null)
//                            fileToUploadPath = getCVSPathFromLookupUri(data.getData()).toString();
//                        else {
//                            Toast.makeText(this,"Something wrong happened", Toast.LENGTH_LONG);
//                            return;
//                        }
//                    } else {
//                        fileToUploadPath = getRealPathFromURI(data.getData());
//                    }
//                    if (fileToUploadPath == null) {
//                        fileToUploadPath = data.getData().toString();
//                    }
//                } else if (imageToUploadUri != null) {
//                    fileToUploadPath = imageToUploadUri.getPath();
//                }
//                if (SygnalUtils.isExtensionImage(fileToUploadPath)) {
//                    if (fileToUploadPath != null) {
//                        sendImageMessageOnServer(fileToUploadPath, 0);
//                    }
//                } else if (fileToUploadPath != null) {
//                    sendFileSharingMessage(fileToUploadPath, 0);
//                }
//            } else {
//                super.onActivityResult(requestCode, resultCode, data);
//            }
//        } else {
//            if (SygnalUtils.isExtensionImage(imageToUploadUri.getPath()))
//                if (imageToUploadUri.getPath() != null) {
//                    showChatActivityLogs("Sending Image on sendImageMessageOnServer()");
//                    showChatActivityLogs("Image path is: " + imageToUploadUri.getPath());
//                    sendImageMessageOnServer(imageToUploadUri.getPath(), 0);
//                }
//        }
//    }



//    public boolean uploadImage(MessageModel message) {
//        Log.i("imageUploadTest", "uploadImage: uploadingImage");
//        String path = message.getParentMessage().getText().replace("#image#memory*-*", "");
//        boolean isNetworkReachable = SygnalManager.getLc() != null && SygnalManager.getLc().isNetworkReachable();
//        if (chatRoom != null && path != null && path.length() > 0 && isNetworkReachable) {
//            try {
//                Bitmap bm = BitmapFactory.decodeFile(path);
//                if (bm == null && path.contains("NONE")) {
//                    Uri uri = Uri.parse(path);
//                    InputStream is = null;
//                    if (uri.getAuthority() != null) {
//                        try {
//                            is = this.getContentResolver().openInputStream(uri);
//                            bm = BitmapFactory.decodeStream(is);
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        } finally {
//                            try {
//                                if (is != null) {
//                                    is.close();
//                                }
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }
//                if (bm != null) {
//                    new getImage().execute(new ImageUploadModel(bm, message.getParentMessage(), path));
//                    return true;
//                } else {
//                    com.rapidev.sygnal.mediastream.Log.e("Error, bitmap factory can't read " + path);
//                    return false;
//                }
//            } catch (RuntimeException e) {
//                com.rapidev.sygnal.mediastream.Log.e("Error, not enough memory to create the bitmap");
//                return false;
//            }
//        } else {
//            return false;
//        }
//    }
//
//
//    public void cancelUpload() {
//        Log.i("imageUploadTest", "uploadImage: cancellingUpload");
//    }
}
