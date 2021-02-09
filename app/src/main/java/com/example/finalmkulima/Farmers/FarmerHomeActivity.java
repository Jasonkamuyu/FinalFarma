package com.example.finalmkulima.Farmers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.finalmkulima.Buyers.HomeActivity;
import com.example.finalmkulima.Buyers.MainActivity;
import com.example.finalmkulima.Model.Products;
import com.example.finalmkulima.R;
import com.example.finalmkulima.ViewHolder.ItemViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FarmerHomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private DatabaseReference unverifiedProductsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_home);

        unverifiedProductsRef= FirebaseDatabase.getInstance().getReference().child("Products");

        recyclerView=findViewById(R.id.farmer_home_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);


        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intentHome=new Intent(FarmerHomeActivity.this, FarmerHomeActivity.class);
                    startActivity(intentHome);


                case R.id.navigation_add:
                    Intent intentCategory=new Intent(FarmerHomeActivity.this, FarmerProductCategoryActivity.class);
                    startActivity(intentCategory);
                    return true;

                case R.id.navigation_update_products:
                    Intent intentUpdate=new Intent(FarmerHomeActivity.this, HomeActivity.class);
                    intentUpdate.putExtra("Farmers", "Farmers");
                    startActivity(intentUpdate);
                    return true;

                case R.id.navigation_view_orders:
                    Intent intentOrders=new Intent(FarmerHomeActivity.this, FarmerNewOrdersActivity.class);
                    startActivity(intentOrders);
                    return true;

                case R.id.navigation_logout:
                    final FirebaseAuth mAuth;
                    mAuth = FirebaseAuth.getInstance();
                    mAuth.signOut();

                    Intent intentMain=new Intent(FarmerHomeActivity.this, MainActivity.class);
                    intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intentMain);
                    finish();
                    return true;

            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Products> options=new FirebaseRecyclerOptions.Builder<Products>()
                .setQuery(unverifiedProductsRef.orderByChild("sid").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid()),Products.class).build();


        FirebaseRecyclerAdapter<Products, ItemViewHolder> adapter =new FirebaseRecyclerAdapter<Products, ItemViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ItemViewHolder holder, int position, @NonNull Products model) {

                holder.txtProductName.setText("Name:  "+model.getPname());
                holder.txtProductDescription.setText("Description:  "+model.getDescription());
                holder.txtProductPrice.setText("Price = " + model.getPrice()+"KSH");
                holder.textProductStatus.setText("Status="+model.getProductState() );

                Picasso.get().load(model.getImage()).into(holder.imageView);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String productID= model.getPid();

                        CharSequence options[] = new CharSequence[]{
                                "Yes",
                                "No"

                        };
                        AlertDialog.Builder builder=new AlertDialog.Builder(FarmerHomeActivity.this);
                        builder.setTitle("Are you sure you want to Delete This Product?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {

                                if(position==0){
                                    DeleteProduct(productID);


                                }

                                if(position==1){



                                }                            }
                        });

                        builder.show();
                    }
                });


            }

            @NonNull
            @Override
            public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.farmer_item_view,parent,false);
                ItemViewHolder holder=new ItemViewHolder(view);
                return holder;

            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();


    }

    private void DeleteProduct(String productID) {

        unverifiedProductsRef.child(productID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(FarmerHomeActivity.this, "The item has been deleted Sucessfully", Toast.LENGTH_SHORT).show();

            }
        });
    }
}