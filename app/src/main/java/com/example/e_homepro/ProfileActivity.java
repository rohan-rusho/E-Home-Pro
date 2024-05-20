package com.example.e_homepro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imageViewProfile;
    private EditText editTextName, editTextEmail;
    private Button buttonChangePhoto, buttonUpdateProfile;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        imageViewProfile = findViewById(R.id.imageViewProfile);
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonChangePhoto = findViewById(R.id.buttonChangePhoto);
        buttonUpdateProfile = findViewById(R.id.buttonUpdateProfile);

        // Load user details
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String name = user.getDisplayName();
            Uri photoUrl = user.getPhotoUrl();

            editTextEmail.setText(email);
            editTextName.setText(name);

            // Load profile photo using Glide
            if (photoUrl != null) {
                Glide.with(this).load(photoUrl).into(imageViewProfile);
            }
        }

        // Set onClickListener for buttonChangePhoto to handle photo upload
        buttonChangePhoto.setOnClickListener(v -> chooseImage());

        // Set onClickListener for buttonUpdateProfile to handle profile updates
        buttonUpdateProfile.setOnClickListener(v -> updateProfile());
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void updateProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Update name
            String newName = editTextName.getText().toString().trim();

            if (!newName.isEmpty()) {
                UserProfileChangeRequest.Builder profileUpdatesBuilder = new UserProfileChangeRequest.Builder()
                        .setDisplayName(newName);

                if (imageUri != null) {
                    // Upload new profile image
                    StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_images");
                    StorageReference imageRef = storageRef.child(user.getUid() + ".jpg");

                    imageRef.putFile(imageUri)
                            .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                profileUpdatesBuilder.setPhotoUri(uri);
                                updateUserProfile(user, profileUpdatesBuilder.build());
                            }))
                            .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
                } else {
                    // Update profile without new image
                    updateUserProfile(user, profileUpdatesBuilder.build());
                }
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUserProfile(FirebaseUser user, UserProfileChangeRequest profileUpdates) {
        user.updateProfile(profileUpdates)
                .addOnSuccessListener(aVoid -> {
                    // Profile updated successfully
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    if (profileUpdates.getPhotoUri() != null) {
                        Glide.with(ProfileActivity.this).load(profileUpdates.getPhotoUri()).into(imageViewProfile);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewProfile.setImageURI(imageUri);
        }
    }
}
