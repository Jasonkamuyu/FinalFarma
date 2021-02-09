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

import com.example.finalmkulima.Buyers.MainActivity;
import com.example.finalmkulima.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class FarmerRegistrationActivity extends AppCompatActivity {

    private Button farmerLoginBegin,registerButton;
    private EditText nameInput,phoneInput,emailInput,passwordInput,addressInput;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private String onlineFarmerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_registration);
        farmerLoginBegin=findViewById(R.id.farmer_already_have_account_btn);
        nameInput=findViewById(R.id.farmer_name);
        phoneInput=findViewById(R.id.farmer_phone);
        emailInput=findViewById(R.id.farmer_email);
        passwordInput=findViewById(R.id.farmer_password);
        addressInput=findViewById(R.id.farmer_address);
        registerButton=findViewById(R.id.farmer_register_btn);

        mAuth=FirebaseAuth.getInstance();
        //onlineFarmerID=mAuth.getCurrentUser().getUid();

        loadingBar = new ProgressDialog(this);


        farmerLoginBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(FarmerRegistrationActivity.this, FarmerLoginActivity.class);
                startActivity(intent);

            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerFarmer();
            }
        });
    }

    private void registerFarmer() {
        String name=nameInput.getText().toString();
        String phone=phoneInput.getText().toString();
        String email=emailInput.getText().toString();
        String password=passwordInput.getText().toString();
        String address=addressInput.getText().toString();

        if(!name.equals("")&& !phone.equals("")  &&!email.equals("")  &&!password.equals("") &&!address.equals("")){

            loadingBar.setTitle("Creating Farmer Account");
            loadingBar.setMessage("Please wait, while we are checking the credentials.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(task.isSuccessful()){
                            final DatabaseReference rootRef;
                            rootRef= FirebaseDatabase.getInstance().getReference();

                            String fid= mAuth.getCurrentUser().getUid();

                            HashMap<String,Object>sellerMap= new HashMap<>();
                            sellerMap.put("fid",fid);
                            sellerMap.put("phone",phone);
                            sellerMap.put("email",email);
                            sellerMap.put("address",address);
                            sellerMap.put("name",name);

                            rootRef.child("Farmers").child(fid).updateChildren(sellerMap)

                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            loadingBar.dismiss();
                                            Toast.makeText(FarmerRegistrationActivity.this,"You are Registered Successfully",Toast.LENGTH_SHORT).show();
                                            Intent intent= new Intent(FarmerRegistrationActivity.this, FarmerHomeActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();

                                        }
                                    });

                        }
                    }
                });

        }
    else{

            Toast.makeText(this,"Please Complete the Registration Form",Toast.LENGTH_SHORT).show();

        }    }
}
