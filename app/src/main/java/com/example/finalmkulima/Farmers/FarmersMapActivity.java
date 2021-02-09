package com.example.finalmkulima.Farmers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
//import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esotericsoftware.kryo.NotNull;
import com.example.finalmkulima.Buyers.MainActivity;
import com.example.finalmkulima.Buyers.SettingsActivity;
import com.example.finalmkulima.Constants;
import com.example.finalmkulima.R;
import com.example.finalmkulima.Transporters.TransporterLoginRegisterActivity;
import com.example.finalmkulima.Transporters.TransportersMapActivity;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FarmersMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {

    private static final String TAG = "TransporterMapsActivity";
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private Geocoder geocoder;


    private int ACCESS_LOCATION_REQUEST_CODE = 10001;
    private Button FarmerSettingsButton, FarmerCallTransporterButton;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    Marker userLocationMarker;
    Circle userLocationAccuracyCircle;
    Location mLastLocation;

    private LatLng pickupLocation;
    private Button LogoutFarmerButton;

    private String destination;

    private LinearLayout mTransporterInfo;

    private ImageView mTransporterProfileImage;

    private TextView mTransporterName, mTransporterPhone, mTransporterCar;

    //SupportMapFragment mapFragment;
//    LocationRequest locationRequest;

    private Boolean requestBol = false;
    private Marker PickupMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmers_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.farmers_map);
        mapFragment.getMapAsync(this);


        geocoder = new Geocoder(this);

        LogoutFarmerButton = findViewById(R.id.farmer_logout);
        FarmerCallTransporterButton = findViewById(R.id.call_ride);
        FarmerSettingsButton = findViewById(R.id.farmer_settings);

        mTransporterName=findViewById(R.id.Driver_Name);
        mTransporterPhone=findViewById(R.id.Driver_Phone);
        mTransporterCar=findViewById(R.id.Driver_Car);
        mTransporterInfo=findViewById(R.id.Transporter_info);
        mTransporterProfileImage=findViewById(R.id.Transporter_profile);


        if (!Places.isInitialized()) {

            Places.initialize(getApplicationContext(), "AIzaSyDSDIJxA6HUiuskt5BS1x-WE1DiMFH1tbI");
        }

        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

        // Start the autocomplete intent.
        Intent intent1 = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent1, AUTOCOMPLETE_REQUEST_CODE);


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        FarmerCallTransporterButton.setOnClickListener(v -> {

            if (requestBol) {

                requestBol = false;

                geoQuery.removeAllListeners();
                driverLocationRef.removeEventListener(driverLocationRefListener);

                if (driverFoundID != null) {
                    DatabaseReference driverref = FirebaseDatabase.getInstance().getReference().child("Transporters").child(driverFoundID).child("FarmerRequests");
                    driverref.removeValue();
                    driverFoundID = null;


                }

                driverFound = false;
                radius = 1;

                String user_Id = FirebaseAuth.getInstance().getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("FarmerRequests");

                GeoFire geoFire = new GeoFire(ref);
                geoFire.removeLocation(user_Id);

                if (PickupMarker != null) {
                    PickupMarker.remove();

                    if (mDriverMarker != null) {
                        mDriverMarker.remove();

                    }
                    FarmerCallTransporterButton.setText("Call Transporter");

                    mTransporterInfo.setVisibility(View.GONE);
                    mTransporterName.setText("");
                    mTransporterPhone.setText("");
                    mTransporterCar.setText("");
                    mTransporterProfileImage.setImageResource(R.drawable.profile);
                }


            } else {
                requestBol = true;
                String user_Id = FirebaseAuth.getInstance().getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("FarmerRequests");

                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(user_Id, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                PickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup From Here"));

                FarmerCallTransporterButton.setText("Searching for Transporter...");

                GetClosestTransporter();
            }

        });

        FarmerSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FarmersMapActivity.this, FarmerSettingsActivity.class);
                startActivity(intent);
                return;
            }
        });


        LogoutFarmerButton.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(FarmersMapActivity.this, FarmerRegistrationActivity.class);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_LONG).show();
            return;

        });

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));


        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.

                destination = place.getName().toString();
            }


            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });
    }

    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundID;


    GeoQuery geoQuery;

    private void GetClosestTransporter() {

        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);

        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestBol) {
                    driverFound = true;
                    driverFoundID = key;

                    DatabaseReference driverref = FirebaseDatabase.getInstance().getReference().child("Transporters").child(driverFoundID).child("FarmerRequests");
                    String customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    HashMap map = new HashMap();
                    map.put("customerRideID", customerID);
                    map.put("destination", destination);
                    driverref.updateChildren(map);


                    getDriverLocation();
                    getTransporterInfo();
                    FarmerCallTransporterButton.setText("Finding Transporter Location...");
                }


            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound) {
                    radius++;
                    GetClosestTransporter();

                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void getTransporterInfo() {

        mTransporterInfo.setVisibility(View.VISIBLE);
        DatabaseReference mFarmerDatabase= FirebaseDatabase.getInstance().getReference().child("Transporters").child(driverFoundID);

        mFarmerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()&&snapshot.getChildrenCount()>0){

                    Map<String, Object> map= (Map<String,Object>)snapshot.getValue();

                    if (map.get("name")!=null) {


                        mTransporterName.setText( map.get("name").toString());

                    }

                    if (map.get("phone")!=null) {


                        mTransporterPhone.setText( map.get("phone").toString());

                    }

                    if (map.get("car")!=null) {


                        mTransporterCar.setText( map.get("car").toString());

                    }

                    if (map.get("profilepictureUrl")!=null) {



                        Picasso.get().load(map.get("profilepictureUrl").toString()).into(mTransporterProfileImage);
                        //Glide.with(getApplicationContext()).load(mProfileImageUrl).into(profileImage);

                    }


                }            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;

    private void getDriverLocation() {
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversworking").child(driverFoundID).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && requestBol) {

                    List<Object> map = (List<Object>) snapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;

                    FarmerCallTransporterButton.setText("Transporter Found: ");

                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }

                    if (map.get(1) != null) {

                        locationLng = Double.parseDouble(map.get(1).toString());
                    }

                    LatLng driverLatLng = new LatLng(locationLat, locationLng);
                    if (mDriverMarker != null) {
                        mDriverMarker.remove();

                    }

                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance < 100) {

                        FarmerCallTransporterButton.setText("Transporter Here");


                    } else {
                        FarmerCallTransporterButton.setText("Transporter Found: " + String.valueOf(distance));
                    }


                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("your Transporter"));

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
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
            }

        }


        // Add a marker at Taj Mahal and move the camera
//        LatLng latLng = new LatLng(27.1751, 78.0421);
//        MarkerOptions markerOptions = new MarkerOptions()
//                                            .position(latLng)
//                                            .title("Taj Mahal")
//                                            .snippet("Wonder of the world!");
//        mMap.addMarker(markerOptions);
//        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
//        mMap.animateCamera(cameraUpdate);

        try {
            List<Address> addresses = geocoder.getFromLocationName("london", 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                LatLng london = new LatLng(address.getLatitude(), address.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(london)
                        .title(address.getLocality());
                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(london, 16));
            }
        } catch (IOException e) {
            e.printStackTrace();
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


        if (userLocationMarker == null) {
//            //Create a new marker
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
//            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
//            markerOptions.rotation(location.getBearing());
//            markerOptions.anchor((float) 0.5, (float) 0.5);
            userLocationMarker = mMap.addMarker(markerOptions);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        }
//
//        else  {
//            //use the previously created marker
//
//           userLocationMarker.setPosition(latLng);
//           userLocationMarker.setRotation(location.getBearing());
//           mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
//
//
//
//       }

//        if (userLocationAccuracyCircle == null) {
//            CircleOptions circleOptions = new CircleOptions();
//            circleOptions.center(latLng);
//            circleOptions.strokeWidth(4);
//            circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
//            circleOptions.fillColor(Color.argb(32, 255, 0, 0));
//            circleOptions.radius(location.getAccuracy());
//            userLocationAccuracyCircle = mMap.addCircle(circleOptions);
//        } else {
//            userLocationAccuracyCircle.setCenter(latLng);
//            userLocationAccuracyCircle.setRadius(location.getAccuracy());
//        }
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

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
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
                //We can show a dialog that permission is not granted...
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}
// , GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,com.google.android.gms.location.LocationListener {
//
//    private GoogleMap mMap;
//    GoogleApiClient mGoogleApiClient;
//    Location mLastLocation;
//    LocationRequest mLocationRequest;
//    FusedLocationProviderClient fusedLocationProviderClient;
////    private LatLng CustomerPickUpLocation;
////    private int radius = 1;
////    private Boolean transporterfound = false;
////    private String transporterfoundID;
////
////
////    private Geocoder geocoder;
////    private int ACCESS_LOCATION_REQUEST_CODE = 10001;
////    FusedLocationProviderClient fusedLocationProviderClient;
////
////    private Button FarmerSettingsButton, FarmerLogoutButton, FarmerCallTransporterButton;
////    private FirebaseAuth mAuth;
////    private FirebaseUser currentUser;
////    private DatabaseReference customerDatabaseReff;
////    LocationResult lastLocation;
////   // LocationRequest locationRequest;
////    private String farmerID;
////    private DatabaseReference DriverAvailableReff,LocationDriverReff;
////    private DatabaseReference TransporterReff;
////    private DatabaseReference DriverLocationReff,FarmerLocationReff,FarmerDatabaseReff;
////    Marker DriverMarker;
////    private DatabaseReference  currentUserReff,onlineReff;
////    Location lastlocation;
////
////    private com.google.android.gms.location.LocationCallback locationCallback;
////
//   SupportMapFragment mapFragment;
////    LocationRequest locationRequest;
////
////    @Override
////    protected void onDestroy() {
////        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
////        geoFire.removeLocation(mAuth.getInstance().getCurrentUser().getUid());
////        onlineReff.removeEventListener(onlineValueEventListener);
////        super.onDestroy();
////    }
////
////
////    @Override
////    protected void onResume() {
////        super.onResume();
////       // registerOnlineSystem();
////    }
//
////    private void registerOnlineSystem() {
////        onlineReff.addValueEventListener(onlineValueEventListener);}
////
////    GeoFire geoFire;
////    ValueEventListener onlineValueEventListener = new ValueEventListener() {
////        @Override
////        public void onDataChange(@NonNull DataSnapshot snapshot) {
////            if (snapshot.exists())
////                currentUserReff.onDisconnect().removeValue();
////
////        }
////
////        @Override
////        public void onCancelled(@NonNull DatabaseError error) {
////        }
////    };
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_farmers_map);
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//
//
//        mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.farmers_map);
//        mapFragment.getMapAsync(this);
//
////        mAuth = FirebaseAuth.getInstance();
////        currentUser=mAuth.getCurrentUser();
////
////        //geoFire = new GeoFire(currentUserReff);
////        geocoder = new Geocoder(this);
////        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
////
////        FarmerSettingsButton = findViewById(R.id.farmer_settings);
////        FarmerLogoutButton = findViewById(R.id.farmer_logout);
////        FarmerCallTransporterButton = findViewById(R.id.call_ride);
////
////        farmerID=FirebaseAuth.getInstance().getCurrentUser().getUid();
////
////
////
////
////        init();
////
//////        LocationManger lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//////        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
//////            @Override
//////            public void onLocationChanged(Location location) {
//////                // TODO Auto-generated method stub
//////            }
//////            @Override
//////            public void onProviderDisabled(String provider) {
//////                // TODO Auto-generated method stub
//////            }
//////            @Override
//////            public void onProviderEnabled(String provider) {
//////                // TODO Auto-generated method stub
//////            }
//////            @Override
//////            public void onStatusChanged(String provider, int status,
//////                                        Bundle extras) {
//////                // TODO Auto-generated method stub
//////            }
//////        });
////
////
////        FarmerLogoutButton.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                mAuth.signOut();
////                LogoutFarmer();
////
////            }
////        });
////
////        FarmerCallTransporterButton.setOnClickListener(v -> {
////            init();
////            //registerOnlineSystem();
////
////
////
////            GeoFire geoFire= new GeoFire(FarmerDatabaseReff);
////            geoFire.setLocation(farmerID,new GeoLocation(lastlocation.getLatitude(),lastlocation.getLongitude()));
////
////            CustomerPickUpLocation= new LatLng(lastlocation.getLatitude(),lastlocation.getLongitude());
////            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CustomerPickUpLocation, 18f));
////            mMap.addMarker(new MarkerOptions().position(CustomerPickUpLocation).title("Pickup Product from Here"));
////
////            FarmerCallTransporterButton.setText("Getting a Transporter....");
////            GetClosestTransporter();
////
////            geoFire.setLocation(farmerID, new GeoLocation(lastlocation.getLatitude(), lastlocation.getLongitude()));
////
////
////
////        });
////    }
////
////    private void init() {
////
////
////
////        onlineReff = FirebaseDatabase.getInstance().getReference().child("Farmers");
////        FarmerDatabaseReff= FirebaseDatabase.getInstance().getReference().child("Farmer Requests");
////        FarmerLocationReff = FirebaseDatabase.getInstance().getReference(Constants.CUSTOMERS_LOCATION_REFERENCES);
////        currentUserReff = FirebaseDatabase.getInstance().getReference(Constants.CUSTOMERS_LOCATION_REFERENCES)
////                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
////        DriverAvailableReff = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
////        LocationDriverReff = FirebaseDatabase.getInstance().getReference().child("Working Drivers");
////
////
////        geoFire = new GeoFire(FarmerDatabaseReff);
////        //registerOnlineSystem();
////
////        locationRequest = new LocationRequest();
////        locationRequest.setSmallestDisplacement(10f);
////        locationRequest.setInterval(5000);
////        locationRequest.setFastestInterval(3000);
////        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
////
////        locationCallback = new LocationCallback() {
////            @Override
////            public void onLocationResult(LocationResult locationResult) {
////                super.onLocationResult(locationResult);
////
////
////
//////                lastLocation=location;
////
////               // CustomerPickUpLocation = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
////
////                // LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
//////                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CustomerPickUpLocation, 18f));
//////                mMap.addMarker(new MarkerOptions().position(CustomerPickUpLocation).title("Pickup Product from Here"));
//////
//////                geoFire.setLocation(farmerID, new GeoLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()));
////
////            }
////        };
////
////
////    }
////
////
////    private void  GetClosestTransporter() {
////        GeoFire geoFire = new GeoFire(DriverAvailableReff);
////        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(CustomerPickUpLocation.latitude, CustomerPickUpLocation.longitude), radius);
////        geoQuery.removeAllListeners();
////
////        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
////            @Override
////            public void onKeyEntered(String key, GeoLocation location) {
////                if (!transporterfound) {
////                    transporterfound = true;
////                    transporterfoundID = key;
////
////                 TransporterReff= FirebaseDatabase.getInstance().getReference().child("Transporters").child(transporterfoundID);
////                   HashMap driverMap= new HashMap();
////                   driverMap.put("FarmerRideID",farmerID);
////                    TransporterReff.updateChildren(driverMap);
////
////                    GettingDriverLocation();
////                    FarmerCallTransporterButton.setText("Looking for Driver Location....");
////
////                }
////            }
////
////            @Override
////            public void onKeyExited(String key) {
////
////            }
////
////            @Override
////            public void onKeyMoved(String key, GeoLocation location) {
////
////            }
////
////            @Override
////            public void onGeoQueryReady() {
////
////                if (!transporterfound) {
////
////                    radius = radius + 1;
////                    GetClosestTransporter();
////
////                }
////            }
////
////            @Override
////            public void onGeoQueryError(DatabaseError error) {
////
////            }
////        });
////    }
////
////    private void GettingDriverLocation()
////    {
////        LocationDriverReff.child(transporterfoundID).child("l")
////        .addValueEventListener(new ValueEventListener() {
////            @Override
////            public void onDataChange(@NonNull DataSnapshot snapshot)
////            {
////                if(snapshot.exists()){
////                    List<Object> driverLocationMap=(List<Object>)snapshot.getValue();
////                    double LocationLatitude =0;
////                    double LocationLng= 0;
////                    FarmerCallTransporterButton.setText("Driver Found");
////
////                    if(driverLocationMap.get(0) !=null){
////
////                        LocationLatitude=Double.parseDouble(driverLocationMap.get(0).toString());
////
////
////                    }
////                    if(driverLocationMap.get(1) !=null){
////
////                        LocationLng=Double.parseDouble(driverLocationMap.get(1).toString());
////                    }
////
////                    LatLng DriverLatLng= new LatLng(LocationLatitude,LocationLng);
////                    if(DriverMarker !=null){
////
////                        DriverMarker.remove();
////
////                    }
////
////                    Location location1=new Location("");
////                    location1.setLatitude(CustomerPickUpLocation.latitude);
////                    location1.setLongitude(CustomerPickUpLocation.longitude);
////
////
////
////                    Location location2=new Location("");
////                    location2.setLatitude(DriverLatLng.latitude);
////                    location2.setLongitude(DriverLatLng.longitude);
////
////                    float Distance = location1.distanceTo(location2);
////                    FarmerCallTransporterButton.setText("Driver found"+String.valueOf(Distance));
////
////
////                    DriverMarker=mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Your Driver is here"));
////
////                }
////
////            }
////
////            @Override
////            public void onCancelled(@NonNull DatabaseError error) {
////
////            }
////        });
//
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        mMap.setMyLocationEnabled(true);
//        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//
//      //  Dexter.withContext(this)
////                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
////                .withListener(new PermissionListener() {
////                    @Override
////                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
////                        mMap.setMyLocationEnabled(true);
////                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
////                        mMap.setOnMyLocationButtonClickListener(() -> {
////                            fusedLocationProviderClient.getLastLocation()
////                                    .addOnFailureListener(new OnFailureListener() {
////                                        @Override
////                                        public void onFailure(@NonNull Exception e) {
////                                            Toast.makeText(FarmersMapActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
////                                        }
////                                    })
////
////                                    .addOnSuccessListener(location -> {
////                                        if(location!= null) {
////                                            lastlocation=location;
////                                            LatLng UserLatng = new LatLng(location.getLatitude(), location.getLongitude());
////                                            mMap.animateCamera(CameraUpdateFactory.newLatLng(UserLatng));
////                                            mMap.animateCamera(CameraUpdateFactory.zoomTo(18f));
////                                        }
////
////                                    });
////                            return true;
////                        });
////
////
////                    }
////
////
////
////                    @Override
////                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
////                        Toast.makeText(FarmersMapActivity.this, "Permission" + permissionDeniedResponse.getPermissionName() + "was denied", Toast.LENGTH_SHORT).show();
////
////                    }
////
////                    @Override
////                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
////
////                    }
////                }).check();
//
//    }
//
//
////    @Override
////    protected void onStop() {
////        super.onStop();
////
////    }
//
//
////    private void LogoutFarmer() {
////        Intent welcomeIntent = new Intent(FarmersMapActivity.this, MainActivity.class);
////        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
////        Toast.makeText(this, "Logged Out Successfuly", Toast.LENGTH_SHORT).show();
////        startActivity(welcomeIntent);
////        finish();
////    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//
//    }
//
//
//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//        mLocationRequest= new LocationRequest();
//        mLocationRequest.setInterval(1000);
//        mLocationRequest.setFastestInterval(1000);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//       // fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
//
//
//
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//
//    }
//
////    @Override
////    public void onLocationChanged(Location location) {
////
////        lastlocation=location;
////
////        if(location!= null){
////            LatLng UserLatng = new LatLng(location.getLatitude(), location.getLongitude());
////            mMap.animateCamera(CameraUpdateFactory.newLatLng(UserLatng));
////            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
////        }
////
////
////    }
////
////    @Override
////    public void onStatusChanged(String provider, int status, Bundle extras) {
////
////    }
////
////    @Override
////    public void onProviderEnabled(String provider) {
////
////    }
////
////    @Override
////    public void onProviderDisabled(String provider) {
////
////    }
//}
