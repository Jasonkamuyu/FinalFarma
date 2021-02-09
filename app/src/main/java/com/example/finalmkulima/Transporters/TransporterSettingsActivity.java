package com.example.finalmkulima.Transporters;

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

import com.example.finalmkulima.Farmers.FarmerSettingsActivity;
import com.example.finalmkulima.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TransporterSettingsActivity extends AppCompatActivity {

    private EditText FarmerName,FarmerPhone,carField;
    private Button FarmerConfirm,FarmerBack;

    private FirebaseAuth mAuth;
    private DatabaseReference mTransporterDatabase;
    private ImageView profileImage;

    private String myUrl = "";

    private StorageTask uploadTask;

    private String userID;
    private String name;
    private String Phone;
    private String car;
    private String mProfileImageUrl;

    private Uri resultUri,ImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transporter_settings);

        FarmerName=findViewById(R.id.Farmer_name);
        FarmerPhone=findViewById(R.id.Farmer_Phone);
        FarmerConfirm=findViewById(R.id.confirm);
        FarmerBack=findViewById(R.id.back);
        profileImage=findViewById(R.id.profile_picture);
        carField=findViewById(R.id.car);

        mAuth= FirebaseAuth.getInstance();
        userID=mAuth.getCurrentUser().getUid();

        mTransporterDatabase= FirebaseDatabase.getInstance().getReference().child("Transporters").child(userID);

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
        car=carField.getText().toString();


        Map userInfo= new HashMap();
        userInfo.put("name",name);
        userInfo.put("phone",Phone);
        userInfo.put("car",car);

        mTransporterDatabase.updateChildren(userInfo);



        if(resultUri!=null){
            StorageReference filePath= FirebaseStorage.getInstance().getReference().child("Transporter_Profile_Images").child(userID);
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
                                    .start(TransporterSettingsActivity.this);


                            Map newImage= new HashMap();
                            newImage.put("profilepictureUrl",downloadUrl.toString());
                            mTransporterDatabase.updateChildren(newImage);

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
        mTransporterDatabase.addValueEventListener(new ValueEventListener() {
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

                    if (map.get("car")!=null) {

                        car=map.get("car").toString();
                        carField.setText(car);

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

        if(requestCode==1  &&  resultCode== Activity.RESULT_OK   ){

            final Uri imageUri= data.getData();
            resultUri = imageUri;
            profileImage.setImageURI(resultUri);
        }

//
//            resultUri= imageUri;


        else{
            Toast.makeText(this, "Error, Try Again.", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(TransporterSettingsActivity.this, FarmerSettingsActivity.class));
            finish();

        }


    }


}
