package com.example.cipher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomePage extends AppCompatActivity {

    ImageButton toProfile, toScan, toTutorial;
    public static final String SHARED_PREFS = "sharedPrefs";
    TextView userNameTextView;
    EditText editTest;

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

        editTest = findViewById(R.id.editTest);

        toProfile = findViewById(R.id.toProfile);
        toProfile.setOnClickListener(v -> {
            SharedPreferences sharedPreferences1 = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            String userId = sharedPreferences1.getString("userId", null);
            String name = sharedPreferences1.getString("name", null);
            String email = sharedPreferences1.getString("email", null);
            String department = sharedPreferences1.getString("department", null);

            Intent intent = new Intent(getApplicationContext(), Profile.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("NAME", name);
            intent.putExtra("EMAIL", email);
            intent.putExtra("DEPARTMENT", department);
            startActivity(intent);
        });

        toScan = findViewById(R.id.toScan);
        toScan.setOnClickListener(view -> {
            Intent intent = new Intent(HomePage.this, ScanQR.class);
            startActivity(intent);
        });

        toTutorial = findViewById(R.id.toTutorial);
        toTutorial.setOnClickListener(view -> {
            String emailToFind = editTest.getText().toString().trim();
            if (!emailToFind.isEmpty()) {
                findUserIdByEmail(emailToFind);
            } else {
                Toast.makeText(HomePage.this, "Please enter an email to search", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void findUserIdByEmail(String email) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://cipher-8035c-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean found = false;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userEmail = userSnapshot.child("email").getValue(String.class);
                    if (userEmail != null && userEmail.equals(email)) {
                        String userId = userSnapshot.getKey();
                        Toast.makeText(HomePage.this, "User ID: " + userId, Toast.LENGTH_LONG).show();
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Toast.makeText(HomePage.this, "Email not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HomePage.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
