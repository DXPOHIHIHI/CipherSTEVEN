package com.example.cipher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {
    ImageButton toHome, logout, Submit;
    public static final String SHARED_PREFS = "sharedPrefs";
    private static final String TAG = "ProfileActivity";

    TextView UserIds, Email, Name, Department;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        logout = findViewById(R.id.profilelogoutbutton);
        logout.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("name", "");
            editor.apply();

            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        toHome = findViewById(R.id.toHome);
        toHome.setOnClickListener(view -> {
            Intent intent = new Intent(Profile.this, HomePage.class);
            startActivity(intent);
        });

        UserIds = findViewById(R.id.profilename2);
        Email = findViewById(R.id.profileemail);
        Name = findViewById(R.id.profilename);
        Department = findViewById(R.id.ProfileDepartment);
        Submit = findViewById(R.id.loginbutton3);
        mAuth = FirebaseAuth.getInstance();

        Intent intents = getIntent();
        String userId = intents.getStringExtra("USER_ID");
        String email = intents.getStringExtra("EMAIL");
        String name = intents.getStringExtra("NAME");
        String department = intents.getStringExtra("DEPARTMENT");

        Log.d(TAG, "Received Intent Data: USER_ID = " + userId + ", EMAIL = " + email + ", NAME = " + name + ", DEPARTMENT = " + department);

        if (userId == null || email == null || name == null) {
            Log.e(TAG, "Missing Intent Extras: Closing Activity");
            finish();
            return;
        }

        UserIds.setText(userId);
        Email.setText(email);
        Name.setText(name);

        userRef = FirebaseDatabase.getInstance("https://cipher-8035c-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("users").child(userId);

        if (department != null) {
            Department.setText(department);
        } else {
            checkAndSetDepartment(userId);
        }

        Submit.setOnClickListener(v -> {
            String newName = Name.getText().toString();
            String newDepartment = Department.getText().toString();
            Log.d(TAG, "Updating User Information: NAME = " + newName + ", DEPARTMENT = " + newDepartment);
            updateUserInformation(userId, newName, newDepartment);
        });
    }

    private void checkAndSetDepartment(String userId) {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.e(TAG, "User ID not found: " + userId);
                    Toast.makeText(Profile.this, "User not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                String department = dataSnapshot.child("department").getValue(String.class);
                if (department == null) {
                    Log.d(TAG, "Department not found, setting default value");
                    userRef.child("department").setValue("Department");
                    Department.setText("Department");
                } else {
                    Log.d(TAG, "Fetched Department: " + department);
                    Department.setText(department);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                handleDatabaseError(databaseError);
            }
        });
    }

    private void updateUserInformation(String userId, String newName, String newDepartment) {
        userRef.child("name").setValue(newName);
        userRef.child("department").setValue(newDepartment)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User information updated successfully");
                    Toast.makeText(Profile.this, "User information updated", Toast.LENGTH_SHORT).show();


                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("name", newName);
                    editor.putString("department", newDepartment);
                    editor.apply();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Update failed: " + e.getMessage());
                    Toast.makeText(Profile.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void handleDatabaseError(DatabaseError databaseError) {
        Toast.makeText(Profile.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Database error: " + databaseError.getMessage());
    }
}
