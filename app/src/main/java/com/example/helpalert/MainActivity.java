package com.example.helpalert;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements LocationListener {

    Button buttonLogout, buttonLocation;
    FirebaseAuth mAuth;
    TextView userInfo, textLocation;
    DatabaseReference reff;
    FirebaseUser firebaseUser;
    LocationManager locationManager;
    FusedLocationProviderClient fusedLocationClient;
    String id, name, number, userName;
    Contact contact;

    ActivityResultLauncher<String[]> mPermissionResultLauncher;
    private boolean isCourseLocationPermissionGranted = false;
    private boolean isFineLocationPermissionGranted = false;
    private boolean isSMSPermissionGranted = false;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonLogout = findViewById(R.id.logout);
        buttonLocation = findViewById(R.id.button_location);
        mAuth = FirebaseAuth.getInstance();
        userInfo = findViewById(R.id.user_details);
        textLocation = findViewById(R.id.text_location);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                if (result.get(Manifest.permission.ACCESS_COARSE_LOCATION) != null){
                    isCourseLocationPermissionGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);
                }

                if (result.get(Manifest.permission.ACCESS_FINE_LOCATION) != null){
                    isFineLocationPermissionGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                }

                if (result.get(Manifest.permission.SEND_SMS) != null){
                    isSMSPermissionGranted = result.get(Manifest.permission.SEND_SMS);
                }
            }
        });

        requestPermission();

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            userInfo.setText(firebaseUser.getEmail());
        }

        buttonLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });


        buttonLocation.setOnTouchListener(new View.OnTouchListener() {
            private long start = 0;
            private long end = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    this.start = System.currentTimeMillis();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    getLocation();
                }
                return true;
            }
        });
    }

    private void requestPermission(){

        isCourseLocationPermissionGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        isFineLocationPermissionGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        isSMSPermissionGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED;

        List<String> permissionRequest = new ArrayList<String>();

        if (!isCourseLocationPermissionGranted){
            permissionRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!isFineLocationPermissionGranted){
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!isSMSPermissionGranted){
            permissionRequest.add(Manifest.permission.SEND_SMS);
        }

        if (!permissionRequest.isEmpty()){
            mPermissionResultLauncher.launch(permissionRequest.toArray(new String[0]));
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            Toast.makeText(MainActivity.this, "Getting Location", Toast.LENGTH_SHORT).show();
                            try {
                                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                String address = addresses.get(0).getAddressLine(0);

                                textLocation.setText(address);
                                Toast.makeText(MainActivity.this, address, Toast.LENGTH_SHORT).show();
                                getContacts(address);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Location is null", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            String address = addresses.get(0).getAddressLine(0);

            textLocation.setText(address);
            locationManager.removeUpdates(this);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void getContacts(String address) {
        firebaseUser = mAuth.getCurrentUser();
        id = firebaseUser.getUid();

        reff = FirebaseDatabase.getInstance("https://help-alert-c5e2d-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users");
        reff.child(id).child("contacts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                DatabaseReference parentRef = dataSnapshot.getRef().getParent();
                contact = dataSnapshot.getValue(Contact.class);
                name = contact.getName();
                number = contact.getNumber();

                parentRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        userName = user.getName();
                        sendSMS(address,userName, name, number);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle errors here
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to read user detail", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSMS(String address, String username, String contactName, String number){
        String message = "Hi "+contactName+", please check in with "
                +username+ " as they seem to have gotten into trouble at "+address;
        try{
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, message, null, null);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Failed to send message, isSMSPermissionGranted: " +isSMSPermissionGranted, Toast.LENGTH_SHORT).show();
        }
    }

}