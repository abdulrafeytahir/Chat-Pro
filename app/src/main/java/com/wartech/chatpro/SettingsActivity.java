package com.wartech.chatpro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.wartech.chatpro.ChatProConstants.PROFILE_PICS;
import static com.wartech.chatpro.ChatProConstants.PROFILE_PIC_URI;
import static com.wartech.chatpro.ChatProConstants.STATUS;
import static com.wartech.chatpro.ChatProConstants.USERNAME;
import static com.wartech.chatpro.ChatProConstants.USERS;
import static com.wartech.chatpro.ChatProConstants.USER_DETAILS;
import static com.wartech.chatpro.R.id.profile_pic;
import static com.wartech.chatpro.R.id.status;
import static com.wartech.chatpro.SignupActivity.mUserPhoneNumber;

public class SettingsActivity extends AppCompatActivity {

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private Button btnSelect;
    private Uri selectedImageUri;
    private CircleImageView ivImage;
    private String userChoosenTask;
    private ImageView img_btn,img_btn2;
    private EditText txt,txt_status;

    private TextView statusTextView;
    private TextView phoneTextView;
    InputMethodManager inputMethodManager;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        statusTextView = findViewById(status);
        phoneTextView = findViewById(R.id.phone_number);
        ivImage = findViewById(R.id.user_profile_pic);
        txt_status=findViewById(R.id.status);
        img_btn2=findViewById(R.id.edit_status);
        txt = findViewById(R.id.user_name);
        txt.setInputType(InputType.TYPE_NULL);
        txt_status.setInputType(InputType.TYPE_NULL);

        img_btn = findViewById(R.id.edit_contact_name);

        requestData();

        img_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txt_status.requestFocus();
                txt_status.setSelection(txt_status.getText().length());
                txt_status.setInputType(InputType.TYPE_CLASS_TEXT);
                inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(view.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);

            }


        });
        txt_status.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //  Toast.makeText(MainActivity.this, txt.getText(), Toast.LENGTH_SHORT).show();
                    txt_status.setInputType(InputType.TYPE_NULL);
                    inputMethodManager.hideSoftInputFromWindow(txt.getApplicationWindowToken(), 0);

                    if(!TextUtils.isEmpty(txt_status.getText().toString())) {
                        mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(USER_DETAILS)
                                .child(STATUS).setValue(txt_status.getText().toString());
                    }

                    return true;
                }

                return false;
            }
        });


        img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txt.requestFocus();
                txt.setSelection(txt.getText().length());
                txt.setInputType(InputType.TYPE_CLASS_TEXT);
                inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(view.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);

            }


        });
        txt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //  Toast.makeText(MainActivity.this, txt.getText(), Toast.LENGTH_SHORT).show();
                    txt.setInputType(InputType.TYPE_NULL);
                    inputMethodManager.hideSoftInputFromWindow(txt.getApplicationWindowToken(), 0);

                    if(!TextUtils.isEmpty(txt.getText().toString())) {
                        mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(USER_DETAILS)
                                .child(USERNAME).setValue(txt.getText().toString());
                    }

                    return true;
                }

                return false;
            }
        });

    }

    private void requestData() {
        DatabaseReference reference = mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(USER_DETAILS);
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child(USERNAME).getValue(String.class);
                String imageUri = dataSnapshot.child(PROFILE_PIC_URI).getValue(String.class);
                String status = dataSnapshot.child(STATUS).getValue(String.class);

                statusTextView.setText(status);
                phoneTextView.setText(mUserPhoneNumber);
                txt.setText(username);

                Picasso.with(ivImage.getContext())
                        .load(imageUri)
                        .into(ivImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Gallery", "Remove Photo",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(SettingsActivity.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    if (result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Gallery")) {
                    userChoosenTask = "Choose from Gallery";
                    if (result)
                        galleryIntent();

                } else if (items[item].equals("Remove Photo")) {
                    userChoosenTask = "Choose from Gallery";
                    if (result)
                        removePhoto();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void removePhoto() {
        if (ivImage != null)
            ivImage.setImageResource(R.drawable.profile_pic);
    }


    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == SELECT_FILE)
                    onSelectFromGalleryResult(data);
                else if (requestCode == REQUEST_CAMERA)
                    onCaptureImageResult(data);
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        selectedImageUri = Uri.fromFile(destination);

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

        uploadImageToFirebase(selectedImageUri);
        ivImage.setImageBitmap(thumbnail);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm = null;
        if (data != null) {
            selectedImageUri = data.getData();
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        uploadImageToFirebase(selectedImageUri);
        ivImage.setImageBitmap(bm);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(PROFILE_PICS).child(imageUri.getLastPathSegment());

        storageReference.putFile(imageUri).addOnSuccessListener
                (this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        String profilePicUri = downloadUrl.toString();
                        mDatabaseRef.child(USERS).child(mUserPhoneNumber).child(USER_DETAILS)
                                .child(PROFILE_PIC_URI).setValue(profilePicUri);

                        Log.d("User Details", "Image successfully added");
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if (userChoosenTask.equals("Choose from Gallery"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }
}