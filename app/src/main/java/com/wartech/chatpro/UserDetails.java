package com.wartech.chatpro;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.wartech.chatpro.ChatProConstants.ACTIVE;
import static com.wartech.chatpro.ChatProConstants.CHAT_PHOTOS;
import static com.wartech.chatpro.ChatProConstants.LAST_SEEN;
import static com.wartech.chatpro.ChatProConstants.PROFILE_PICS;
import static com.wartech.chatpro.ChatProConstants.PROFILE_PIC_URI;
import static com.wartech.chatpro.ChatProConstants.STATUS;
import static com.wartech.chatpro.ChatProConstants.USERNAME;
import static com.wartech.chatpro.ChatProConstants.USERS;
import static com.wartech.chatpro.ChatProConstants.USER_DETAILS;
import static com.wartech.chatpro.SignupActivity.mUserPhoneNumber;

public class UserDetails extends AppCompatActivity {
    private static final int RC_SELECT_PICTURE = 202;
    private String profilePicUri;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        ImageView imageView = findViewById(R.id.profile_pic);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RC_SELECT_PICTURE);
            }
        });

        Button doneButton = findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // add user details in the database
                addUserDetails();
            }
        });
    }

    public void addUserDetails() {
        EditText usernameEditText = findViewById(R.id.username_edit_text);
        String username = usernameEditText.getText().toString();

        EditText statusEditText = findViewById(R.id.status_edit_text);
        String status = statusEditText.getText().toString();
        if (TextUtils.isEmpty(username)) {
            // if user hasn't entered a name in text field, show a toast
            Toast.makeText(UserDetails.this, "Please enter username", Toast.LENGTH_SHORT).show();
        } else {

            if (TextUtils.isEmpty(status)) {
                status = "Hey there! I am using Chat Pro.";
            }
            // otherwise set username in the database
            // Create a new map of values, save them in firebase database
            Map userDetails = new HashMap();
            userDetails.put(USERNAME, username);
            userDetails.put(PROFILE_PIC_URI, profilePicUri);
            userDetails.put(STATUS, status);
            userDetails.put(LAST_SEEN, ACTIVE);

            mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(USER_DETAILS).updateChildren(userDetails);

            // move to main activity
            Intent intent = new Intent(UserDetails.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SELECT_PICTURE && resultCode == RESULT_OK) {

            try {
                // loading image URI and setting the imageview to profile pic
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                CircleImageView imageView = findViewById(R.id.profile_pic);
                imageView.setImageBitmap(selectedImage);

                // upload image to the firebase database
                StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                        .child(PROFILE_PICS).child(imageUri.getLastPathSegment());

                storageReference.putFile(imageUri).addOnSuccessListener
                        (this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                profilePicUri = downloadUrl.toString();
                                Log.d("User Details", "Image successfully added");
                            }
                        });

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
