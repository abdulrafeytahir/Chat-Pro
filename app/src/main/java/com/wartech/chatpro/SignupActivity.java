package com.wartech.chatpro;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import static android.os.Build.VERSION_CODES.M;
import static com.wartech.chatpro.ChatProConstants.ACTIVE;
import static com.wartech.chatpro.ChatProConstants.APP_INFO;
import static com.wartech.chatpro.ChatProConstants.LAST_SEEN;
import static com.wartech.chatpro.ChatProConstants.USERS;
import static com.wartech.chatpro.ChatProConstants.USER_DETAILS;


public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mDatabaseRef;

    private final String TAG = "ChatPro";
    private static final int RC_SIGN_IN = 101;
    public static String mUserPhoneNumber;
    static boolean mCalledAlready = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // enabling firebase to cache data in the device
        if (!mCalledAlready) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            mCalledAlready = true;
        }
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
            @RequiresApi(api = M)
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

    // Method to check if device is connected to the internet
    public boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Method to set User Detials in Firebase Database
     **/


    /**
     * Method to check user details
     **/
    public void checkUserDetails() {
        DatabaseReference ref = mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(USER_DETAILS);
        ref.keepSynced(true);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // check if user details have been added
                if (!dataSnapshot.exists()) {
                    // if not, then set view to get user details on runtime
                    Intent intent = new Intent(SignupActivity.this, UserDetails.class);
                    startActivity(intent);

                } else {
                    // if user is logged in, set status to Active
                    mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(USER_DETAILS)
                            .child(LAST_SEEN).setValue(ACTIVE);

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
                if (!isNetworkAvailable()) {
                    Intent intent = new Intent(SignupActivity.this, EmptyActivity.class);
                    startActivity(intent);
                }
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
        DatabaseReference ref = mDatabaseRef.child(APP_INFO).child(ChatProConstants.VERSION_NAME);
        ref.keepSynced(true);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "version name in Firebase: " + value);
                if (versionName.equals(value)) {
                    // check user details if version matches
                    checkUserDetails();

                } else {
                    // show a dialog box telling the user to update the app
                    createAlertDialog();
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
        builder.setMessage("A new version of the app is available. Please update to synchronize your data");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
    }

}
