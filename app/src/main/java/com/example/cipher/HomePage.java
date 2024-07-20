package com.example.cipher;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomePage extends AppCompatActivity {

    ImageButton toProfile, toScan;
    public static final String SHARED_PREFS = "sharedPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.homepage);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toProfile = findViewById(R.id.toProfile);
        toProfile.setOnClickListener(view -> {
            Intent intent = new Intent(HomePage.this, Profile.class);
            startActivity(intent);
        });

        toScan = findViewById(R.id.toScan);
        toScan.setOnClickListener(view -> {
            Intent intent = new Intent(HomePage.this, ScanQR.class);
            startActivity(intent);
        });

        toProfile = findViewById(R.id.toProfile);
        toProfile.setOnClickListener(view -> {
            Intent intent = new Intent(HomePage.this, Profile.class);
            startActivity(intent);
        });

    }
}
