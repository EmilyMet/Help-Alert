package com.example.helpalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AddContacts extends AppCompatActivity {

    TextInputEditText editTextName, editTextNumber;
    Button buttonReg;
    FirebaseAuth mAuth;
    DatabaseReference reff;
    User user;
    Contact contact;
    FirebaseUser firebaseUser;
    String id, userName, userEmail;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            Intent intent = new Intent(getApplicationContext(), Register.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_add_contacts);
        mAuth = FirebaseAuth.getInstance();
        editTextName = (TextInputEditText)findViewById(R.id.name);
        editTextNumber = (TextInputEditText)findViewById(R.id.number);
        buttonReg = findViewById(R.id.btn_addcontact);
        reff = FirebaseDatabase.getInstance("https://help-alert-c5e2d-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users");

        buttonReg.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View view) {
                String name, number;
                name = String.valueOf(editTextName.getText());
                number = String.valueOf(editTextNumber.getText());

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(AddContacts.this, "Enter name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(number)) {
                    Toast.makeText(AddContacts.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Toast.makeText(AddContacts.this, name, Toast.LENGTH_SHORT).show();

                contact = new Contact();
                contact.setNumber(name);
                contact.setNumber(number);

                firebaseUser = mAuth.getCurrentUser();
                id = firebaseUser.getUid();
                Toast.makeText(AddContacts.this, id, Toast.LENGTH_SHORT).show();

                reff = FirebaseDatabase.getInstance("https://help-alert-c5e2d-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users");
                reff.child(id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        user = dataSnapshot.getValue(User.class);
                        userName = user.getName();
                        userEmail = user.getEmail();

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(AddContacts.this,"Failed to read user detail",Toast.LENGTH_SHORT).show();
                    }
                });



                HashMap contactDetails = new HashMap<>();
                contactDetails.put("name", name);
                contactDetails.put("number", number);

                reff.child(id).child("contacts").updateChildren(contactDetails).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()){
                            Toast.makeText(AddContacts.this,"Successfully Updated",Toast.LENGTH_SHORT).show();
                            nextPage();

                        }else {
                            Toast.makeText(AddContacts.this,"Failed to Update",Toast.LENGTH_SHORT).show();

                        }

                    }
                });


            }
        });
    }

    public void nextPage(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

}