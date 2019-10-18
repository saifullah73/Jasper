package com.example.jasper.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.jasper.Activities.MainActivity.MainActivity;
import com.example.jasper.R;
import com.example.jasper.AppBackend.Xmpp.XMPPConnection;

public class Login extends AppCompatActivity {
    
    EditText passwordView,usernameView,domainView;
    Button loginBtn;
    ProgressBar progressBar;
    private static Login instance;
    private static final String TAG = "Login";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        passwordView = findViewById(R.id.password);
        usernameView = findViewById(R.id.username);
        domainView = findViewById(R.id.domainView);
        domainView.setText(getString(R.string.domain));
        loginBtn = findViewById(R.id.loginBtn);
        progressBar = findViewById(R.id.progressBar);
        instance = this;
        
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (perfromCheck()){
                    progressBar.setVisibility(View.VISIBLE);
                    final String username = usernameView.getText().toString().trim();
                    final String password = passwordView.getText().toString().trim();
                    final String domain = domainView.getText().toString().trim();
                    XMPPConnection.setConnection(username,password,domain);
                }
                
            }
        });
    }

    public static Login getInstance(){
        if (instance==null){
            return new Login();
        }
        return instance;
    }
    
    
    private boolean perfromCheck(){
        if(usernameView.getText().length() ==0) {
            Toast.makeText(this, "Username can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (passwordView.getText().length() == 0) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void success(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "run: auth done and connected successfully" );
                Intent i = new Intent(Login.this, MainActivity.class);
                startActivity(i);
            }
        });

    }

    public void failure(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"Cannot login",Toast.LENGTH_SHORT).show();
            }
        });
    }


}
