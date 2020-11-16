package com.example.finalmkulima.Farmer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.finalmkulima.HomeActivity;
import com.example.finalmkulima.MainActivity;
import com.example.finalmkulima.R;

public class AdminCategoryActivity extends AppCompatActivity {

    private ImageView fruits, meat;
    private ImageView vegetables, animals;

    private Button logoutButton, checkOrdersButton, maintainProductsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_category);

        fruits = (ImageView) findViewById(R.id.fruits);
        meat = (ImageView) findViewById(R.id.meat);
        vegetables = (ImageView) findViewById(R.id.vegetables);
        animals = (ImageView) findViewById(R.id.animals);

        logoutButton = (Button) findViewById(R.id.admin_logout_btn);
        checkOrdersButton = (Button) findViewById(R.id.check_orders_btn);
        maintainProductsButton = (Button) findViewById(R.id.maintain_btn);

        maintainProductsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AdminCategoryActivity.this, HomeActivity.class);
                intent.putExtra("Admin", "Admin");

                startActivity(intent);

            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AdminCategoryActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                finish();

            }
        });

        checkOrdersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminCategoryActivity.this, FarmerNewOrdersActivity.class);
                startActivity(intent);


            }
        });


        fruits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminCategoryActivity.this, AdminAddNewProduct.class);
                intent.putExtra("category", "fruits");
                startActivity(intent);
            }
        });

        meat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminCategoryActivity.this, AdminAddNewProduct.class);
                intent.putExtra("category", "meat");
                startActivity(intent);
            }
        });

        vegetables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminCategoryActivity.this, AdminAddNewProduct.class);
                intent.putExtra("category", "vegetables");
                startActivity(intent);
            }
        });

        animals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminCategoryActivity.this, AdminAddNewProduct.class);
                intent.putExtra("category", "animals");
                startActivity(intent);
            }
        });


    }
}
