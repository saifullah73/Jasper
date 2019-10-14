//package com.example.jasper.Adapters;
//
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.content.res.ColorStateList;
//import android.graphics.Bitmap;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//import android.net.Uri;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.constraint.ConstraintLayout;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.widget.AppCompatImageView;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageButton;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.DataSource;
//import com.bumptech.glide.load.engine.GlideException;
//import com.bumptech.glide.request.RequestListener;
//import com.bumptech.glide.request.target.Target;
//import com.example.jasper.Models.MessageModel;
//import com.example.jasper.R;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//import static android.view.View.GONE;
//import static android.view.View.VISIBLE;
//
//public class MessageModelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//    public static final int SENT_MESSAGE_TYPE = 1;
//    public static final int RECEIVED_MESSAGE_TYPE = 2;
//
//    private List<MessageModel> mMessages;
//    private boolean isDeleteModeOn = false;
//    private List<MessageModel> mDeleteAbleMessages;
//    private ChangeToolbarMenuItemsListener changeToolbarMenuItemsListener;
//
//
//    public MessageModelAdapter(List<MessageModel> mMessages) {
//        this.mMessages = mMessages;
//        mDeleteAbleMessages = new ArrayList<>();
//        setHasStableIds(true);
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        MessageModel message = mMessages.get(position);
//        if (message.getSender()) {
//            return SENT_MESSAGE_TYPE;
//        } else {
//            return RECEIVED_MESSAGE_TYPE;
//        }
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view;
//        if (viewType == SENT_MESSAGE_TYPE) {
//            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_message_sent_layout, parent, false);
//            return new SentMessageViewHolder(view);
//        } else if (viewType == RECEIVED_MESSAGE_TYPE) {
//            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_message_received_layout, parent, false);
//            return new ReceivedMessageViewHolder(view);
//        }
//        return null;
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        MessageModel message = mMessages.get(position);
//        Log.d("imagedownloadprogress", "onBindViewHolder: inflating: " + position);
//        switch (holder.getItemViewType()) {
//            case SENT_MESSAGE_TYPE:
//                message.getParentMessage().setListener((SentMessageViewHolder) holder);
//                ((SentMessageViewHolder) holder).sId = message.getParentMessage().getStorageId();
//                handleIfMessageSentOrArrived(holder, message, position, "sent");
//                initDeleteMode(holder, message, position, "sent");
//                handleProgress(holder, message, position, "sent");
//                SygnalManager.addListener((SentMessageViewHolder) holder);
//                break;
//            case RECEIVED_MESSAGE_TYPE:
//                message.getParentMessage().setListener((ReceivedMessageViewHolder) holder);
//                ((ReceivedMessageViewHolder) holder).rId = message.getParentMessage().getStorageId();
//                handleIfMessageSentOrArrived(holder, message, position, "received");
//                initDeleteMode(holder, message, position, "received");
//                SygnalManager.addListener((ReceivedMessageViewHolder) holder);
//                break;
//        }
//    }
//
//    private void initDeleteMode(final RecyclerView.ViewHolder holder, final MessageModel message, final int pos, String type) {
//        if (type.equals("sent")) {
//            ((SentMessageViewHolder) holder).sentTextMsgBody.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    if (!isDeleteModeOn) {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was not selected.");
//                            addMessageToDeleteList(message, pos);
//                            putDeleteModeOn();
//                        } else {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                            putDeleteModeOn();
//                        }
//                    } else {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was ON onLongClick. and message was not selected.");
//                        } else {
//                            showNewAdapterLogs("Delete mode was ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                        }
//
//                    }
//                    return true;
//                }
//            });
//
//            ((SentMessageViewHolder) holder).sentImageMsgPicture.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    if (!isDeleteModeOn) {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was not selected.");
//                            addMessageToDeleteList(message, pos);
//                            putDeleteModeOn();
//                        } else {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                            putDeleteModeOn();
//                        }
//                    } else {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was ON onLongClick.and message was not selected.");
//                            addMessageToDeleteList(message, pos);
//                        } else {
//                            showNewAdapterLogs("Delete mode was ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                        }
//
//                    }
//                    return true;
//                }
//            });
//
//            ((SentMessageViewHolder) holder).sentImageMsgChildLayout.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    if (!isDeleteModeOn) {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was not selected.");
//                            addMessageToDeleteList(message, pos);
//                            putDeleteModeOn();
//                        } else {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                            putDeleteModeOn();
//                        }
//                    } else {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was ON onLongClick.and message was not selected.");
//                            addMessageToDeleteList(message, pos);
//                        } else {
//                            showNewAdapterLogs("Delete mode was ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                        }
//
//                    }
//                    return true;
//                }
//            });
//
//            ((SentMessageViewHolder) holder).sentLocationMsgPicture.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    if (!isDeleteModeOn) {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was not selected.");
//                            addMessageToDeleteList(message, pos);
//                            putDeleteModeOn();
//                        } else {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                            putDeleteModeOn();
//                        }
//                    } else {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was ON onLongClick.and message was not selected.");
//                            addMessageToDeleteList(message, pos);
//                        } else {
//                            showNewAdapterLogs("Delete mode was ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                        }
//
//                    }
//                    return true;
//                }
//            });
//
//            ((SentMessageViewHolder) holder).sentLocationMsgChildLayout.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    if (!isDeleteModeOn) {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was not selected.");
//                            addMessageToDeleteList(message, pos);
//                            putDeleteModeOn();
//                        } else {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                            putDeleteModeOn();
//                        }
//                    } else {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was ON onLongClick.and message was not selected.");
//                            addMessageToDeleteList(message, pos);
//                        } else {
//                            showNewAdapterLogs("Delete mode was ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                        }
//
//                    }
//                    return true;
//                }
//            });
//
//
//        } else if (type.equals("received")) {
//            ((ReceivedMessageViewHolder) holder).receivedTextMsgBody.setOnLongClickListener(new View.OnLongClickListener() {
//                @SuppressLint("SyntheticAccessor")
//                @Override
//                public boolean onLongClick(View view) {
//                    if (!isDeleteModeOn) {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was not selected.");
//                            addMessageToDeleteList(message, pos);
//                            putDeleteModeOn();
//                        } else {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                            putDeleteModeOn();
//                        }
//                    } else {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was ON onLongClick. and message was not selected.");
//                        } else {
//                            showNewAdapterLogs("Delete mode was ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                        }
//
//                    }
//                    return true;
//                }
//            });
//
//            ((ReceivedMessageViewHolder) holder).receivedImageMsgPicture.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    if (!isDeleteModeOn) {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was not selected.");
//                            addMessageToDeleteList(message, pos);
//                            putDeleteModeOn();
//                        } else {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                            putDeleteModeOn();
//                        }
//                    } else {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was ON onLongClick.and message was not selected.");
//                            addMessageToDeleteList(message, pos);
//                        } else {
//                            showNewAdapterLogs("Delete mode was ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                        }
//
//                    }
//                    return true;
//                }
//            });
//
//            ((ReceivedMessageViewHolder) holder).receivedImageMsgChildLayout.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    if (!isDeleteModeOn) {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was not selected.");
//                            addMessageToDeleteList(message, pos);
//                            putDeleteModeOn();
//                        } else {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                            putDeleteModeOn();
//                        }
//                    } else {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was ON onLongClick.and message was not selected.");
//                            addMessageToDeleteList(message, pos);
//                        } else {
//                            showNewAdapterLogs("Delete mode was ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                        }
//
//                    }
//                    return true;
//                }
//            });
//
//            ((ReceivedMessageViewHolder) holder).receivedLocationMsgPicture.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    if (!isDeleteModeOn) {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was not selected.");
//                            addMessageToDeleteList(message, pos);
//                            putDeleteModeOn();
//                        } else {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                            putDeleteModeOn();
//                        }
//                    } else {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was ON onLongClick.and message was not selected.");
//                            addMessageToDeleteList(message, pos);
//                        } else {
//                            showNewAdapterLogs("Delete mode was ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                        }
//
//                    }
//                    return true;
//                }
//            });
//
//            ((ReceivedMessageViewHolder) holder).receivedLocationMsgChildLayout.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    if (!isDeleteModeOn) {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was not selected.");
//                            addMessageToDeleteList(message, pos);
//                            putDeleteModeOn();
//                        } else {
//                            showNewAdapterLogs("Delete mode was not ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                            putDeleteModeOn();
//                        }
//                    } else {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Delete mode was ON onLongClick.and message was not selected.");
//                            addMessageToDeleteList(message, pos);
//                        } else {
//                            showNewAdapterLogs("Delete mode was ON onLongClick. and message was selected.");
//                            removeMessageFromDeleteList(message, pos);
//                        }
//
//                    }
//                    return true;
//                }
//            });
//
//        } else {
//        }
//    }
//
//    public void putDeleteModeOff() {
//        isDeleteModeOn = false;
//        changeToolbarMenuItemsListener.ChangeToolBarMenuItems(0, false);
//        mDeleteAbleMessages.clear();
//        resetAllItems();
//        showNewAdapterLogs("Delete List size after clearing: " + mDeleteAbleMessages.size());
//    }
//
//    public void setInterface(ChangeToolbarMenuItemsListener ChangeToolbarMenuItemsListener) {
//        this.changeToolbarMenuItemsListener = ChangeToolbarMenuItemsListener;
//    }
//
//    public interface ChangeToolbarMenuItemsListener {
//        void ChangeToolBarMenuItems(int selectedMsgs, boolean onlyTextMsgSelected);
//    }
//
//    private void putDeleteModeOn() {
//        showNewAdapterLogs("Delete Mode turned ON.");
//        isDeleteModeOn = true;
//    }
//
//    public void resetAllItems() {
//        for (int i = 0; i < mMessages.size(); i++) {
//            if (mMessages.get(i).getSelected() || mMessages.get(i).getExpandable()) {
//                mMessages.get(i).setSelected(false);
//                mMessages.get(i).setExpandable(false);
//                notifyItemChanged(i);
//            }
//        }
//    }
//
//    private void addMessageToDeleteList(MessageModel message, int i) {
//        mDeleteAbleMessages.add(message);
//        showNewAdapterLogs("Message " + i + " Added. and total: " + mDeleteAbleMessages.size());
//        changeToolbarMenuItemsListener.ChangeToolBarMenuItems(mDeleteAbleMessages.size(), checkOnlyIfTextMsgSelected());
//        message.setSelected(true);
//        notifyItemChanged(i);
//    }
//
//    public List<MessageModel> getDeletedAbleList() {
//        return mDeleteAbleMessages;
//    }
//
//    private void removeMessageFromDeleteList(MessageModel message, int i) {
//        mDeleteAbleMessages.remove(message);
//        message.setSelected(false);
//        showNewAdapterLogs("Message " + i + " Removed. and total: " + mDeleteAbleMessages.size());
//        changeToolbarMenuItemsListener.ChangeToolBarMenuItems(mDeleteAbleMessages.size(), checkOnlyIfTextMsgSelected());
//        if (mDeleteAbleMessages.size() == 0) {
//            putDeleteModeOff();
//            showNewAdapterLogs("Delete Mode Turned OFF.");
//        }
//        notifyItemChanged(i);
//    }
//
//    private boolean checkOnlyIfTextMsgSelected() {
//        if (mDeleteAbleMessages != null && !mDeleteAbleMessages.isEmpty()) {
//            for (int i = 0; i < mDeleteAbleMessages.size(); i++) {
//                if (!mDeleteAbleMessages.get(i).getMsgType().equals("text")) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
//
//    private void showNewAdapterLogs(String msg) {
//        Log.d("NewMessageAdapterLogs", "showNewAdapterLogs: " + msg);
//    }
//
//    private void handleIfMessageSentOrArrived(RecyclerView.ViewHolder holder, MessageModel message, int position, String type) {
//        if (message.getMsgType().equals("text")) {
//            handleIfTextMessage(holder, message, type, position);
//        } else if (message.getMsgType().equals("image")) {
//            handleIfImageMessage(holder, message, type, position);
//        } else if (message.getMsgType().equals("location")) {
//            handleIfLocationMessage(holder, message, type, position);
//        } else {
//            // Do nothing...
//        }
//    }
//
//    private void handleIfTextMessage(final RecyclerView.ViewHolder holder, final MessageModel message, String type, final int position) {
//        if (type.equals("sent")) {
//            ((SentMessageViewHolder) holder).sentImageMsgParentLayout.setVisibility(GONE);
//            ((SentMessageViewHolder) holder).sentLocationMsgParentLayout.setVisibility(GONE);
//            ((SentMessageViewHolder) holder).sentTextMsgParentLayout.setVisibility(VISIBLE);
//
//           // ((SentMessageViewHolder) holder).sentTextMsgBody.setText(message.getParentMessage().getText());
//
//            if (message.getSelected()) {
//                ((SentMessageViewHolder) holder).sentMsgHideAbleView.setVisibility(VISIBLE);
//
//            } else {
//                ((SentMessageViewHolder) holder).sentMsgHideAbleView.setVisibility(GONE);
//            }
//
//
//            ((SentMessageViewHolder) holder).sentTextMsgBody.setOnClickListener(new View.OnClickListener() {
//                @SuppressLint("SyntheticAccessor")
//                @Override
//                public void onClick(View v) {
//                    if (!isDeleteModeOn) {
//
//                    } else {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Mode was ON onClick. and message was not selected.");
//                            addMessageToDeleteList(message, position);
//                        } else {
//                            showNewAdapterLogs("Mode was ON onClick. and message was selected.so removing...");
//                            removeMessageFromDeleteList(message, position);
//                        }
//                    }
//
//                }
//            });
//
//            ((SentMessageViewHolder) holder).sentMsgHideAbleView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (message.getSelected() && isDeleteModeOn && ((SentMessageViewHolder) holder).sentMsgHideAbleView.getVisibility() == VISIBLE) {
//                        removeMessageFromDeleteList(message, position);
//                    }
//                }
//            });
//
//        } else if (type.equals("received")) {
//            ((ReceivedMessageViewHolder) holder).receivedImageMsgParentLayout.setVisibility(GONE);
//            ((ReceivedMessageViewHolder) holder).receivedLocationMsgParentLayout.setVisibility(GONE);
//            ((ReceivedMessageViewHolder) holder).receivedTextMsgParentLayout.setVisibility(VISIBLE);
//
//           // ((ReceivedMessageViewHolder) holder).receivedTextMsgBody.setText(message.getParentMessage().getText());
//
//            if (message.getSelected()) {
//                ((ReceivedMessageViewHolder) holder).receivedMsgHideAbleView.setVisibility(View.VISIBLE);
//            } else {
//                ((ReceivedMessageViewHolder) holder).receivedMsgHideAbleView.setVisibility(GONE);
//            }
//
//            ((ReceivedMessageViewHolder) holder).receivedTextMsgBody.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (!isDeleteModeOn) {
//
//                    } else {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Mode was ON onClick. and message was not selected.");
//                            addMessageToDeleteList(message, position);
//                        } else {
//                            showNewAdapterLogs("Mode was ON onClick. and message was selected.so removing...");
//                            removeMessageFromDeleteList(message, position);
//                        }
//                    }
//
//                }
//            });
//
//            ((ReceivedMessageViewHolder) holder).receivedMsgHideAbleView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (message.getSelected() && isDeleteModeOn && ((ReceivedMessageViewHolder) holder).receivedMsgHideAbleView.getVisibility() == VISIBLE) {
//                        removeMessageFromDeleteList(message, position);
//                    }
//                }
//            });
//
//        } else {
//            // Do nothing...
//        }
//
//    }
//
//    private void handleProgress(final RecyclerView.ViewHolder holder, final MessageModel message, final int position, String type) {
//        if (message.getMsgType().equals("image")) {
//            if (type.equals("sent")) {
//                ((SentMessageViewHolder) holder).imgProgressActionBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (message.getImageMsgStatus().equals("uploadable")) {
//                            Log.d("imagedownloadprogress", "Status of " + position + " on clicking is: " + message.getImageMsgStatus());
//                            ((SentMessageViewHolder) holder).imgProgressActionBtn.setImageResource(R.drawable.ic_close_black_24dp);
//                            ((SentMessageViewHolder) holder).sentImgMsgProgressBar.setVisibility(VISIBLE);
//                            message.setImageMsgStatus("uploading");
//                      //      ChatActivity.getInstance().uploadImage(getItem(position));
//                            notifyItemChanged(position);
//                        } else if (message.getImageMsgStatus().equals("uploading")) {
//                            Log.d("imagedownloadprogress", "Status of " + position + " on clicking is: " + message.getImageMsgStatus());
//                            ((SentMessageViewHolder) holder).imgProgressActionBtn.setImageResource(R.drawable.ic_file_upload_black_24dp);
//                            ((SentMessageViewHolder) holder).sentImgMsgProgressBar.setVisibility(GONE);
//                            message.setImageMsgStatus("uploadable");
//                       //     ChatActivity.getInstance().cancelUpload();
//                            notifyItemChanged(position);
//                        } else {
//                            message.setImageMsgStatus("uploaded");
//                            notifyItemChanged(position);
//                        }
//                    }
//                });
//            } else {
//
//            }
//        }
//
//    }
//
//    @SuppressLint("SyntheticAccessor")
//    private void handleIfImageMessage(final RecyclerView.ViewHolder holder, final MessageModel message, String type, final int position) {
//        String msgText = message.getParentMessage().getText();
//        // Outgoing Image Message.
//        if (type.equals("sent")) {
//
//            ((SentMessageViewHolder) holder).sentMsgHideAbleView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (message.getSelected() && isDeleteModeOn && ((SentMessageViewHolder) holder).sentMsgHideAbleView.getVisibility() == VISIBLE) {
//                        removeMessageFromDeleteList(message, position);
//                    }
//                }
//            });
//
//            if (message.getSelected()) {
//                ((SentMessageViewHolder) holder).sentMsgHideAbleView.setVisibility(View.VISIBLE);
//            } else {
//                ((SentMessageViewHolder) holder).sentMsgHideAbleView.setVisibility(GONE);
//            }
//
//            String sentPath = null;
//            if (msgText.contains("#image#memory*-*")) {
//                sentPath = msgText.replace("#image#memory*-*", "");
//                ((SentMessageViewHolder) holder).sentImgMsgProgressBar.setVisibility(VISIBLE);
//                Glide.with(((SentMessageViewHolder) holder).itemView)
//                        .load(sentPath)
//                        .centerCrop()
//                        .listener(new RequestListener<Drawable>() {
//                            @Override
//                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                                ((SentMessageViewHolder) holder).sentImgMsgProgressBar.setVisibility(GONE);
//                                return false;
//                            }
//
//                            @Override
//                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                                ((SentMessageViewHolder) holder).sentImgMsgProgressBar.setVisibility(GONE);
//                                return false;
//                            }
//                        })
//                        .placeholder(R.drawable.ic_photo_size_select_actual_black_24dp)
//                        .into(((SentMessageViewHolder) holder).sentImageMsgPicture);
//                ((SentMessageViewHolder) holder).sentImageMsgParentLayout.setVisibility(VISIBLE);
//                ((SentMessageViewHolder) holder).sentLocationMsgParentLayout.setVisibility(GONE);
//                ((SentMessageViewHolder) holder).sentTextMsgParentLayout.setVisibility(GONE);
//
//
//                String status = message.getImageMsgStatus();
//                if (status != null) {
//                    Log.d("imagedownloadprogress", "Status of " + position + " is: " + status);
//                    Log.i("imageUploadTest", "uploadImage: status " + status);
//                    if (status.equals("uploading")) {
//                        //((SentMessageViewHolder) holder).sentImgMsgProgress.setAttributeResourceId(R.drawable.ic_close_black_24dp);
//                        ((SentMessageViewHolder) holder).imgProgressActionBtn.setImageResource(R.drawable.ic_close_black_24dp);
//                        ((SentMessageViewHolder) holder).sentImgMsgProgressBar.setVisibility(VISIBLE);
//                    } else if (status.equals("uploadable")) {
//                        //((SentMessageViewHolder) holder).sentImgMsgProgress.setAttributeResourceId(R.drawable.ic_file_upload_black_24dp);
//                        ((SentMessageViewHolder) holder).imgProgressActionBtn.setImageResource(R.drawable.ic_file_upload_black_24dp);
//                        ((SentMessageViewHolder) holder).sentImgMsgProgressBar.setVisibility(GONE);
//                    } else if (status.equals("uploaded")) {
//                        ((SentMessageViewHolder) holder).imgProgressActionBtn.setVisibility(GONE);
//                        ((SentMessageViewHolder) holder).sentImgMsgProgressBar.setVisibility(GONE);
//                    } else {
//                        ((SentMessageViewHolder) holder).imgProgressActionBtn.setVisibility(GONE);
//                        ((SentMessageViewHolder) holder).sentImgMsgProgressBar.setVisibility(GONE);
//                    }
//                } else {
//                    Log.i("imageUploadTest", "uploadImage: status " + status);
//                }
//
//            }
//            if (msgText.contains("#image#server*-*")) {
//                sentPath = msgText.replace("#image#server*-*", "");
//                final String path = msgText.replace("#image#server*-*", "");
//                ((SentMessageViewHolder) holder).sentImgMsgProgressBar.setVisibility(VISIBLE);
//                ((SentMessageViewHolder) holder).sentImgMsgProgressBar.setBackgroundResource(R.drawable.progress_btn_background);
//                ((SentMessageViewHolder) holder).sentImgMsgProgressBar.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.lesserwhite)));
//                ((SentMessageViewHolder) holder).sentImageMsgParentLayout.setVisibility(VISIBLE);
//                ((SentMessageViewHolder) holder).sentLocationMsgParentLayout.setVisibility(GONE);
//                ((SentMessageViewHolder) holder).sentTextMsgParentLayout.setVisibility(GONE);
//                ((SentMessageViewHolder) holder).imgProgressActionBtn.setVisibility(GONE);
//                File file = ChatActivity.getInstance().loadImageInMemory(path);
//                if (file != null) {
//                    Glide.with(((SentMessageViewHolder) holder).itemView)
//                            .load(file)
//                            .centerCrop()
//                            .listener(new RequestListener<Drawable>() {
//                                @Override
//                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                                    ((SentMessageViewHolder) holder).sentImgMsgProgressBar.setVisibility(GONE);
//                                    return false;
//                                }
//
//                                @Override
//                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                                    ((SentMessageViewHolder) holder).sentImgMsgProgressBar.setVisibility(GONE);
//                                    return false;
//                                }
//                            })
//                            .placeholder(R.drawable.ic_photo_size_select_actual_black_24dp)
//                            .into(((SentMessageViewHolder) holder).sentImageMsgPicture);
//                } else {
//                    Glide.with(((SentMessageViewHolder) holder).itemView).load(path)
//                            .listener(new RequestListener<Drawable>() {
//                                @Override
//                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                                    ((SentMessageViewHolder) holder).sentImgMsgProgressBar.setVisibility(GONE);
//                                    return false;
//                                }
//
//                                @Override
//                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                                    ((SentMessageViewHolder) holder).sentImgMsgProgressBar.setVisibility(GONE);
//                                    ChatActivity.getInstance().saveImageInBitmap(path, ((BitmapDrawable) resource).getBitmap());
//                                    return false;
//                                }
//                            }).centerCrop()
//                            .placeholder(R.drawable.ic_photo_size_select_actual_black_24dp)
//                            .into(((SentMessageViewHolder) holder).sentImageMsgPicture);
//                }
//            }
//            final String finalSentPath = sentPath;
//            ((SentMessageViewHolder) holder).sentImageMsgPicture.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (!isDeleteModeOn) {
//                        Log.d("imagepath", "onClick: Image Path is: " + finalSentPath);
//                        Intent intent = new Intent(holder.itemView.getContext(), ImageDeatilActvity.class);
//                        intent.putExtra("path", finalSentPath);
//                        holder.itemView.getContext().startActivity(intent);
//                    } else {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Mode was ON onClick. and message was not selected.");
//                            addMessageToDeleteList(message, position);
//                        } else {
//                            showNewAdapterLogs("Mode was ON onClick. and message was selected. so removing...");
//                            removeMessageFromDeleteList(message, position);
//                        }
//                    }
//                }
//            });
//        } else if (type.equals("received")) {
//
//            if (message.getSelected()) {
//                ((ReceivedMessageViewHolder) holder).receivedMsgHideAbleView.setVisibility(View.VISIBLE);
//            } else {
//                ((ReceivedMessageViewHolder) holder).receivedMsgHideAbleView.setVisibility(GONE);
//            }
//
//            ((ReceivedMessageViewHolder) holder).receivedMsgHideAbleView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (message.getSelected() && isDeleteModeOn && ((ReceivedMessageViewHolder) holder).receivedMsgHideAbleView.getVisibility() == VISIBLE) {
//                        removeMessageFromDeleteList(message, position);
//                    }
//                }
//            });
//
//            if (msgText.contains("#image#server*-*")) {
//                ((ReceivedMessageViewHolder) holder).receivedImgMsgProgress.setVisibility(VISIBLE);
//                Log.i("imageUploadTest", "server message");
//                final String path = msgText.replace("#image#server*-*", "");
//                ((ReceivedMessageViewHolder) holder).receivedImageMsgParentLayout.setVisibility(VISIBLE);
//                ((ReceivedMessageViewHolder) holder).receivedLocationMsgParentLayout.setVisibility(GONE);
//                ((ReceivedMessageViewHolder) holder).receivedTextMsgParentLayout.setVisibility(GONE);
//                File file = ChatActivity.getInstance().loadImageInMemory(path);
//                if (file != null) {
//                    Glide.with(((ReceivedMessageViewHolder) holder).itemView)
//                            .load(file)
//                            .listener(new RequestListener<Drawable>() {
//                                @Override
//                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                                    ((ReceivedMessageViewHolder) holder).receivedImgMsgProgress.setVisibility(GONE);
//                                    return false;
//                                }
//
//                                @Override
//                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                                    ((ReceivedMessageViewHolder) holder).receivedImgMsgProgress.setVisibility(GONE);
//                                    return false;
//                                }
//                            }).centerCrop()
//                            .placeholder(R.drawable.ic_photo_size_select_actual_black_24dp)
//                            .into(((ReceivedMessageViewHolder) holder).receivedImageMsgPicture);
//                } else {
//                    Glide.with(((ReceivedMessageViewHolder) holder).itemView).load(path).listener(new RequestListener<Drawable>() {
//                        @Override
//                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                            ((ReceivedMessageViewHolder) holder).receivedImgMsgProgress.setVisibility(GONE);
//                            return false;
//                        }
//
//                        @Override
//                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                            ((ReceivedMessageViewHolder) holder).receivedImgMsgProgress.setVisibility(GONE);
//                            ChatActivity.getInstance().saveImageInBitmap(path, ((BitmapDrawable) resource).getBitmap());
//                            return false;
//                        }
//                    }).centerCrop()
//                            .placeholder(R.drawable.ic_photo_size_select_actual_black_24dp)
//                            .into(((ReceivedMessageViewHolder) holder).receivedImageMsgPicture);
//
//                }
//
//                final String finalSentPath = path;
//                ((ReceivedMessageViewHolder) holder).receivedImageMsgPicture.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (!isDeleteModeOn) {
//                            Log.d("imagepath", "onClick: Image Path is: " + finalSentPath);
//                            Intent intent = new Intent(holder.itemView.getContext(), ImageDeatilActvity.class);
//                            intent.putExtra("path", finalSentPath);
//                            holder.itemView.getContext().startActivity(intent);
//                        } else {
//                            if (!message.getSelected()) {
//                                showNewAdapterLogs("Mode was ON onClick. and message was not selected.");
//                                addMessageToDeleteList(message, position);
//                            } else {
//                                showNewAdapterLogs("Mode was ON onClick. and message was selected. so removing...");
//                                removeMessageFromDeleteList(message, position);
//                            }
//                        }
//                    }
//                });
//
//            }
//        }
//    }
//
//    private void handleIfLocationMessage(final RecyclerView.ViewHolder holder, final MessageModel message, String type, final int position) {
//        if (type.equals("sent")) {
//            ((SentMessageViewHolder) holder).sentImageMsgParentLayout.setVisibility(GONE);
//            ((SentMessageViewHolder) holder).sentLocationMsgParentLayout.setVisibility(VISIBLE);
//            ((SentMessageViewHolder) holder).sentTextMsgParentLayout.setVisibility(GONE);
//
//            if (message.getSelected()) {
//                ((SentMessageViewHolder) holder).sentMsgHideAbleView.setVisibility(View.VISIBLE);
//            } else {
//                ((SentMessageViewHolder) holder).sentMsgHideAbleView.setVisibility(GONE);
//            }
//
//            ((SentMessageViewHolder) holder).sentMsgHideAbleView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (message.getSelected() && isDeleteModeOn && ((SentMessageViewHolder) holder).sentMsgHideAbleView.getVisibility() == VISIBLE) {
//                        removeMessageFromDeleteList(message, position);
//                    }
//                }
//            });
//
//
//            String[] parsedData = message.getParentMessage().getText().split("#");
//            final String cordinated = parsedData[1];
//            ((SentMessageViewHolder) holder).sentLocationMsgPicture.setVisibility(VISIBLE);
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        final Bitmap image = ChatActivity.getInstance().getLocationImage(cordinated);
//                        Glide.with(((SentMessageViewHolder) holder).itemView).load(image).centerCrop().placeholder(R.drawable.ic_photo_size_select_actual_black_24dp).into(((SentMessageViewHolder) holder).sentLocationMsgPicture);
//                    } catch (Exception i) {
//                        System.out.print(i.getMessage());
//                    }
//
//                }
//            }).start();
//
//            ((SentMessageViewHolder) holder).sentLocationMsgPicture.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (!isDeleteModeOn) {
//                        Uri gmmIntentUri = Uri.parse("geo:" + cordinated + "?q=" + cordinated);
//                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//                        mapIntent.setPackage("com.google.android.apps.maps");
//                        if (mapIntent.resolveActivity(ChatActivity.getInstance().getPackageManager()) != null) {
//                            holder.itemView.getContext().startActivity(mapIntent);
//                        }
//                    } else {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Mode was ON onClick. and message was not selected.");
//                            addMessageToDeleteList(message, position);
//                        } else {
//                            showNewAdapterLogs("Mode was ON onClick. and message was selected. so removing...");
//                            removeMessageFromDeleteList(message, position);
//                        }
//                    }
//                }
//            });
//        } else if (type.equals("received")) {
//            ((ReceivedMessageViewHolder) holder).receivedImageMsgParentLayout.setVisibility(GONE);
//            ((ReceivedMessageViewHolder) holder).receivedLocationMsgParentLayout.setVisibility(VISIBLE);
//            ((ReceivedMessageViewHolder) holder).receivedTextMsgParentLayout.setVisibility(GONE);
//
//            if (message.getSelected()) {
//                ((ReceivedMessageViewHolder) holder).receivedMsgHideAbleView.setVisibility(View.VISIBLE);
//            } else {
//                ((ReceivedMessageViewHolder) holder).receivedMsgHideAbleView.setVisibility(GONE);
//            }
//
//            ((ReceivedMessageViewHolder) holder).receivedMsgHideAbleView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (message.getSelected() && isDeleteModeOn && ((ReceivedMessageViewHolder) holder).receivedMsgHideAbleView.getVisibility() == VISIBLE) {
//                        removeMessageFromDeleteList(message, position);
//                    }
//                }
//            });
//
//            String[] parsedData = message.getParentMessage().getText().split("#");
//            final String cordinated = parsedData[1];
//            ((ReceivedMessageViewHolder) holder).receivedLocationMsgPicture.setVisibility(VISIBLE);
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        final Bitmap image = ChatActivity.getInstance().getLocationImage(cordinated);
//                        Glide.with(((ReceivedMessageViewHolder) holder).itemView).load(image).centerCrop().placeholder(R.drawable.ic_photo_size_select_actual_black_24dp).into(((ReceivedMessageViewHolder) holder).receivedLocationMsgPicture);
//                    } catch (Exception i) {
//                        System.out.print(i.getMessage());
//                    }
//
//                }
//            }).start();
//
//            ((ReceivedMessageViewHolder) holder).receivedLocationMsgPicture.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (!isDeleteModeOn) {
//                        Uri gmmIntentUri = Uri.parse("geo:" + cordinated + "?q=" + cordinated);
//                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//                        mapIntent.setPackage("com.google.android.apps.maps");
//                        if (mapIntent.resolveActivity(ChatActivity.getInstance().getPackageManager()) != null) {
//                            holder.itemView.getContext().startActivity(mapIntent);
//                        }
//                    } else {
//                        if (!message.getSelected()) {
//                            showNewAdapterLogs("Mode was ON onClick. and message was not selected.");
//                            addMessageToDeleteList(message, position);
//                        } else {
//                            showNewAdapterLogs("Mode was ON onClick. and message was selected. so removing...");
//                            removeMessageFromDeleteList(message, position);
//                        }
//                    }
//                }
//            });
//        } else {
//
//        }
//    }
//
//    @NonNull
//    public MessageModel getItem(int i) {
//        return mMessages.get(i);
//    }
//
//    public boolean isDeleteModeOn() {
//        return isDeleteModeOn;
//    }
//
//    @Override
//    public int getItemCount() {
//        return mMessages.size();
//    }
//
//    public class SentMessageViewHolder extends RecyclerView.ViewHolder implements SygnalMessageModel.SygnalMessageModelListener {
//
//        private TextView sentTextMsgBody;
//        private AppCompatImageView sentImageMsgPicture, sentLocationMsgPicture;
//        private ConstraintLayout sentTextMsgParentLayout, sentImageMsgParentLayout, sentLocationMsgParentLayout;
//        private RelativeLayout sentTextMsgChildLayout, sentImageMsgChildLayout, sentLocationMsgChildLayout;
//        private ProgressBar sentImgMsgProgressBar, sentLocationMsgProgressBar;
//        private ImageButton imgProgressActionBtn;
//        private LinearLayout sentMsgHideAbleView;
//        public int sId;
//
//        // Find Views
//
//        public SentMessageViewHolder(View itemView) {
//            super(itemView);
//
//            sentTextMsgBody = itemView.findViewById(R.id.sent_msg_text_body_textview);
//
//
//            sentImageMsgPicture = itemView.findViewById(R.id.sent_msg_image_imageview);
//            sentLocationMsgPicture = itemView.findViewById(R.id.sent_msg_location_imageview);
//
//
//            sentTextMsgParentLayout = itemView.findViewById(R.id.sent_msg_text_parent_layout);
//            sentImageMsgParentLayout = itemView.findViewById(R.id.sent_msg_image_parent_layout);
//            sentLocationMsgParentLayout = itemView.findViewById(R.id.sent_msg_location_parent_layout);
//
//            sentTextMsgChildLayout = itemView.findViewById(R.id.sent_msg_text_child_layout);
//            sentImageMsgChildLayout = itemView.findViewById(R.id.sent_msg_image_child_layout);
//            sentLocationMsgChildLayout = itemView.findViewById(R.id.sent_msg_location_child_layout);
//
//
//            sentMsgHideAbleView = itemView.findViewById(R.id.sent_msg_hideable);
//
//            imgProgressActionBtn = itemView.findViewById(R.id.sent_msg_image_progress_action);
//
//
//            sentImgMsgProgressBar = itemView.findViewById(R.id.sent_msg_image_addtitionalProgressbar);
//
//
//        }
//
//        @Override
//        public void onSygnalMessageModelStateChanged(@NonNull SygnalMessageModel msg, @NonNull SygnalMessageModel.State state) {
//            for (int i = 0; i < mMessages.size(); i++) {
//                if (mMessages.get(i).getParentMessage() == msg) {
//                    mMessages.get(i).setStatus(state.toString());
//                    notifyItemChanged(i);
//                }
//            }
//        }
//
//        @Override
//        public void onSygnalMessageModelFileTransferReceived(SygnalMessageModel msg, SygnalContent content, SygnalBuffer buffer) {
//
//        }
//
//        @Override
//        public void onSygnalMessageModelFileTransferSent(SygnalMessageModel msg, SygnalContent content, int offset, int size, SygnalBuffer bufferToFill) {
//
//        }
//
//        @Override
//        public void onSygnalMessageModelFileTransferProgressChanged(SygnalMessageModel msg, SygnalContent content, int offset, int total) {
//            //if (msg.getStorageId() == sId) sentImgMsgProgress.setProgress(offset * 100 / total);
//            Log.d("imagedownloadprogress", "Image uploading Progress: " + offset);
//
//        }
//    }
//
//    public class ReceivedMessageViewHolder extends RecyclerView.ViewHolder implements SygnalMessageModel.SygnalMessageModelListener {
//
//        private TextView receivedTextMsgBody;
//        private AppCompatImageView receivedImageMsgPicture, receivedLocationMsgPicture;
//        private ConstraintLayout receivedTextMsgParentLayout, receivedImageMsgParentLayout, receivedLocationMsgParentLayout;
//        private RelativeLayout receivedTextMsgChildLayout, receivedImageMsgChildLayout, receivedLocationMsgChildLayout;
//        private ProgressBar receivedImgMsgProgress, receivedLocationProgress;
//        private LinearLayout receivedMsgHideAbleView;
//        public int rId;
//
//        public ReceivedMessageViewHolder(View itemView) {
//            super(itemView);
//
//            receivedTextMsgBody = itemView.findViewById(R.id.received_msg_text_body_textview);
//
//            receivedImageMsgPicture = itemView.findViewById(R.id.received_msg_image_imageview);
//            receivedLocationMsgPicture = itemView.findViewById(R.id.received_msg_location_imageview);
//
//            receivedTextMsgParentLayout = itemView.findViewById(R.id.received_msg_text_parent_layout);
//            receivedImageMsgParentLayout = itemView.findViewById(R.id.received_msg_image_parent_layout);
//            receivedLocationMsgParentLayout = itemView.findViewById(R.id.received_msg_location_parent_layout);
//
//
//            receivedTextMsgChildLayout = itemView.findViewById(R.id.received_msg_text_child_layout);
//            receivedImageMsgChildLayout = itemView.findViewById(R.id.received_msg_image_child_layout);
//            receivedLocationMsgChildLayout = itemView.findViewById(R.id.received_msg_location_child_layout);
//
//            receivedImgMsgProgress = itemView.findViewById(R.id.received_msg_image_progress_bar);
//            receivedLocationProgress = itemView.findViewById(R.id.received_msg_location_progress_bar);
//
//            receivedMsgHideAbleView = itemView.findViewById(R.id.received_msg_hideable);
//        }
//
//        @Override
//        public void onSygnalMessageModelStateChanged(@NonNull SygnalMessageModel msg, @NonNull SygnalMessageModel.State state) {
//            for (int i = 0; i < mMessages.size(); i++) {
//                if (mMessages.get(i).getParentMessage() == msg) {
//                    mMessages.get(i).setStatus(state.toString());
//                    notifyItemChanged(i);
//                }
//            }
//        }
//
//        @Override
//        public void onSygnalMessageModelFileTransferReceived(SygnalMessageModel msg, SygnalContent content, SygnalBuffer buffer) {
//
//        }
//
//        @Override
//        public void onSygnalMessageModelFileTransferSent(SygnalMessageModel msg, SygnalContent content, int offset, int size, SygnalBuffer bufferToFill) {
//
//        }
//
//        @Override
//        public void onSygnalMessageModelFileTransferProgressChanged(SygnalMessageModel msg, SygnalContent content, int offset, int total) {
//            if (msg.getStorageId() == rId) receivedImgMsgProgress.setProgress(offset * 100 / total);
//        }
//    }
//
//
//}
