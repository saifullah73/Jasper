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
import android.location.Location;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.content.*;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jasper.Adapters.ChatMessageAdapter;
import com.example.jasper.AppBackend.Interfaces.CustomFileTransferListener;
import com.example.jasper.AppBackend.Interfaces.FetchCallbackListener;
import com.example.jasper.AppBackend.Presistance.DBHelper;
import com.example.jasper.AppBackend.Xmpp.XmppCore;
import com.example.jasper.Constants;
import com.example.jasper.LocationManagerClass;
import com.example.jasper.Models.MessageModel;
import com.example.jasper.R;
import com.example.jasper.Utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import at.markushi.ui.CircleButton;

import static android.view.View.GONE;

public class ChatActivity extends AppCompatActivity implements ChatMessageAdapter.ChangeToolbarMenuItemsListener {
    private static final String TAG = "ChatActivity";
    private static final int ADD_PHOTO = 1337;
    private static final int ADD_DOC = 1336;

    public enum MessageType {LOCATION, IMAGE, FILE, TEXT}

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
    private boolean isActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Log.i("NewImp", "here");
        initViews();
        hideKeyboard();
        initRecyclerView();
        instance = this;
        Intent i = getIntent();
        if (i != null) {
            username = i.getStringExtra("username");
            initChatRoom(username);
        } else {
            username = "User";
            initChatRoom(username);
        }
        domainName = getString(R.string.domainName);
        mAdapter.setInterface(this);
        DBHelper.getInstance(getApplicationContext()).getHistory(Constants.currentUser, username, "18-10-2019", new FetchCallbackListener() {
            @Override
            public void onSuccess(List<MessageModel> list) {
                Log.i("DBTest", "testing for message fetch works");
                for (MessageModel m : list) {
                    insertMessage(m);
                }
            }
        });
    }

    @Override
    public void ChangeToolBarMenuItems(int selectedMsgs, boolean onlyTextMsgSelected) {
        if (menu != null) {
            changeToolbarIcons(selectedMsgs, onlyTextMsgSelected);
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

    public static boolean isInstanceNull() {
        return instance == null;
    }


    private void updateAdapter() {
        if (mAdapter.getItemCount() != 0) {
            recyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            hideScrollToBottomBtn();
            mAdapter.notifyDataSetChanged();
        }
    }


    public void insertMessage(MessageModel message) {
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


    public void showChatActivityLogs(String msg) {
        Log.d("ChatActivityLogs", "showAdapterLogs: " + msg);
    }

    public void hideScrollToBottomBtn() {
        scrollToBottom.setVisibility(View.INVISIBLE);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        insert = findViewById(R.id.insert_extra_btn);
        sendDocument = findViewById(R.id.send_file_btn);
        extra = findViewById(R.id.extra_layout);
        sendSms = findViewById(R.id.send_sms_btn);
        editMsg = findViewById(R.id.enter_msg_edittext);
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

        sendDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, ADD_DOC);
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
            loadHistory();
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
        loadHistory();
        //chatRoom.markAsRead();
    }

    public void loadHistory() {
    }

    public String getTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }


    private void sendTextMessage() {
        sendTextMessage(editMsg.getText().toString(), MessageType.TEXT, username + "@" + domainName);
        editMsg.setText("");
        refreshAndMarkAsRead();
    }


    private void sendTextMessage(String messageToSend, MessageType type, String entityBareId, String fileStatus) {
        String msgType;
        switch (type) {
            case TEXT:
                msgType = "text";
                break;
            case IMAGE:
                msgType = "image";
                break;
            case LOCATION:
                msgType = "location";
                break;
            case FILE:
                msgType = "file";
                break;
            default:
                msgType = "text";
        }

        if (XmppCore.getInstance().sendMessage(messageToSend, entityBareId)) {
            MessageModel data = new MessageModel("sent", messageToSend, getTimestamp(), msgType, fileStatus);
            DBHelper.getInstance(getApplicationContext()).putMessageInDB(username, Constants.currentUser, getTimestamp(), messageToSend);
            insertMessage(data);
        } else {
            Toast.makeText(getApplicationContext(), "Error sending message", Toast.LENGTH_SHORT);
        }
    }

    private void sendTextMessage(String messageToSend, MessageType type, String entityBareId) {
        String msgType;
        switch (type) {
            case TEXT:
                msgType = "text";
                break;
            case IMAGE:
                msgType = "image";
                break;
            case LOCATION:
                msgType = "location";
                break;
            case FILE:
                msgType = "file";
                break;
            default:
                msgType = "text";
        }

        if (XmppCore.getInstance().sendMessage(messageToSend, entityBareId)) {
            MessageModel data = new MessageModel("sent", messageToSend, getTimestamp(), msgType);
            DBHelper.getInstance(getApplicationContext()).putMessageInDB(username, Constants.currentUser, getTimestamp(), messageToSend);
            insertMessage(data);
        } else {
            Toast.makeText(getApplicationContext(), "Error sending message", Toast.LENGTH_SHORT);
        }
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

    public static boolean isVisible() {
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
        Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.image_picker_title));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
        startActivityForResult(chooserIntent, ADD_PHOTO);
    }

    public File loadImageInMemory(String url) {
        return null;
    }

    public boolean saveImageInBitmap(String url, Bitmap image) {

        String[] filename = url.split("/file/");
        if (filename.length > 1) {
            String filenamedata = filename[1];

            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Sygnal/"); //Creates app specific folder
            if (!path.exists())
                path.mkdirs();


            File imageFile = new File(path, filenamedata); // Imagename.png

            try {
                if (path.canWrite()) {
                    if (!imageFile.exists())
                        imageFile.createNewFile();
                }

                FileOutputStream out = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.PNG, 100, out); // Compress Image
                out.flush();
                out.close();

                MediaScannerConnection.scanFile(this, new String[]{imageFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {

                    }
                });
            } catch (Exception e) {
                System.out.print(e.getMessage());
            }
            return true;

        } else {
            return false;
        }


    }

    public void ShareLocation() {
        LocationManagerClass Loc = new LocationManagerClass(this);
        Location location = Loc.getLocation();
        // Making Protocol to send the currentLocation
        if (location == null) {
            Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show();
            return;
        }
        String Protocol = "##location##" + location.getLatitude() + "," + location.getLongitude();
        sendTextMessage(Protocol, MessageType.LOCATION, username + "@" + Constants.domain);
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

    private CustomFileTransferListener ff = new CustomFileTransferListener() {
        @Override
        public void onFailure() {

        }

        @Override
        public void onSuccess() {

        }

        @Override
        public int getProgress() {
            return 0;
        }
    };

    public void openFileViewer(String path) {
        try {
            MimeTypeMap myMime = MimeTypeMap.getSingleton();
            String mimeType = myMime.getMimeTypeFromExtension(fileExt(path).substring(1));
            File file = new File(path);
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri apkURI = FileProvider.getUriForFile(getApplication(), getApplication().getApplicationContext().getPackageName() + ".provider", file);
            install.setDataAndType(apkURI, mimeType);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(install);
        } catch (Exception e) {
            Log.i("XmppCore", "error occured " + e.toString());
        }
    }

    private String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        showChatActivityLogs("Entering onActivityResult()");
        if (data != null) {
            if (requestCode == ADD_PHOTO && resultCode == Activity.RESULT_OK) {
                String fileToUploadPath = null;
                if (data != null && data.getData() != null) {
                    showChatActivityLogs("data not null");
                    fileToUploadPath = getRealPathFromURI(data.getData());
                    showChatActivityLogs("filepath2 " + fileToUploadPath);
                    sendFile(fileToUploadPath);
                }
            } else if (resultCode == Activity.RESULT_OK && requestCode == ADD_DOC) {
                if (data != null && data.getData() != null) {
                    String fileToUploadPath;
                    Log.i("XmppCoref1", "data not null");
                    fileToUploadPath = getRealPathFromURI(data.getData());
                    if (fileToUploadPath == null) {
                        fileToUploadPath = Utils.getPath(getApplication(), data.getData());
                        Log.i("XmppCoref3", fileToUploadPath);
                    }
                    Log.i("XmppCoref2", fileToUploadPath);
                    sendFile(fileToUploadPath);
                } else {
                    Log.i("XmppCoref4", "data was null");
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public String getUsername() {
        return username;
    }


    private void sendFile(String fileToUploadPath) {
        try {
            String res = Constants.map.get(username);
            if (res == null) {
                res = "mobile";
            }
            XmppCore.getInstance().sendFile(username, fileToUploadPath, "file", res, ff);
            if (Utils.isImage(fileToUploadPath)) {
                Log.i("FileUpload", "sendFile(): isImage = true");
                sendTextMessage("##image##" + fileToUploadPath, MessageType.IMAGE, username + "@" + domainName, "uploaded");
            } else {
                Log.i("FileUpload", "sendFile(): isImage = false");
                sendTextMessage("##file##" + fileToUploadPath, MessageType.FILE, username + "@" + domainName, "uploaded");
            }
        } catch (Exception e) {
            Toast.makeText(getApplication(), "Error sending file", Toast.LENGTH_SHORT).show();
            Log.e("XmppCore", "File Sharing Error" + e.toString());
        }
    }


}