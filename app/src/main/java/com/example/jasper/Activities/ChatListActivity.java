package com.example.jasper.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.jasper.Adapters.ChatListAdapter;
import com.example.jasper.Interfaces.ChatListClickListener;
import com.example.jasper.Models.Contact;
import com.example.jasper.R;
import com.example.jasper.xmpp.XMPPConnection;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;




import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ChatListActivity extends AppCompatActivity implements ChatListClickListener{


    private static final String TAG = "ChatListFragment";
    private View view;
    private AppCompatButton button;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChatListAdapter chatListAdapter;
    private static ChatListActivity mInstance;
    private TextView noChatsToShowView;
    private FloatingActionButton addNewChat, deletechat, selectDeselect;
    private ConstraintLayout.LayoutParams params;
    private boolean isDeleteMode;
    private List<Contact> chatroomList = new ArrayList<>();


    public static ChatListActivity getInstance(){
        if(mInstance == null){
            mInstance = new ChatListActivity();
        }
        return mInstance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatlistactivity);
        initComponents();



    }



    private void initComponents() {
        recyclerView = findViewById(R.id.chatList_recylerView);
        registerForContextMenu(recyclerView);
        addNewChat = findViewById(R.id.add_new_chat);
        deletechat = findViewById(R.id.delete_chatroom);
        swipeRefreshLayout = findViewById(R.id.chat_list_swipe_refresh);
        selectDeselect = findViewById(R.id.chatlist_select_deselect);
        noChatsToShowView = findViewById(R.id.no_chats_to_show_txt);
        params = (ConstraintLayout.LayoutParams) addNewChat.getLayoutParams();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        chatListAdapter = new ChatListAdapter(chatroomList, this);
        recyclerView.setAdapter(chatListAdapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateChatRoomsFromServer();
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && addNewChat.getVisibility() == View.VISIBLE) {
                    addNewChat.hide();
                } else if (dy < 0 && addNewChat.getVisibility() != View.VISIBLE) {
                    addNewChat.show();
                }
            }
        });
        deletechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                removeChatsConversation(chatListAdapter.getSelectedItems());
                getOutOfDeleteMode();
            }
        });

//        addNewChat.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //startActivity(new Intent(getContext(), ChatActivity.class));
//                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
//                intent.putExtra("intentPurpose", "addnewchat");
//                startActivity(intent);
//            }
//        });


//        selectDeselect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (chatListAdapter.areAllItemSelected()) {
//                    selectDeselect.setImageDrawable(getResources().getDrawable(R.drawable.select_all, getContext().getTheme()));
//                    chatListAdapter.deselectAll();
//                } else {
//                    selectDeselect.setImageDrawable(getResources().getDrawable(R.drawable.deselect_all, getContext().getTheme()));
//                    chatListAdapter.selectAll();
//                }
//            }
//        });
//        refresh();
//    }
    }

        private void updateChatRoomsFromServer() {
            refresh();
            swipeRefreshLayout.setRefreshing(false);
        }

        public List<Contact> getChatList () {
            ArrayList<Contact> chatList = new ArrayList<Contact>();
            if (XMPPConnection.mConnection != null) {
                Roster roster = Roster.getInstanceFor(XMPPConnection.mConnection);
                Collection<RosterEntry> entries = roster.getEntries();
                for (RosterEntry entry : entries) {
                    chatList.add(new Contact(entry.getName(),0));
                }




            }
            return chatList;
        }



    public void refresh() {
        chatroomList = getChatList();
        if (chatroomList.isEmpty()) {
            Log.i(TAG, "refresh: empty");
            noChatsToShowView.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
        } else {
            Log.i(TAG, "refresh: not empty");
            noChatsToShowView.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        }
        chatListAdapter.setDataset(chatroomList);
        chatListAdapter.notifyDataSetChanged();
    }

    public boolean getIsDeleteMode() {
        return isDeleteMode;
    }

    public void setIsDeleteMode(boolean b) {
        isDeleteMode = b;
    }

    public void getOutOfDeleteMode() {
        chatListAdapter.getOutOfDeleteMode();
        setIsDeleteMode(false);
        deletechat.setVisibility(View.GONE);
        selectDeselect.setVisibility(View.GONE);
    }

    public void enterDeleteMode() {
        setIsDeleteMode(true);
        deletechat.setVisibility(View.VISIBLE);
        selectDeselect.setVisibility(View.VISIBLE);
        selectDeselect.setImageDrawable(getResources().getDrawable(R.drawable.select_all, getApplication().getTheme()));
    }

    private void removeChatsConversation(List<Contact> rooms) {
//        if (rooms.size() == 0) {
//            Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show();
//            chatListAdapter.getOutOfDeleteMode();
//        } else {
//            for (int i = 0; i < rooms.size(); i++) {
//                String sipUri = rooms.get(i).getSipUri();
//                if (sipUri != null) {
//                    SygnalChatRoom chatroom = SygnalManager.getLc().getOrCreateChatRoom(sipUri);
//                    if (chatroom != null) {
//                        chatroom.deleteHistory();
//                    }
//                }
//            }
//            Toast.makeText(getContext(), "Chatrooms deleted successfully", Toast.LENGTH_SHORT).show();
//            MainActivity.getInstance().updateMissedChatCount();
//            refresh();
//        }
    }

    public void change(int i, Context context) {
        int size = 0;
        TypedValue tv = new TypedValue();
        try {
            if (getApplication().getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
                size = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
            }
        } catch (Exception e) {
            size = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        Log.d("changeparam", "sendToolbarInfo: param:" + i);
        params.setMargins(40, 60, 60, Math.abs(size + i + 60));
        addNewChat.setLayoutParams(params);
        deletechat.setLayoutParams(params);

    }





    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onItemClick(int position) {
        Intent i = new Intent(this,MainActivity.class);
        i.putExtra("username",chatListAdapter.getItem(position).getName());
        startActivity(i);
        refresh();
    }

}
