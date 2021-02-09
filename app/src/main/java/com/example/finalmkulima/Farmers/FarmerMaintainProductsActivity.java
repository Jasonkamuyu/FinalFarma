package com.example.finalmkulima.Farmers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.finalmkulima.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class FarmerMaintainProductsActivity extends AppCompatActivity {
    private Button applyChangesButton,deleteBtn;
    private EditText name, price, description;
    private ImageView imageView;

    private String productID = "";
    private DatabaseReference productsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_maintain_products);

        productID = getIntent().getStringExtra("pid");
        productsRef = FirebaseDatabase.getInstance().getReference().child("Products").child(productID);

        name = (EditText) findViewById(R.id.product_name_maintain);
        price = (EditText) findViewById(R.id.product_price_maintain);
        description = (EditText) findViewById(R.id.product_description_maintain);

        imageView = (ImageView) findViewById(R.id.product_image_maintain);
        applyChangesButton = (Button) findViewById(R.id.apply_changes_btn);
        deleteBtn = (Button) findViewById(R.id.delete_product_btn);

        displaySpecificProductInfo();

        applyChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyChanges();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteThisProduct();
            }
        });

    }

    private void deleteThisProduct()
    {

        productsRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                Intent intent = new Intent(FarmerMaintainProductsActivity.this, FarmerHomeActivity.class);
                startActivity(intent);
                finish();

                Toast.makeText(FarmerMaintainProductsActivity.this, "The Product is Deleted Successfully", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void applyChanges() {

        String pName = name.getText().toString();
        String pPrice = price.getText().toString();
        String pDescription = description.getText().toString();

        if (pName.equals("")) {

            Toast.makeText(FarmerMaintainProductsActivity.this, "Write down the Name", Toast.LENGTH_SHORT).show();

        } else if (pPrice.equals("")) {

            Toast.makeText(FarmerMaintainProductsActivity.this, "Write down Product Price", Toast.LENGTH_SHORT).show();

        } else if (pDescription.equals("")) {

            Toast.makeText(FarmerMaintainProductsActivity.this, "Write down the Description", Toast.LENGTH_SHORT).show();

        } else {

            HashMap<String, Object> productMap = new HashMap<>();
            productMap.put("pid", productID);
            productMap.put("description", pDescription);
            productMap.put("price", pPrice);
            productMap.put("pname", pName);

            productsRef.updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        Toast.makeText(FarmerMaintainProductsActivity.this, "Changes Applied Successfuly", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(FarmerMaintainProductsActivity.this, FarmerHomeActivity.class);
                        startActivity(intent);
                        finish();


                    }
                }
            });


        }
    }

    private void displaySpecificProductInfo() {

        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String pName = snapshot.child("pname").getValue().toString();
                    String pPrice = snapshot.child("price").getValue().toString();
                    String pDescription = snapshot.child("description").getValue().toString();
                    String pImage = snapshot.child("image").getValue().toString();

                    name.setText(pName);
                    price.setText(pPrice);
                    description.setText(pDescription);
                    Picasso.get().load(pImage).into(imageView);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
