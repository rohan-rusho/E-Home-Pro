package com.example.e_homepro;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private String username; // Store the username

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Retrieve the username passed from LoginActivity
        username = getIntent().getStringExtra("name");
        if (username == null) {
            // Handle null username (optional)
            username = "User";
        }

        // Set welcome text with username
        String welcomeMessage = "Welcome, " + username + "!";
        TextView welcomeTextView = findViewById(R.id.welcomeText);
        welcomeTextView.setText(welcomeMessage);

        // Initialize buttons
        Button profileButton = findViewById(R.id.profileButton);
        Button logoutButton = findViewById(R.id.logoutButton);
        Button lightControlButton = findViewById(R.id.lightControlButton);
        Button tempControlButton = findViewById(R.id.tempControlButton);
        Button securityControlButton = findViewById(R.id.securityControlButton);

        // Set click listeners
        profileButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                // Open ProfileActivity and pass username
                Intent profileIntent = new Intent(DashboardActivity.this, ProfileActivity.class);
                profileIntent.putExtra("username", username);
                startActivity(profileIntent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log out and go back to LoginActivity
                Intent logoutIntent = new Intent(DashboardActivity.this, LoginActivity.class);
                startActivity(logoutIntent);
                finish(); // Finish this activity to prevent going back to it with back button
            }
        });

        lightControlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Open LightControlActivity (assuming this activity exists)
                    Intent lightControlIntent = new Intent(DashboardActivity.this, LightControlActivity.class);
                    startActivity(lightControlIntent);
                } catch (Exception e) {
                    // Log any exceptions that occur
                    e.printStackTrace();
                }
            }
        });


        tempControlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show under development message
                showUnderDevelopmentDialog("Temperature control");
            }
        });

        securityControlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show under development message
                showUnderDevelopmentDialog("Viewing security cameras");
            }
        });
    }

    // Method to show the under development dialog
    private void showUnderDevelopmentDialog(String featureName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
        builder.setTitle("Under Development");
        builder.setMessage(featureName + " is under development. Check back soon!");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false); // Prevent dialog from being dismissed when touched outside
        builder.show();
    }
}
