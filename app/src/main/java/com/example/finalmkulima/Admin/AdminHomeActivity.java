package com.example.finalmkulima.Admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.finalmkulima.Buyers.HomeActivity;
import com.example.finalmkulima.Buyers.MainActivity;
import com.example.finalmkulima.Farmers.FarmerNewOrdersActivity;
import com.example.finalmkulima.R;

public class AdminHomeActivity extends AppCompatActivity {

     private Button logoutButton, checkOrdersButton, maintainProductsButton,checkApprovedProductsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        logoutButton = (Button) findViewById(R.id.admin_logout_btn);
        //checkOrdersButton = (Button) findViewById(R.id.check_orders_btn);
        //maintainProductsButton = (Button) findViewById(R.id.maintain_btn);
        checkApprovedProductsBtn=findViewById(R.id.check_approve_products_btn);

//        maintainProductsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent intent = new Intent(AdminHomeActivity.this, HomeActivity.class);
//                intent.putExtra("Admin", "Admin");
//
//                startActivity(intent);
//
//            }
//        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AdminHomeActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                finish();

            }
        });

//        checkOrdersButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(AdminHomeActivity.this, FarmerNewOrdersActivity.class);
//                startActivity(intent);
//
//
//            }
//        });

        checkApprovedProductsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHomeActivity.this, AdminCheckNewProductsActivity.class);
                startActivity(intent);


            }
        });


    }
}
