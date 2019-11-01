package com.example.jasper.Activities;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jasper.Activities.MainActivity.MainActivity;
import com.example.jasper.AppBackend.Interfaces.XMPPCore;
import com.example.jasper.AppBackend.Xmpp.XmppCore;
import com.example.jasper.Models.Contact;
import com.example.jasper.R;
import com.example.jasper.Utils;

import java.util.List;

public class Signup extends AppCompatActivity implements View.OnClickListener  {
    TextInputEditText name,username,password,domain,confirmpassword;
    Button signUp;
    TextView loginPage;
    private static Signup instance;
    ProgressBar progressBar;
    XmppCore xmppCore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        name=findViewById(R.id.NametextInputEditText);
        username=findViewById(R.id.UsertextInputEditText);
        password=findViewById(R.id.PasswordtextInputEditText);
        domain=findViewById(R.id.dd_signup);
        confirmpassword = findViewById(R.id.cc_signup);
        signUp=findViewById(R.id.signupButton);
        progressBar = findViewById(R.id.signup_prog);
        signUp.setOnClickListener(this);
        instance = this;


    }


    public static Signup getInstance(){
        if (instance==null){
            return new Signup();
        }
        return instance;
    }



    @Override
    public void onClick(View v) {

        if (v==signUp){
            Editable editName=(Editable) name.getText();
            String name=editName.toString().trim();

            Editable editUser=(Editable) username.getText();
            String user=editUser.toString().trim();

            Editable editPass=(Editable) password.getText();
            String password=editPass.toString().trim();

            Editable editConfirm=(Editable) confirmpassword.getText();
            String confirmPass=editConfirm.toString().trim();

            Editable domain0=(Editable) domain.getText();
            String domain1=domain0.toString().trim();

            if (name.length() ==0 || username.length() == 0){
                Toast.makeText(this,"Please fill all fields",Toast.LENGTH_SHORT).show();
            }
            else if (password.length() == 0){
                Toast.makeText(this,"Please enter a password",Toast.LENGTH_SHORT).show();
            }
            else if(!password.equals(confirmPass)){
                Toast.makeText(this,"Password do not match",Toast.LENGTH_SHORT).show();
            }
            else {
                signUp.setEnabled(false);
                Utils.hideKeyboard(instance);
                progressBar.setVisibility(View.VISIBLE);
                XmppCore.signup(name, user, password, domain1);
            }
        }
    }
    public void success(){
        Log.i("Signup", "I am called" );
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               progressBar.setVisibility(View.GONE);
                Log.i("Signup", "run: auth done and connected successfully" );
                Toast.makeText(getApplication(),"Account Created Successfully",Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Signup.this, Login.class);
                startActivity(i);
                finish();
           }
    });

    }

    public void failure(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               // progressBar.setVisibility(View.GONE);
                signUp.setEnabled(true);
                Toast.makeText(getApplicationContext(),"Error creating account",Toast.LENGTH_SHORT).show();
            }
        });
    }

}
