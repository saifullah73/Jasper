package com.example.jasper.Activities.MainActivity;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.jasper.Activities.Chat.ChatActivity;
import com.example.jasper.AppBackend.Xmpp.XmppCore;
import com.example.jasper.Constants;
import com.example.jasper.R;

public class AddChatDialog extends Dialog implements android.view.View.OnClickListener {

    private Activity activity;
    public Button cancel, add;
    private ProgressBar progressBar;
    private EditText editText;
    private TextView errorText;

    public AddChatDialog(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.new_chat_dialog);
        errorText = findViewById(R.id.error_text);
        editText = findViewById(R.id.dialog_editText);
        progressBar = findViewById(R.id.dialog_progress);
        add = (Button) findViewById(R.id.add_chat_dialog);
        cancel = (Button) findViewById(R.id.cancel_dialog);
        add.setOnClickListener(this);
        cancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_chat_dialog:
                break;
            case R.id.cancel_dialog:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }

    private void addChat(){
        String username = editText.getText().toString().trim();
        XmppCore.getInstance().sendMessage("HI",username+"@"+ Constants.domain);
    }
}