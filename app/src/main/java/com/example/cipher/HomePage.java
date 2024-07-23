package com.example.cipher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomePage extends AppCompatActivity {

    ImageButton toProfile, toScan;
    public static final String SHARED_PREFS = "sharedPrefs";
    TextView userNameTextView;

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

        userNameTextView = findViewById(R.id.textView5);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String userName = sharedPreferences.getString("name", "User Name not found");
        userNameTextView.setText(userName);

        toProfile = findViewById(R.id.toProfile);
        toProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                String userId = sharedPreferences.getString("userId", null);
                String name = sharedPreferences.getString("name", null);
                String email = sharedPreferences.getString("email", null);
                String department = sharedPreferences.getString("department", null);

                Intent intent = new Intent(getApplicationContext(), Profile.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("NAME", name);
                intent.putExtra("EMAIL", email);
                intent.putExtra("DEPARTMENT", department);
                startActivity(intent);
            }
        });

        toScan = findViewById(R.id.toScan);
        toScan.setOnClickListener(view -> {
            Intent intent = new Intent(HomePage.this, ScanQR.class);
            startActivity(intent);
        });
    }
}