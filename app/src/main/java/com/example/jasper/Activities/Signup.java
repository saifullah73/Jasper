package com.example.jasper.Activities;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.jasper.AppBackend.Interfaces.XMPPCore;
import com.example.jasper.AppBackend.Xmpp.XmppCore;
import com.example.jasper.Models.Contact;
import com.example.jasper.R;

import java.util.List;

public class Signup extends AppCompatActivity implements View.OnClickListener  {
    TextInputEditText name,username,password,confirmpassword;
    Button signUp;
    TextView loginPage;
    XmppCore xmppCore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        name=findViewById(R.id.NametextInputEditText);
        username=findViewById(R.id.UsertextInputEditText);
        password=findViewById(R.id.PasswordtextInputEditText);
        confirmpassword=findViewById(R.id.ConfirmpasswordEditText);
        signUp=findViewById(R.id.signupButton);
        loginPage=findViewById(R.id.login);

        signUp.setOnClickListener(this);
        loginPage.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        if (v==signUp){
            Editable editName=(Editable) name.getText();
            String name=editName.toString();

            Editable editUser=(Editable) username.getText();
            String user=editUser.toString();

            Editable editPass=(Editable) password.getText();
            String password=editPass.toString();

            Editable editConfirm=(Editable) confirmpassword.getText();
            String confirmPass=editConfirm.toString();

            XmppCore.getInstance().signup(name,user,password,confirmPass);
        }

        else if (v==loginPage){
            Intent intent=new Intent(this,Login.class);
            startActivity(intent);
        }

    }
}
