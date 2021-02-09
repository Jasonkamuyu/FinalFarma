package com.example.finalmkulima.Transporters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalmkulima.Farmers.FarmerHomeActivity;
import com.example.finalmkulima.Farmers.FarmerLoginActivity;
import com.example.finalmkulima.Farmers.FarmerRegistrationActivity;
import com.example.finalmkulima.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class TransporterLoginRegisterActivity extends AppCompatActivity {

    private EditText transporterEmail, transporterPassword;
    private TextView noAccount,transporterStatus;
    private Button transporterLogin,transporterRegister;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transporter_login_register);

        transporterEmail=findViewById(R.id.transporter_email_txt);
        transporterPassword=findViewById(R.id.transporter_password_txt);
        noAccount=findViewById(R.id.transporter_register_txt);
        transporterLogin=findViewById(R.id.transporter_login_btn);
        transporterRegister=findViewById(R.id.transporter_register);
        transporterStatus=findViewById(R.id.transporter_txt);

        transporterRegister.setVisibility(View.INVISIBLE);
        transporterRegister.setEnabled(true);

        mAuth=FirebaseAuth.getInstance();
        loadingBar=new ProgressDialog(this);


        noAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transporterRegister.setVisibility(View.VISIBLE);
                noAccount.setVisibility(View.INVISIBLE);
                transporterStatus.setText("Register Transporter");

                transporterLogin.setVisibility(View.INVISIBLE);
                transporterLogin.setEnabled(true);
            }
        });

        transporterRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                RegisterTransporter();

            }
        });

        transporterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email=transporterEmail.getText().toString();
                String password= transporterPassword.getText().toString();

                LoginTransporter(email,password);

            }
        });
    }

    private void LoginTransporter(String email, String password)
    {

        if(TextUtils.isEmpty(email)){

            Toast.makeText(TransporterLoginRegisterActivity.this, "Please Input Your Email", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(password)){

            Toast.makeText(TransporterLoginRegisterActivity.this, "Please Input Your Password", Toast.LENGTH_SHORT).show();
        }

        else{

            loadingBar.setTitle("Transporter Login");
            loadingBar.setMessage("please wait while we check your credentials..");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Intent intent= new Intent(TransporterLoginRegisterActivity.this, TransportersMapActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                loadingBar.dismiss();
                                finish();

                            }

                            else{

                                Toast.makeText(TransporterLoginRegisterActivity.this,"Please Complete the Registration Form",Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

        }


        }



    private void RegisterTransporter()
    {

        String email=transporterEmail.getText().toString();
        String password= transporterPassword.getText().toString();

        if(TextUtils.isEmpty(email)){

            Toast.makeText(TransporterLoginRegisterActivity.this, "Please Input Your Email", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(password)){

            Toast.makeText(TransporterLoginRegisterActivity.this, "Please Input Your Password", Toast.LENGTH_SHORT).show();
        }

        else{

            loadingBar.setTitle("Transporter Registration");
            loadingBar.setMessage("please wait while we register your data..");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                       if(task.isSuccessful()){

                           final DatabaseReference rootRef;
                           rootRef= FirebaseDatabase.getInstance().getReference();

                           String Tid= mAuth.getCurrentUser().getUid();

                           HashMap<String,Object> TransporterMap= new HashMap<>();
                           TransporterMap.put("Tid",Tid);
                           TransporterMap.put("email",email);
                           TransporterMap.put("password",password);

                           rootRef.child("Transporters").child(Tid).updateChildren(TransporterMap)

                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task)
                                       {
                                           loadingBar.dismiss();
                                           Toast.makeText(TransporterLoginRegisterActivity.this,"You are Registered Successfully",Toast.LENGTH_SHORT).show();
                                           Intent intent= new Intent(TransporterLoginRegisterActivity.this, TransportersMapActivity.class);
                                           intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                           startActivity(intent);
                                           finish();

                                       }
                                   });

                       }

                       else{

                           Toast.makeText(TransporterLoginRegisterActivity.this, "Email already Exists...Please try another email ", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                       }                        }
                    });

        }


    }
}
