package com.wartech.chatpro;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

import static android.R.attr.value;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mDatabaseRef;

    private final String TAG = "ChatPro";
    private static final int RC_SIGN_IN = 1;
    public static String mUserPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Components;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        // Initialize AuthStateListener
        initializeAuthStateListener();

    }

    /**
     * Method to Initialize AuthStateListener
     **/
    public void initializeAuthStateListener() {

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // if user is logged in and app is up-to-date
                    // set userPhoneNumber and check user details
                    mUserPhoneNumber = user.getPhoneNumber();
                    // check if app is fully updated
                    checkBuildVersion();

                } else {
                    // if user is not logged in or isn't registered, verify phone number
                    phoneNumberVerification();
                }
            }
        };
    }

    /**
     * Method to set User Detials in Firebase Database
     **/
    public void addUserDetails() {
        EditText usernameEditText = findViewById(R.id.usernameEditText);
        String username = usernameEditText.getText().toString();
        if (TextUtils.isEmpty(username)) {
            // if user hasn't enterd a name in text field, show a toast
            Toast.makeText(SignupActivity.this, "Please enter username", Toast.LENGTH_SHORT).show();
        } else {
            // otherwise set username in the databaase
            mDatabaseRef.child("users").child(mUserPhoneNumber).child("user_details")
                    .child("username").setValue(username);

            mDatabaseRef.child("users").child(mUserPhoneNumber).child("user_details")
                    .child("last_seen").setValue("Active");

            // move to main activity
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Method to check user details
     **/
    public void checkUserDetails() {
        mDatabaseRef.child("users").child(mUserPhoneNumber).child("user_details")
                .child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                // if username is null
                if (TextUtils.isEmpty(value)) {
                    // set view to get user details on runtime
                    setContentView(R.layout.activity_user_details);

                    Button doneButton = findViewById(R.id.doneButton);
                    doneButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // add user details in the database
                            addUserDetails();
                        }
                    });
                } else {
                    // if user is logged in, set status to Active
                    mDatabaseRef.child("users").child(mUserPhoneNumber).child("user_details")
                            .child("last_seen").setValue("Active");

                    // move to main Activity
                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Method to authenticate user and verify phone number
     **/
    public void phoneNumberVerification() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(
                                Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER)
                                        .build())).build(), RC_SIGN_IN);
    }

    /**
     * onActivityResult processes the result of login requests
     **/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            // if login requset is successful show a toast,
            // otherwise tell user that the request failed and exit.
            if (resultCode == RESULT_OK) {
                Toast.makeText(SignupActivity.this, "Signed in successfully!!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(SignupActivity.this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // remove AuthStateListener when activity is paused
        if (mAuthStateListener != null) {
            Log.d(TAG, "remove AuthStateListener for user: " + mUserPhoneNumber);
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // add AuthStateListener when activity is resumed
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    /**
     * Method to check whether app versionName matches the one in Firebase
     **/
    public void checkBuildVersion() {
        final String versionName = BuildConfig.VERSION_NAME;
        Log.d(TAG, "version name in app: " + versionName);
        mDatabaseRef.child("app_info").child("version_name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "version name in Firebase: " + value);
                if (versionName.equals(value)) {
                    // check user details if version matches
                    checkUserDetails();

                } else {

                    Toast.makeText(SignupActivity.this, "Your app is fully updated", Toast.LENGTH_SHORT).show();
                    // createAlertDialog();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Method to create Alert Dialog for updating app
     **/
    public void createAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
        builder.setTitle("Update Required!");
        builder.setMessage("A newer version of the app is available. Please update to synchronize your data");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
    }

}
