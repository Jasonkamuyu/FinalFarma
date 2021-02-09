package com.example.finalmkulima.Transporters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalmkulima.Buyers.MainActivity;
import com.example.finalmkulima.Constants;
import com.example.finalmkulima.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.internal.ConnectionCallbacks;
import com.google.android.gms.common.api.internal.OnConnectionFailedListener;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.rey.material.widget.SnackBar;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TransportersMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {

private static final String TAG = "MapsActivity";
private GoogleMap mMap;
private Geocoder geocoder;
private int ACCESS_LOCATION_REQUEST_CODE = 10001;
        FusedLocationProviderClient fusedLocationProviderClient;
        LocationRequest locationRequest;

        Marker TransporterLocationMarker;
        Circle userLocationAccuracyCircle;
        Location mLastLocation;

        private Button LogoutTransporterButton,SettingsTransporterButton;
        private String customerId="";
        private Boolean isLoggingOut=false;

        private LinearLayout mCustomerInfo;

        private ImageView mCustomerProfileImage;

        private TextView mCustomerName,mCustomerPhone,mCustomerDestination;

@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transporters_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this);

        mCustomerInfo=findViewById(R.id.Customer_info);
        mCustomerName=findViewById(R.id.Customer_Name);
        mCustomerPhone=findViewById(R.id.Customer_Phone);
        mCustomerProfileImage=findViewById(R.id.Customer_profile);
        mCustomerDestination=findViewById(R.id.Customer_Destination);
        SettingsTransporterButton=findViewById(R.id.transporter_settings_btn);

    LogoutTransporterButton = findViewById(R.id.transporter_logout_btn);



        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        SettingsTransporterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent= new Intent(TransportersMapActivity.this,TransporterSettingsActivity.class);
                startActivity(intent);
                finish();
                return;

            }
        });

    LogoutTransporterButton.setOnClickListener(v -> {

        isLoggingOut=true;
        disconnectDriver();
        FirebaseAuth.getInstance().signOut();
        Intent intent= new Intent(TransportersMapActivity.this,TransporterLoginRegisterActivity.class);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_LONG).show();
        return;

    });

    getAssignedFarmer();
        }

    private void getAssignedFarmer() {


    String driverId=FirebaseAuth.getInstance().getCurrentUser().getUid();
    DatabaseReference assignedFarmerRef= FirebaseDatabase.getInstance().getReference().child("Transporters").child(driverId).child("FarmerRequests").child("customerRideID");

    assignedFarmerRef.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {

            if(snapshot.exists()){


                    customerId=snapshot.getValue().toString();
                    getAssignedFarmerPickupLocation();
                getAssignedCustomerDestination();
                    getAssignedCustomerInfo();

                }

        else{
            customerId="";
                if (pickuplocationmarker!=null) {
                    pickuplocationmarker.remove();
                }

                if (assignedFarmerPickupLocationRefListener!=null) {

                    assignedFarmerPickupLocationRef.removeEventListener(assignedFarmerPickupLocationRefListener);
                }

                mCustomerInfo.setVisibility(View.GONE);
                mCustomerName.setText("");
                mCustomerPhone.setText("");
                mCustomerDestination.setText("Destination: ");
                mCustomerProfileImage.setImageResource(R.drawable.profile);

            }
        }


        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });


    }

    private void getAssignedCustomerDestination() {

        String driverId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedFarmerRef= FirebaseDatabase.getInstance().getReference().child("Transporters").child(driverId).child("FarmerRequests").child("destination");

        assignedFarmerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){


                   String destination =snapshot.getValue().toString();
                   mCustomerDestination.setText("Destination: "+destination);


                }

                else{
                    mCustomerDestination.setText("Destination: ");




                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    private void getAssignedCustomerInfo() {

    mCustomerInfo.setVisibility(View.VISIBLE);
       DatabaseReference mFarmerDatabase= FirebaseDatabase.getInstance().getReference().child("Farmers").child(customerId);

        mFarmerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()&&snapshot.getChildrenCount()>0){

                    Map<String, Object>map= (Map<String,Object>)snapshot.getValue();

                    if (map.get("name")!=null) {


                        mCustomerName.setText( map.get("name").toString());

                    }

                    if (map.get("phone")!=null) {


                        mCustomerPhone.setText( map.get("phone").toString());

                    }

                    if (map.get("profilepictureUrl")!=null) {



                        Picasso.get().load(map.get("profilepictureUrl").toString()).into(mCustomerProfileImage);
                        //Glide.with(getApplicationContext()).load(mProfileImageUrl).into(profileImage);

                    }


                }            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    Marker pickuplocationmarker;
private  DatabaseReference assignedFarmerPickupLocationRef;
private ValueEventListener assignedFarmerPickupLocationRefListener;

    private void getAssignedFarmerPickupLocation() {

        assignedFarmerPickupLocationRef= FirebaseDatabase.getInstance().getReference().child("FarmerRequests").child(customerId).child("l");

        assignedFarmerPickupLocationRefListener=assignedFarmerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()&&customerId.equals("")){

                    List<Object> map= (List<Object>)snapshot.getValue();
                    double locationLat=0;
                    double locationLng=0;


                    if(map.get(0)!=null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }

                    if(map.get(1)!=null) {

                        locationLng=Double.parseDouble(map.get(1).toString());
                    }

                    LatLng driverLatLng = new LatLng(locationLat,locationLng);

                    pickuplocationmarker=mMap.addMarker(new MarkerOptions().position(driverLatLng).title("pickup location"));
                           }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    /**
 * Manipulates the map once available.
 * This callback is triggered when the map is ready to be used.
 * This is where we can add markers or lines, add listeners or move the camera. In this case,
 * we just add a marker near Sydney, Australia.
 * If Google Play services is not installed on the device, the user will be prompted to install
 * it inside the SupportMapFragment. This method will only be triggered once the user has
 * installed Google Play services and returned to the app.
 */
@Override
public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            enableUserLocation();
//            zoomToUserLocation();
        } else {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
        //We can show user a dialog why this permission is necessary
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
        } else  {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
        }

        }


        }

        LocationCallback locationCallback = new LocationCallback() {
@Override
public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        Log.d(TAG, "onLocationResult: " + locationResult.getLastLocation());
        if (mMap != null) {
        setUserLocationMarker(locationResult.getLastLocation());
        }
        }
        };

private void setUserLocationMarker(Location location) {

    mLastLocation = location;
    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

    if (TransporterLocationMarker == null) {
        //Create a new marker
        MarkerOptions markerOptions1 = new MarkerOptions();
        markerOptions1.position(latLng);
        markerOptions1.icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
        markerOptions1.rotation(location.getBearing());
        markerOptions1.anchor((float) 0.5, (float) 0.5);
        TransporterLocationMarker = mMap.addMarker(markerOptions1);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
    }

//    MarkerOptions markerOptions = new MarkerOptions();
//    markerOptions.position(latLng);
//            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
//            markerOptions.rotation(location.getBearing());
//            markerOptions.anchor((float) 0.5, (float) 0.5);

//    TransporterLocationMarker = mMap.addMarker(markerOptions);
//    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
//    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

    String user_Id= FirebaseAuth.getInstance().getCurrentUser().getUid();

    DatabaseReference refAvailable=FirebaseDatabase.getInstance().getReference().child("driversAvailable");
    DatabaseReference refWorking=FirebaseDatabase.getInstance().getReference().child("driversworking");

    GeoFire geoFireAvailable= new GeoFire(refAvailable);
    GeoFire geoFireWorking= new GeoFire(refWorking);


    switch (customerId){

        case "":
            geoFireWorking.removeLocation(user_Id);
            geoFireAvailable.setLocation(user_Id,new GeoLocation(location.getLatitude(),location.getLongitude()));
            break;

            default:
                geoFireAvailable.removeLocation(user_Id);
                geoFireWorking.setLocation(user_Id,new GeoLocation(location.getLatitude(),location.getLongitude()));
                break;

    }



        }

private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }

private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }

@Override
protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        startLocationUpdates();
        } else {
        // you need to request permissions...
        }
        }

        private void disconnectDriver(){

            String user_Id= FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref=FirebaseDatabase.getInstance().getReference().child("driversAvailable");
            GeoFire geoFire= new GeoFire(ref);
            geoFire.removeLocation(user_Id  );
        }



@Override
protected void onStop() {
        super.onStop();

    if (!isLoggingOut) {
        disconnectDriver();

        stopLocationUpdates();
    }


        }

private void enableUserLocation() {
        mMap.setMyLocationEnabled(true);
        }

private void zoomToUserLocation() {
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
@Override
public void onSuccess(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
//                mMap.addMarker(new MarkerOptions().position(latLng));
        }
        });
        }

@Override
public void onMapLongClick(LatLng latLng) {
        Log.d(TAG, "onMapLongClick: " + latLng.toString());
        try {
        List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        if (addresses.size() > 0) {
        Address address = addresses.get(0);
        String streetAddress = address.getAddressLine(0);
        mMap.addMarker(new MarkerOptions()
        .position(latLng)
        .title(streetAddress)
        .draggable(true)
        );
        }
        } catch (IOException e) {
        e.printStackTrace();
        }
        }

@Override
public void onMarkerDragStart(Marker marker) {
        Log.d(TAG, "onMarkerDragStart: ");
        }

@Override
public void onMarkerDrag(Marker marker) {
        Log.d(TAG, "onMarkerDrag: ");
        }

@Override
public void onMarkerDragEnd(Marker marker) {
        Log.d(TAG, "onMarkerDragEnd: ");
        LatLng latLng = marker.getPosition();
        try {
        List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        if (addresses.size() > 0) {
        Address address = addresses.get(0);
        String streetAddress = address.getAddressLine(0);
        marker.setTitle(streetAddress);
        }
        } catch (IOException e) {
        e.printStackTrace();
        }
        }

@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        enableUserLocation();
        zoomToUserLocation();
        } else {

            Toast.makeText(getApplicationContext(), "Please Provide Persmission", Toast.LENGTH_SHORT).show();
        //We can show a dialog that permission is not granted...
        }
        }
        }
        }

