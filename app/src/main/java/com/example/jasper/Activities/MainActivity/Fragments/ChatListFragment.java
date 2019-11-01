package com.example.jasper.Activities.MainActivity.Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import com.example.jasper.Activities.Chat.ChatActivity;
import com.example.jasper.Activities.MainActivity.AddChatDialog;
import com.example.jasper.Activities.MainActivity.MainActivity;
import com.example.jasper.Adapters.ChatListAdapter;
import com.example.jasper.AppBackend.Xmpp.XmppCore;
import com.example.jasper.Constants;
import com.example.jasper.Interfaces.ChatListClickListener;
import com.example.jasper.Models.Contact;
import com.example.jasper.R;


import android.content.Context;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment implements ChatListClickListener{

    private static final String TAG = "ChatListFragment";
    private RecyclerView recyclerView;
    private View view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChatListAdapter chatListAdapter;
    private static ChatListFragment mInstance;
    private TextView noChatsToShowView;
    private FloatingActionButton addNewChat, deletechat, selectDeselect;
    private ConstraintLayout.LayoutParams params;
    private boolean isDeleteMode;
    private List<Contact> chatroomList = new ArrayList<>();

    public ChatListFragment() {
    }


    public static ChatListFragment getInstance(){
        if(mInstance == null){
            mInstance = new ChatListFragment();
        }
        return mInstance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.chatlistfragment, container, false);
        initComponents();
        return view;
    }

    private void initComponents() {
        recyclerView = view.findViewById(R.id.chatList_recylerView);
        registerForContextMenu(recyclerView);
        addNewChat = view.findViewById(R.id.add_new_chat);
        deletechat = view.findViewById(R.id.delete_chatroom);
        swipeRefreshLayout = view.findViewById(R.id.chat_list_swipe_refresh);
        selectDeselect = view.findViewById(R.id.chatlist_select_deselect);
        noChatsToShowView = view.findViewById(R.id.no_chats_to_show_txt);
        params = (ConstraintLayout.LayoutParams) addNewChat.getLayoutParams();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
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

        addNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddChatDialog dialog = new AddChatDialog(getActivity());
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                dialog.show();
                dialog.getWindow().setAttributes(lp);
            }
        });

    }

        private void updateChatRoomsFromServer() {
            refresh();
            swipeRefreshLayout.setRefreshing(false);
        }

        public List<Contact> getChatList () {
            return XmppCore.getInstance().getContactsListForCurrentUser();
        }



    public void refresh() {
        MainActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });
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


    public void change(int i, Context context) {
        int size = 0;
        TypedValue tv = new TypedValue();
        try {
            if (getActivity().getApplication().getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
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
        Intent i = new Intent(getActivity(), ChatActivity.class);
        String username = chatListAdapter.getItem(position).getName();
        String jid = username+ "@"+ Constants.domain;
        i.putExtra("username",username);
        String resource = XmppCore.getInstance().getResourceOfUser(jid);
        Log.i("XmppCoreR", "ChatList() username= "+username);
        if (resource !=null){
            Log.i("XmppCoreR", "ChatList() resource= "+resource);
            Constants.map.put(username,resource);
        }
        Constants.unread.put(chatListAdapter.getItem(position).getName(),0);
        startActivity(i);
        MainActivity.getInstance().updateMissedChatCount();
        refresh();
    }

}
