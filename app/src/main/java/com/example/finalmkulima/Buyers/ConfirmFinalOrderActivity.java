package com.example.finalmkulima.Buyers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.finalmkulima.DarajaApiClient;
import com.example.finalmkulima.Model.AccessToken;
import com.example.finalmkulima.Model.STKPush;
import com.example.finalmkulima.PayPalConfig;
import com.example.finalmkulima.Prevalent.Prevalent;
import com.example.finalmkulima.R;
import com.example.finalmkulima.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.example.finalmkulima.Constants.BUSINESS_SHORT_CODE;
import static com.example.finalmkulima.Constants.CALLBACKURL;
import static com.example.finalmkulima.Constants.PARTYB;
import static com.example.finalmkulima.Constants.PASSKEY;
import static com.example.finalmkulima.Constants.TRANSACTION_TYPE;

public class ConfirmFinalOrderActivity extends AppCompatActivity  {

    private DarajaApiClient mApiClient;
    private ProgressDialog mProgressDialog;
    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private String productID = "";


    @BindView(R.id.etAmount) EditText mAmount;
    @BindView(R.id.etPhone)EditText mPhone;
    @BindView(R.id.confirm_final_order_btn_mpesa)
    Button mPay;



    public EditText addressEditText,cityEditText,nameEditText,phoneEditText;
    private Button confirmButtonBtn;

    private String totalAmount="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_final_order);

        Intent intent= new Intent(this,PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        startService(intent);

        // productID = getIntent().getStringExtra("pid");

        totalAmount = getIntent().getStringExtra("Total Price");
        Toast.makeText(this, "Total Price= KSH" + totalAmount, Toast.LENGTH_LONG).show();

        confirmButtonBtn = (Button) findViewById(R.id.confirm_final_order_btn);
        nameEditText = (EditText) findViewById(R.id.shipment_name);
        phoneEditText = (EditText) findViewById(R.id.shipment_phone_number);
        addressEditText = (EditText) findViewById(R.id.shipment_address);
        cityEditText = (EditText) findViewById(R.id.shipment_city);
        ButterKnife.bind(this);

//        phoneEditText.setEnabled(false);
//        nameEditText.setEnabled(false);
        mAmount.setEnabled(false);

        mApiClient = new DarajaApiClient();
        mProgressDialog = new ProgressDialog(this);
        mApiClient.setIsDebug(true);

        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        DisplayUserDetails(nameEditText, phoneEditText, mPhone);
        DisplayAmount(mAmount);


        confirmButtonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckPaypal();
            }
        });

        mPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckMpesa();


            }
        });
    }

    private void DisplayAmount(EditText mAmount) {
        DatabaseReference reff=FirebaseDatabase.getInstance().getReference().child("Products").child(Prevalent.currentOnlineUser.getPhone());;
        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

//                    String amount=snapshot.child("price").getValue().toString();
                    mAmount.setText(totalAmount);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


        private void DisplayUserDetails(EditText mPhone, EditText nameEditText, EditText phoneEditText) {

        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getPhone());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name=snapshot.child("name").getValue().toString();
                String phone=snapshot.child("phone").getValue().toString();
                String yourphone=snapshot.child("phone").getValue().toString();
                        nameEditText.setText(name);
                        phoneEditText.setText(phone);
                        mPhone.setText(yourphone);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });
    }

    private void getAccessToken() {

        mApiClient.setGetAccessToken(true);
        mApiClient.mpesaService().getAccessToken().enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(@NonNull Call<AccessToken> call, @NonNull Response<AccessToken> response) {

                if (response.isSuccessful()) {
                    mApiClient.setAuthToken(response.body().accessToken);

                }
            }

            @Override
            public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {

            }
        });
    }

//    @Override
//    public void onClick(View v) {
//        if (v== mPay){
//
//
//        }
//    }


    private int PAYPAL_REQUEST_CODE=1;
    private static PayPalConfiguration config=new PayPalConfiguration().environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(PayPalConfig.PAYPAL_CLIENT_ID);

    private void payPalPayment() {

        PayPalPayment payment=new PayPalPayment(new BigDecimal(totalAmount),"USD","Total Price",
                PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent= new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payment);
        startActivityForResult(intent,PAYPAL_REQUEST_CODE);

        if (payment.isProcessable()) {
            confirmPaypalOrder();
        }

    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this,PayPalService.class));
        super.onDestroy();

    }


    private void CheckMpesa() {
        if(TextUtils.isEmpty(nameEditText.getText().toString())){

            Toast.makeText(ConfirmFinalOrderActivity.this,"Please Provide Your Name",Toast.LENGTH_SHORT).show();
        }

       else if(TextUtils.isEmpty(phoneEditText.getText().toString())){

            Toast.makeText(ConfirmFinalOrderActivity.this,"Please Provide Your Number",Toast.LENGTH_SHORT).show();
        }
         else if(TextUtils.isEmpty(addressEditText.getText().toString())){

            Toast.makeText(ConfirmFinalOrderActivity.this,"Please Provide Your Home Address",Toast.LENGTH_SHORT).show();
        }
       else if(TextUtils.isEmpty(cityEditText.getText().toString())){

            Toast.makeText(ConfirmFinalOrderActivity.this,"Please Provide Home City Name",Toast.LENGTH_SHORT).show();
        }
       else{

           getAccessToken();
           String phone_number = mPhone.getText().toString();
           String amount = mAmount.getText().toString();
           performSTKPush(phone_number, amount);

        }
    }


    private void CheckPaypal() {
        if (TextUtils.isEmpty(nameEditText.getText().toString())) {

            Toast.makeText(ConfirmFinalOrderActivity.this, "Please Provide Your Name", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(phoneEditText.getText().toString())) {

            Toast.makeText(ConfirmFinalOrderActivity.this, "Please Provide Your Number", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(addressEditText.getText().toString())) {

            Toast.makeText(ConfirmFinalOrderActivity.this, "Please Provide Your Home Address", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(cityEditText.getText().toString())) {

            Toast.makeText(ConfirmFinalOrderActivity.this, "Please Provide Home City Name", Toast.LENGTH_SHORT).show();
        } else {


            payPalPayment();


        }
    }

    public void confirmMpesaOrder() {

        final String saveCurrentDate,saveCurrentTime;
        Calendar callfordate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(callfordate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(callfordate.getTime());

        final DatabaseReference ordersRef= FirebaseDatabase.getInstance().getReference().child("Orders")
                .child(Prevalent.currentOnlineUser.getPhone());

        HashMap<String,Object>ordersMap=new HashMap<>();

        ordersMap.put("totalAmount", totalAmount);
        ordersMap.put("name", nameEditText.getText().toString());
        ordersMap.put("phone", phoneEditText.getText().toString());
        ordersMap.put("address", addressEditText.getText().toString());
        ordersMap.put("city", cityEditText.getText().toString());
        ordersMap.put("date", saveCurrentDate);
        ordersMap.put("time", saveCurrentTime);
        ordersMap.put("state","not shipped");

        ordersRef.updateChildren(ordersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    FirebaseDatabase.getInstance().getReference().child("Cart List")
                            .child("User View").child(Prevalent.currentOnlineUser.getPhone())
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

//                                        confirmMpesaOrder();
//
                                        Toast.makeText(ConfirmFinalOrderActivity.this,"Your Order has been Placed Successfully",Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(ConfirmFinalOrderActivity.this, CartActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();


                                    }
                                }
                            });

                }            }
        });




    }


    public void confirmPaypalOrder() {

       final String saveCurrentDate,saveCurrentTime;

        Calendar callfordate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(callfordate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(callfordate.getTime());

        final DatabaseReference ordersRef= FirebaseDatabase.getInstance().getReference().child("Orders")
                .child(Prevalent.currentOnlineUser.getPhone());

        HashMap<String,Object>ordersMap=new HashMap<>();

        ordersMap.put("totalAmount", totalAmount);
        ordersMap.put("name", nameEditText.getText().toString());
        ordersMap.put("phone", phoneEditText.getText().toString());
        ordersMap.put("address", addressEditText.getText().toString());
        ordersMap.put("city", cityEditText.getText().toString());
        ordersMap.put("date", saveCurrentDate);
        ordersMap.put("time", saveCurrentTime);
        ordersMap.put("state","not shipped");

        ordersRef.updateChildren(ordersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    FirebaseDatabase.getInstance().getReference().child("Cart List")
                            .child("User View").child(Prevalent.currentOnlineUser.getPhone())
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        Toast.makeText(ConfirmFinalOrderActivity.this,"Your Order has been Placed Successfully",Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(ConfirmFinalOrderActivity.this, PaymentActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();


                                    }
                                }
                            });

                }            }
        });



    }


    public void performSTKPush(String phone_number,String amount) {
        mProgressDialog.setMessage("Processing your request");
        mProgressDialog.setTitle("Please Wait...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        String timestamp = Utils.getTimestamp();
        STKPush stkPush = new STKPush(
                BUSINESS_SHORT_CODE,
                Utils.getPassword(BUSINESS_SHORT_CODE, PASSKEY, timestamp),
                timestamp,
                TRANSACTION_TYPE,
                String.valueOf(amount),
                Utils.sanitizePhoneNumber(phone_number),
                PARTYB,
                Utils.sanitizePhoneNumber(phone_number),
                CALLBACKURL,
                "Mkulia App", //Account reference
                "Testing"  //Transaction description
        );

        mApiClient.setGetAccessToken(false);

        //Sending the data to the Mpesa API, remember to remove the logging when in production.
        mApiClient.mpesaService().sendPush(stkPush).enqueue(new Callback<STKPush>() {
            @Override
            public void onResponse(@NonNull Call<STKPush> call, @NonNull Response<STKPush> response) {
                mProgressDialog.dismiss();
                try {
                    if (response.isSuccessful()) {
                        Timber.d("post submitted to API. %s", response.body());


                          confirmMpesaOrder();
//                        Intent intent=new Intent(ConfirmFinalOrderActivity.this, CartActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(intent);
//                        finish();
                    }

                    else {
                        Timber.e("Response %s", response.errorBody().string());
                    }



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<STKPush> call, @NonNull Throwable t) {
                mProgressDialog.dismiss();
                Timber.e(t);
            }
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


}
