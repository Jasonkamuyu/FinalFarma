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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalmkulima.Buyers.HomeActivity;
import com.example.finalmkulima.Model.FarmerOrders;
import com.example.finalmkulima.R;
import com.example.finalmkulima.Transporters.TransportersMapActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FarmerNewOrdersActivity extends AppCompatActivity {

    private Button report_button;
    private RecyclerView ordersList;
    private DatabaseReference ordersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_new_orders);

        ordersRef= FirebaseDatabase.getInstance().getReference().child("Orders");
        report_button=findViewById(R.id.report_button);

        ordersList=findViewById(R.id.orders_list);
        ordersList.setLayoutManager(new LinearLayoutManager(this));


        report_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newintent= new Intent (FarmerNewOrdersActivity.this, ReportActivity.class);
                startActivity(newintent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<FarmerOrders>options=new FirebaseRecyclerOptions.Builder<FarmerOrders>()
                .setQuery(ordersRef,FarmerOrders.class)
                .build();

        FirebaseRecyclerAdapter<FarmerOrders,FarmerOrdersViewHolder>adapter=
                new FirebaseRecyclerAdapter<FarmerOrders, FarmerOrdersViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FarmerOrdersViewHolder holder, int position, @NonNull FarmerOrders model) {

                        holder.userName.setText(model.getName());
                        holder.userPhone.setText(model.getPhone());
                        holder.userTotalPrice.setText("Total Amount= KSH"+model.getTotalAmount());
                        holder.userDateTime.setText("Order at "+model.getDate()+" "+model.getTime());
                        holder.userShippingAddress.setText("Shipping Address"+model.getAddress()+","+model.getCity());

                        holder.showOrdersButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                String uID =getRef(position).getKey();
                                Intent intent= new Intent(FarmerNewOrdersActivity.this, FarmerUserProductsActivity.class);
                                intent.putExtra("uid",uID);
                                startActivity(intent);
                            }
                        });

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[]=new CharSequence[]{

                                        "Yes",
                                        "No"
                                };

                                AlertDialog.Builder builder= new AlertDialog.Builder(FarmerNewOrdersActivity.this);
                                builder.setTitle("Have You Shipped These Products?");

                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(which==0){

                                            String uID =getRef(position).getKey();
                                            RemoveOrder(uID);

                                        }

                                        else{

                                           Intent callTransporter= new Intent(FarmerNewOrdersActivity.this, FarmersMapActivity.class);
                                            callTransporter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(callTransporter);
                                            Toast.makeText(FarmerNewOrdersActivity.this, "Choose a Transporter to Transport the Product ", Toast.LENGTH_LONG).show();
                                            finish();

                                        }

                                    }
                                });

                                builder.show();
                            }
                        });


                    }

                    @NonNull
                    @Override
                    public FarmerOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                       View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.orders_layout,parent,false);
                       return new FarmerOrdersViewHolder(view);
                    }
                };

        ordersList.setAdapter(adapter);
        adapter.startListening();
    }



    public static class FarmerOrdersViewHolder extends RecyclerView.ViewHolder{

        public TextView userName, userPhone, userTotalPrice, userDateTime, userShippingAddress;
        public Button showOrdersButton;
        public FarmerOrdersViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.order_user_name);
            userPhone=itemView.findViewById(R.id.order_phone_number);
            userTotalPrice=itemView.findViewById(R.id.order_total_price);
            userDateTime=itemView.findViewById(R.id.order_date_time);
            userShippingAddress=itemView.findViewById(R.id.order_address_city);
            showOrdersButton=itemView.findViewById(R.id.show_all_products_btn);
        }
    }

    private void RemoveOrder(String uID) {

        ordersRef.child(uID).removeValue();
    }
}
