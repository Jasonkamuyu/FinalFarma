package com.example.finalmkulima.Farmers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.finalmkulima.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class FarmerLoginActivity extends AppCompatActivity {
    private Button loginFamerButton;
    private EditText emailInput,passwordInput;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_login);

        emailInput=findViewById(R.id.farmer_login_email);
        passwordInput=findViewById(R.id.farmer_login_password);
        loginFamerButton=findViewById(R.id.farmer_login_btn);

        loadingBar = new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();


        loginFamerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginFarmer();
            }
        });
    }

    private void loginFarmer() {
        final String email=emailInput.getText().toString();
        final String password=passwordInput.getText().toString();

        if(!email.equals("")  &&!password.equals("") ){

            loadingBar.setTitle("Logging In");
            loadingBar.setMessage("Please wait, while we are checking the credentials.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                       if(task.isSuccessful()){
                           Intent intent= new Intent(FarmerLoginActivity.this, FarmerHomeActivity.class);
                           intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                           startActivity(intent);
                           finish();



                       }                        }
                    });
    }

        else{

            Toast.makeText(FarmerLoginActivity.this,"Please complete Login Form",Toast.LENGTH_SHORT).show();

        }}}
