package com.example.finalmkulima.Farmers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.finalmkulima.Buyers.SettingsActivity;
import com.example.finalmkulima.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FarmerSettingsActivity extends AppCompatActivity {

    private EditText FarmerName,FarmerPhone;
    private Button FarmerConfirm,FarmerBack;

    private FirebaseAuth mAuth;
    private DatabaseReference mFarmerDatabase;
    private ImageView profileImage;

    private String myUrl = "";

    private StorageTask uploadTask;

    private String userID;
    private String name;
    private String Phone;
    private String mProfileImageUrl;

    private Uri resultUri,ImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_settings);

        FarmerName=findViewById(R.id.Farmer_name);
        FarmerPhone=findViewById(R.id.Farmer_Phone);
        FarmerConfirm=findViewById(R.id.confirm);
        FarmerBack=findViewById(R.id.back);
        profileImage=findViewById(R.id.profile_picture);

        mAuth= FirebaseAuth.getInstance();
        userID=mAuth.getCurrentUser().getUid();

        mFarmerDatabase= FirebaseDatabase.getInstance().getReference().child("Farmers").child(userID);

        getUserInfo();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });

        FarmerConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFarmerInformation();
            }
        });

        FarmerBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }

    private void updateFarmerInformation() {

        name=FarmerName.getText().toString();
        Phone=FarmerPhone.getText().toString();


        Map userInfo= new HashMap();
        userInfo.put("name",name);
        userInfo.put("phone",Phone);

        mFarmerDatabase.updateChildren(userInfo);



        if(resultUri!=null){
            StorageReference filePath= FirebaseStorage.getInstance().getReference().child("Farmer_Profile_Images").child(userID);
            Bitmap bitmap= null;

            try {
                bitmap= MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream boas= new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,20,boas);

            byte[] data= boas.toByteArray();
            UploadTask uploadTask= filePath.putBytes(data);

           // uploadTask = filePath.putFile(ImageUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception
                {
                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                    }

                    return filePath.getDownloadUrl();
                }
            })
                    .addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            CropImage.activity(resultUri)
                                    .setAspectRatio(1, 1)
                                    .start(FarmerSettingsActivity.this);


                            Map newImage= new HashMap();
                            newImage.put("profilepictureUrl",downloadUrl.toString());
                            mFarmerDatabase.updateChildren(newImage);

                            Toast.makeText(this, "Image Successfully Updated", Toast.LENGTH_SHORT).show();

                            finish();
                            return;

                        }
                    });




//            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                    Uri downloadUrl= taskSnapshot.getD
//
//                }
//            });

                            }

                            finish();
                        }




    private void getUserInfo(){
        mFarmerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()&&snapshot.getChildrenCount()>0){

                    Map<String, Object>map= (Map<String,Object>)snapshot.getValue();

                    if (map.get("name")!=null) {

                        name=map.get("name").toString();
                        FarmerName.setText(name);

                    }

                    if (map.get("phone")!=null) {

                        Phone=map.get("phone").toString();
                        FarmerPhone.setText(Phone);

                    }

                    if (map.get("profilepictureUrl")!=null) {

                        mProfileImageUrl=map.get("profilepictureUrl").toString();

                        Picasso.get().load(mProfileImageUrl).into(profileImage);
                        //Glide.with(getApplicationContext()).load(mProfileImageUrl).into(profileImage);

                    }


                }            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1  &&  resultCode==Activity.RESULT_OK   ){

            final Uri imageUri= data.getData();
            resultUri = imageUri;
            profileImage.setImageURI(resultUri);
        }

//
//            resultUri= imageUri;


        else{
            Toast.makeText(this, "Error, Try Again.", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(FarmerSettingsActivity.this, FarmerSettingsActivity.class));
            finish();

        }


        }

}