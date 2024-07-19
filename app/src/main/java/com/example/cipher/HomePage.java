package com.example.cipher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomePage extends AppCompatActivity {

    Button logout;
    public static final String SHARED_PREFS = "sharedPrefs";
    ImageButton toProfile, toScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.homepage);

        logout = findViewById(R.id.logoutBtn);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("name", "");
                editor.apply();

                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        toProfile = findViewById(R.id.toProfile);
        toProfile.setOnClickListener(view -> {
            Intent intent = new Intent(HomePage.this, Profile.class);
            startActivity(intent);
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
    }
}