package com.example.finalmkulima;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalmkulima.Model.Cart;
import com.example.finalmkulima.Prevalent.Prevalent;
import com.example.finalmkulima.ViewHolder.CartViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Button NextProcessButton,NextProcessButton2;
    private TextView txttotalAmount,txtmsg1;

    private int overallTotalPrice=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Intent intent= new Intent(this,PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        startService(intent);

        recyclerView = findViewById(R.id.cart_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        NextProcessButton = (Button) findViewById(R.id.next_process_btn);
        NextProcessButton2 = (Button) findViewById(R.id.next_process_btn2);
        txttotalAmount = (TextView) findViewById(R.id.total_price);
        txtmsg1=(TextView)findViewById(R.id.msg1);

        NextProcessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txttotalAmount.setText("Total Price= KSH"+String.valueOf(overallTotalPrice));
                Intent intent= new Intent(CartActivity.this,ConfirmFinalOrderActivity.class);
                intent.putExtra("Total Price",String.valueOf(overallTotalPrice));
                startActivity(intent);
                finish();
            }
        });

        NextProcessButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payPalPayment();
            }
        });
    }

    private int PAYPAL_REQUEST_CODE=1;
    private static PayPalConfiguration config=new PayPalConfiguration().environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(PayPalConfig.PAYPAL_CLIENT_ID);

    private void payPalPayment() {

        PayPalPayment payment=new PayPalPayment(new BigDecimal(overallTotalPrice),"USD","Total Price",
                PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent= new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payment);

        startActivityForResult(intent,PAYPAL_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PAYPAL_REQUEST_CODE){
            if(resultCode== Activity.RESULT_OK){

                PaymentConfirmation confirm=data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if(confirm!=null){

                    try{

                        JSONObject jsonObject= new JSONObject(confirm.toJSONObject().toString());

                        String paymentResponse=jsonObject.getJSONObject("response").getString("state");

                        if(paymentResponse.equals("aprooved")){
                            Toast.makeText(getApplicationContext(),"Payment successful",Toast.LENGTH_LONG).show();



                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

            else{

                Toast.makeText(getApplicationContext(),"Payment Unsuccessful",Toast.LENGTH_LONG).show();
                DatabaseReference PaymentRef = FirebaseDatabase.getInstance().getReference();
                PaymentRef.child("payment").setValue(true);



            }
        }    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this,PayPalService.class));
        super.onDestroy();

    }

    @Override
    protected void onStart() {
        super.onStart();
        CheckOrderState();

        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");
        FirebaseRecyclerOptions<Cart> options = new FirebaseRecyclerOptions.Builder<Cart>()
                .setQuery(cartListRef.child("User View").child(Prevalent.currentOnlineUser.getPhone()).child("Products"), Cart.class).build();


        FirebaseRecyclerAdapter<Cart, CartViewHolder>adapter =
        new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout,parent,false);
                CartViewHolder holder= new CartViewHolder(view);
                return holder;
            }

            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder holder, int position, @NonNull Cart model) {

                holder.txtProductQuantity.setText("Quantity="+model.getQuantity());
                holder.txtProductPrice.setText("Price"+model.getPrice()+"KSH");
                holder.txtProductName.setText(model.getPname());

                int oneTypeProductPrice=((Integer.valueOf(model.getPrice()))) * Integer.valueOf(model.getQuantity());
                overallTotalPrice=overallTotalPrice+oneTypeProductPrice;

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        CharSequence options[] = new CharSequence[]{

                                "Edit",
                                "Delete"
                        };
                        AlertDialog.Builder builder= new AlertDialog.Builder(CartActivity.this);
                        builder.setTitle("Cart Options:");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                           if(which==0){

                               Intent intent= new Intent(CartActivity.this,ProductDetailsActivity.class);
                               intent.putExtra("pid",model.getPid());
                               startActivity(intent);

                           }
                            if(which==1){

                                cartListRef.child("User View").child(Prevalent.currentOnlineUser.getPhone()).child("Products").child(model.getPid())
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){

                                            Toast.makeText(CartActivity.this,"Item Deleted Successfully",Toast.LENGTH_SHORT).show();
                                            Intent intent= new Intent(CartActivity.this,HomeActivity.class);
                                            startActivity(intent);

                                        }
                                    }
                                });

                            }                            }
                        });

                        builder.show();
                    }
                });


            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }



    private void CheckOrderState(){
        DatabaseReference ordersRef;
        ordersRef= FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentOnlineUser.getPhone());
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String shippingStatus=snapshot.child("state").getValue().toString();
                    String userName=snapshot.child("name").getValue().toString();

                    if(shippingStatus.equals("shipped")){

                        txttotalAmount.setText("Dear "+userName+"\n order is shipped successfully.");
                        recyclerView.setVisibility(View.GONE);

                        txtmsg1.setVisibility(View.VISIBLE);
                        txtmsg1.setText("Your Order Has Been Shipped Successfuly. You Will Soon Receieve Your Order \n PROCEED TO MAKE PAYMENT");
                        NextProcessButton.setVisibility(View.GONE);
                        NextProcessButton2.setVisibility(View.VISIBLE);

                        Toast.makeText(CartActivity.this,"You can Purchase more items Once You Receive your final order",Toast.LENGTH_LONG).show();

                    }

                    else if(shippingStatus.equals("not shipped")){

                        txttotalAmount.setText("Shipping State= Not Shipped");
                        recyclerView.setVisibility(View.GONE);

                        txtmsg1.setVisibility(View.VISIBLE);
                        NextProcessButton.setVisibility(View.GONE);
                        NextProcessButton2.setVisibility(View.VISIBLE);

                        Toast.makeText(CartActivity.this,"You can Purchase more items Once You Receive your final order",Toast.LENGTH_LONG).show();





                    }
                }            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
