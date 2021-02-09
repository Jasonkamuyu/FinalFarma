package com.example.finalmkulima.Farmers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.finalmkulima.R;

public class FarmerProductCategoryActivity extends AppCompatActivity {

    private ImageView fruits, meat;
    private ImageView vegetables, animals;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_product_category);

        fruits = (ImageView) findViewById(R.id.fruits);
        meat = (ImageView) findViewById(R.id.meat);
        vegetables = (ImageView) findViewById(R.id.vegetables);
        animals = (ImageView) findViewById(R.id.animals);




        fruits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FarmerProductCategoryActivity.this, FarmerAddNewProductActivity.class);
                intent.putExtra("category", "fruits");
                startActivity(intent);
            }
        });

        meat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FarmerProductCategoryActivity.this, FarmerAddNewProductActivity.class);
                intent.putExtra("category", "meat");
                startActivity(intent);
            }
        });

        vegetables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FarmerProductCategoryActivity.this, FarmerAddNewProductActivity.class);
                intent.putExtra("category", "vegetables");
                startActivity(intent);
            }
        });

        animals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FarmerProductCategoryActivity.this, FarmerAddNewProductActivity.class);
                intent.putExtra("category", "animals");
                startActivity(intent);
            }
        });


    }
}
