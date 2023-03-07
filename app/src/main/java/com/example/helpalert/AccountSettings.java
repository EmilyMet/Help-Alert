package com.example.helpalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AccountSettings extends AppCompatActivity {
    BottomNavigationView navigationView;
    FirebaseUser firebaseUser;
    FirebaseAuth mAuth;
    DatabaseReference reff;
    Button buttonLogout, buttonContacts, buttonPassword;
    private Dialog passwordDialog, contactDialog;
    TextInputEditText editTextOldPass, editTextNewPass, editTextConfirmPass;
    TextInputEditText editTxtName, editTxtNumber;
    String email;
    User user;
    String id, userName, userEmail;
    Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_account_settings);
        mAuth = FirebaseAuth.getInstance();
        buttonLogout = findViewById(R.id.logout);
        buttonContacts = findViewById(R.id.contact);
        buttonPassword = findViewById(R.id.password);

        navigationView = findViewById(R.id.navigation);
        navigationView.setSelectedItemId(R.id.account);
        navigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.buttonTrack:
                        // Handle the home button click
                        startActivity(new Intent(AccountSettings.this, MainActivity.class));
                        return true;
                    case R.id.mapTrack:
                        // Handle the dashboard button click
                        startActivity(new Intent(AccountSettings.this, MapTracking.class));
                        return true;
                    case R.id.account:
                        // Handle the notifications button click
                        startActivity(new Intent(AccountSettings.this, AccountSettings.class));
                        return true;
                    default:
                        return false;
                }
            }
        });

        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            email = firebaseUser.getEmail();
        }

        passwordDialog = new Dialog(this);
        passwordDialog.setContentView(R.layout.password_dialog);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            passwordDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_background));
        }
        passwordDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        passwordDialog.setCancelable(false); //Optional
        passwordDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Setting the animations to dialog

        Button bttnSubmit = passwordDialog.findViewById(R.id.btnSubmit);
        Button bttnCancel = passwordDialog.findViewById(R.id.btnCancel);

        bttnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextOldPass = (TextInputEditText)passwordDialog.findViewById(R.id.editTextOldPass);
                editTextNewPass = (TextInputEditText)passwordDialog.findViewById(R.id.editTextNewPass);
                editTextConfirmPass = (TextInputEditText)passwordDialog.findViewById(R.id.editTextConfirmPass);

                String oldPass, newPass, confirmPass;
                oldPass = String.valueOf(editTextOldPass.getText());
                newPass = String.valueOf(editTextNewPass.getText());
                confirmPass = String.valueOf(editTextConfirmPass.getText());

                if (TextUtils.isEmpty(oldPass)) {
                    Toast.makeText(AccountSettings.this, "Enter old password", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (TextUtils.isEmpty(newPass)) {
                    Toast.makeText(AccountSettings.this, "Enter password", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (TextUtils.isEmpty(confirmPass)) {
                    Toast.makeText(AccountSettings.this, "Confirm password", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (!newPass.equals(confirmPass)){
                    Toast.makeText(AccountSettings.this, "Two passwords don't watch", Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    changePassword(newPass);
                    passwordDialog.dismiss();
                }
            }
        });

        bttnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                passwordDialog.dismiss();
            }
        });

        contactDialog = new Dialog(this);
        contactDialog.setContentView(R.layout.contacts_dialog);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            contactDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_background));
        }
        contactDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        contactDialog.setCancelable(false); //Optional
        contactDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Setting the animations to dialog

        Button bttnSub = contactDialog.findViewById(R.id.btnSubmit);
        Button bttnCanc = contactDialog.findViewById(R.id.btnCancel);

        bttnSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTxtName = (TextInputEditText)contactDialog.findViewById(R.id.editTextName);
                editTxtNumber = (TextInputEditText)contactDialog.findViewById(R.id.editTextNumber);

                String cName, cNumber;
                cName = String.valueOf(editTxtName.getText());
                cNumber = String.valueOf(editTxtNumber.getText());

                if (TextUtils.isEmpty(cName)) {
                    Toast.makeText(AccountSettings.this, "Enter contact's name", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (TextUtils.isEmpty(cNumber)) {
                    Toast.makeText(AccountSettings.this, "Enter contact's number", Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    changeContact(cName, cNumber);
                    contactDialog.dismiss();
                }

            }
        });

        bttnCanc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                contactDialog.dismiss();
            }
        });

        buttonLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        buttonContacts.setOnClickListener(view -> {
            contactDialog.show();
        });

        buttonPassword.setOnClickListener(view -> {
            passwordDialog.show();
        });
    }

    private void changePassword(String newPassword){
        //FirebaseAuth mAuth = FirebaseAuth.getInstance();
        //FirebaseUser user = mAuth.getCurrentUser();

        firebaseUser.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AccountSettings.this, "User password updated successfully.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AccountSettings.this, "User password update failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void changeContact(String name, String number){
        firebaseUser = mAuth.getCurrentUser();
        id = firebaseUser.getUid();

        contact = new Contact();
        contact.setNumber(name);
        contact.setNumber(number);

        reff = FirebaseDatabase.getInstance("https://help-alert-c5e2d-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users");

        HashMap contactDetails = new HashMap<>();
        contactDetails.put("name", name);
        contactDetails.put("number", number);

        reff.child(id).child("contacts").updateChildren(contactDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                if (task.isSuccessful()){
                    Toast.makeText(AccountSettings.this,"Successfully Updated",Toast.LENGTH_SHORT).show();

                }else {
                    Toast.makeText(AccountSettings.this,"Failed to Update",Toast.LENGTH_SHORT).show();

                }

            }
        });
    }
}