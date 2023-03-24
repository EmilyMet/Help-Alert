package com.example.helpalert;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements LocationListener {

    Button buttonLocation, buttonPause, buttonSend;
    FirebaseAuth mAuth;
    TextView userInfo, textLocation;
    DatabaseReference reff, reffAlerts;
    long maxid=0;
    FirebaseUser firebaseUser;
    LocationManager locationManager;
    FusedLocationProviderClient fusedLocationClient;
    String id, name, number, userName;
    Contact contact;
    BottomNavigationView navigationView;
    Double latitude, longitude;

    ActivityResultLauncher<String[]> mPermissionResultLauncher;
    private boolean isCourseLocationPermissionGranted = false;
    private boolean isFineLocationPermissionGranted = false;
    private boolean isSMSPermissionGranted = false;
    private boolean isResponseYes = false;
    private boolean isResponseNo = false;
    private boolean isTracking = false;
    private boolean isHoldingButton = false;
    private Dialog dialog;
    CountDownTimer timer;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_main);
        buttonLocation = findViewById(R.id.button_location);
        buttonPause = findViewById(R.id.pauseTrack);
        buttonSend = findViewById(R.id.sendlocation);
        mAuth = FirebaseAuth.getInstance();
        userInfo = findViewById(R.id.user_details);
        textLocation = findViewById(R.id.text_location);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        reffAlerts = FirebaseDatabase.getInstance("https://help-alert-c5e2d-default-rtdb.europe-west1.firebasedatabase.app").getReference("Alerts");

        reffAlerts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                    maxid = (snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
            //userInfo.setText(firebaseUser.getEmail());
        }



        navigationView = findViewById(R.id.navigation);
        navigationView.setSelectedItemId(R.id.buttonTrack);

        navigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.buttonTrack:
                        // Handle the home button click
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                        return true;
                    case R.id.mapTrack:
                        // Handle the dashboard button click
                        startActivity(new Intent(MainActivity.this, MapTracking.class));
                        return true;
                    case R.id.account:
                        // Handle the notifications button click
                        startActivity(new Intent(MainActivity.this, AccountSettings.class));
                        return true;
                    default:
                        return false;
                }
            }
        });


        //Create the Dialog here
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.check_in_dialog);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_background));
        }
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false); //Optional
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Setting the animations to dialog

        Button bttnYes = dialog.findViewById(R.id.btn_yes);
        Button bttnNo = dialog.findViewById(R.id.btn_no);

        bttnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(MainActivity.this, "Okay", Toast.LENGTH_SHORT).show();
                isResponseYes = true;
                dialog.dismiss();
            }
        });

        bttnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                isResponseNo = true;
                dialog.dismiss();
            }
        });


        buttonLocation.setOnTouchListener(new View.OnTouchListener() {
            private long start = 0;
            private long end = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    isTracking = true;
                    isResponseNo = false;
                    isResponseYes = false;
                    isHoldingButton = true;
                    userInfo.setText("Tracking location...");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    isHoldingButton = false;
                    timer = new CountDownTimer(10000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            // Do nothing every second until 30 seconds have passed
                            if(isHoldingButton){
                                timer.cancel();
                            }
                        }

                        public void onFinish() {
                            // Call your function after 30 seconds have passed
                            if(isTracking && !isHoldingButton) {
                                startCheckInDialog();
                            }
                        }
                    }.start();

                    //getLocation();
                }
                return true;
            }
        });

        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTracking) {
                    isTracking = false;
                    userInfo.setText("Hold the button to start location tracking");
                }
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });
    }

    private void startCheckInDialog(){
        dialog.show();
        timer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                // Do nothing every second until 30 seconds have passed
                if (isResponseYes) {
                    getLocation();
                    timer.cancel();
                    userInfo.setText("Hold the button to start location tracking");
                    isTracking = false;
                }
                if (isResponseNo) {
                    timer.cancel();
                    userInfo.setText("Hold the button to start location tracking");
                    isTracking = false;
                }
                if(!isTracking) {
                    timer.cancel();
                }
            }

            public void onFinish() {
                // Call your function after 30 seconds have passed
                if (!isResponseYes && !isResponseNo) {
                    getLocation();
                    userInfo.setText("Hold the button to start location tracking");
                    isTracking = false;
                }
                    dialog.dismiss();
            }
        }.start();

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
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                String address = addresses.get(0).getAddressLine(0);
                                String url = "https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude;
                                Log.d("MyApp", url);

                                textLocation.setText(address);

                                getContacts(address, url);

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




    private void getContacts(String address, String url) {
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
                        sendSMS(address,url,userName, name, number);
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

    private void sendSMS(String address, String url, String username, String contactName, String number){
        String message1 = "Hi "+contactName+", please check in with "
                +username+ " as they seem to have gotten into trouble at "+address;
        String message2 = ". The Google Maps link is "+url;
        ArrayList<String> parts = new ArrayList<>();
        parts.add(message1);
        parts.add(message2);
        try{
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendMultipartTextMessage(number, null, parts, null, null);
            Toast.makeText(this, "Location Sent", Toast.LENGTH_SHORT).show();
            storeAlertData(username, address);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
        }
    }

    private void storeAlertData(String userName, String address){
        Date currentDate = new Date();
        String id = firebaseUser.getUid();
        //SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        //String dateString = dateFormat.format(currentDate);

        Alert alert = new Alert();
        alert.setId(String.valueOf(id));
        alert.setDate(currentDate);
        alert.setAddress(address);
        alert.setLatitude(latitude);
        alert.setLongitude(longitude);
        reffAlerts.child(String.valueOf(maxid)).setValue(alert);
    }

}