package com.example.finalmkulima.Farmers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.example.finalmkulima.Model.Products;
import com.example.finalmkulima.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ReportActivity extends AppCompatActivity {

    private DatabaseReference ordersRef;
    private TextView textView1,textView2, textView3;
    private ListView mListView;
    private String productID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        textView1=findViewById(R.id.txt_Customer_Phone);
        textView2=findViewById(R.id.txt_Customer_Total);
        textView3=findViewById(R.id.txt1234);



        productID = getIntent().getStringExtra("pid");

        ordersRef= FirebaseDatabase.getInstance().getReference().child("Orders");
       // mListView =(ListView) findViewById(R.id.list_view);

        getOrdersReport(productID);
    }

    private void getOrdersReport(String productID) {

        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("Products");
        productsRef.child(productID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    Products products = snapshot.getValue(Products.class);
                    textView1.setText("Customer Phone Number" + products.getPhone());
                    textView2.setText("Items Price"+ products.getPrice()+"KSH");
                    textView3.setText("Total Income"+products.getPrice()+"KSH");


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
