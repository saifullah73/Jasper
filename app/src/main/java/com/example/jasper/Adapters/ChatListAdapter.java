package com.example.jasper.Adapters;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;


import com.example.jasper.Activities.MainActivity;
import com.example.jasper.Interfaces.ChatListClickListener;
import com.example.jasper.Models.Contact;
import com.example.jasper.R;

import java.util.ArrayList;
import java.util.List;


public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    private static final String TAG = "ChatListAdapter";
    private List<Contact> dataset;
    private boolean isDeleteMode;
    private boolean areAllItemSelected = false;
    private List<Contact> selectedItems;
    private ChatListClickListener chatListClickListener;


    public ChatListAdapter(List<Contact> data, ChatListClickListener chatListClickListener) {
        this.dataset = data;
        this.chatListClickListener = chatListClickListener;
        selectedItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatlist_cell, parent, false);
        return new ChatListAdapter.ChatListViewHolder(view, chatListClickListener);
    }


    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, final int position) {
        Contact contact = dataset.get(position);
        String name = contact.getName();
        holder.usernameview.setText(name);
        holder.lastMessageView.setText("");
        if (contact.getUnreadMessageCount() > 0) {
            holder.usernameview.setTypeface(null, Typeface.BOLD);
            holder.unreadMessageView.setText(String.valueOf(contact.getUnreadMessageCount()));
            holder.unreadMessageView.setVisibility(View.VISIBLE);
            //MainActivity.getInstance().updateMissedChatCount();
            if (contact.getUnreadMessageCount() > 99) {
                holder.unreadMessageView.setText("99+");
            }
        } else {
            holder.unreadMessageView.setVisibility(View.GONE);
            holder.usernameview.setTypeface(null, Typeface.NORMAL);
        }
        //holder.timestampView.setText(SygnalUtils.timestampToHumanDate(MainActivity.getInstance(), chatRoom.getLastMessageTime(), ChatListFragment.getInstance().getString(R.string.messages_list_date_format)));


        holder.layout.setOnClickListener(holder);
        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                enterDeleteMode();
                return false;
            }
        });
        if (isDeleteMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            if (areAllItemSelected) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    selectedItems.add(dataset.get(position));
                    Log.i(TAG, "checkbox: added " + dataset.get(position).getName());
                } else {
                    selectedItems.remove((dataset.get(position)));
                    Log.i(TAG, "checkbox: removed " + dataset.get(position).getName());
                }
            }
        });
    }


    public boolean areAllItemSelected() {
        return areAllItemSelected;
    }

    public List<Contact> getSelectedItems() {
        return selectedItems;
    }

    public void selectAll() {
        areAllItemSelected = true;
        selectedItems.addAll(dataset);
        notifyDataSetChanged();
    }

    public void deselectAll() {
        areAllItemSelected = false;
        selectedItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public void setDataset(List<Contact> dataset) {
        this.dataset = dataset;
    }

    public void enterDeleteMode() {
        isDeleteMode = true;
        //ChatListFragment.getInstance().enterDeleteMode();
        notifyDataSetChanged();
    }

    public void getOutOfDeleteMode() {
        isDeleteMode = false;
        areAllItemSelected = false;
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public Contact getItem(int position) {
        return dataset.get(position);
    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView usernameview, lastMessageView, unreadMessageView, timestampView;
        private ConstraintLayout layout;
        private CheckBox checkBox;
        private ImageView chatListAvatar;
        private ChatListClickListener listener;

        public ChatListViewHolder(@NonNull View itemView, ChatListClickListener listener) {
            super(itemView);
            usernameview = itemView.findViewById(R.id.chatList_username);
            lastMessageView = itemView.findViewById(R.id.chatList_lastSentMessage);
            unreadMessageView = itemView.findViewById(R.id.chat_list_unreadMessages);
            timestampView = itemView.findViewById(R.id.chat_list_last_message_timestamp);
            checkBox = itemView.findViewById(R.id.chat_list_checkbox);
            layout = itemView.findViewById(R.id.chatlist_constraint_layout);
            chatListAvatar = itemView.findViewById(R.id.chatlist_avatar);
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(getAdapterPosition());
        }
    }
}

