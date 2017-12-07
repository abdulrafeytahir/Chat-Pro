package com.wartech.chatpro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private Button btnSelect;
    //  private ImageView ivImage;
    private  CircleImageView ivImage;
    private String userChoosenTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        ivImage = (CircleImageView) findViewById(R.id.profile_pic);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectImage();
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


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Gallery"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    private void selectImage() {
        final CharSequence[] items = {"Choose from Gallery","Remove Photo",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(UserDetails.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=Utility.checkPermission(UserDetails.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask ="Take Photo";
                    if(result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Gallery")) {
                    userChoosenTask ="Choose from Gallery";
                    if(result)
                        galleryIntent();

                }
                else if(items[item].equals("Remove Photo"))
                {
                    userChoosenTask ="Choose from Gallery";
                    if(result)
                        removePhoto();
                }
                else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    private void removePhoto()
    {
        if(ivImage!=null)
            ivImage.setImageResource(R.drawable.profile_pic);


    }


    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }



    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // imgs.setImageBitmap(thumbnail);
        ivImage.setImageBitmap(thumbnail);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ivImage.setImageBitmap(bm);
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
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }


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
                    (
                            this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
