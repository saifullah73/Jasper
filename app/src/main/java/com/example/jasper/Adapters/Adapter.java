package com.example.jasper.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jasper.Models.MessageModel;
import com.example.jasper.R;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.Holder> {

    private ArrayList<MessageModel> mMessagesData;
    public Adapter(ArrayList<MessageModel> arrayList){
        mMessagesData = arrayList;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_layout,parent,false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        MessageModel data = mMessagesData.get(position);
        holder.heading.setText(data.getHeading());
        holder.messages.setText(data.getMessages());

    }

    @Override
    public int getItemCount() {
        return mMessagesData.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView heading,messages;
        public Holder(View itemView) {
            super(itemView);
            heading = itemView.findViewById(R.id.heading);
            messages = itemView.findViewById(R.id.messageBody);
        }
    }
}
