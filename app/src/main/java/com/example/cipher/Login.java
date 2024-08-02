package com.example.cipher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

public class Login extends AppCompatActivity {

    private static final String TAG = "LoginActivity";  // Define TAG here

    EditText email, pw;
    String userEmail, userPW;
    ImageButton btn;
    FirebaseAuth mAuth;
    public static final String SHARED_PREFS = "sharedPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.loginemail);
        pw = findViewById(R.id.loginpassword);
        btn = findViewById(R.id.loginbutton);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userEmail = email.getText().toString().trim();
                userPW = pw.getText().toString().trim();

                if (validateInput(userEmail, userPW)) {
                    authenticateUser(userEmail, userPW);
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton createAccountButton = findViewById(R.id.createaccountbutton);
        createAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });
    }

    private void authenticateUser(String userEmail, String userPW) {
        mAuth.signInWithEmailAndPassword(userEmail, userPW).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();
                    Log.d(TAG, "User ID: " + userId);

                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userId", userId);
                    editor.putString("email", userEmail);
                    editor.apply();

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://cipher-8035c-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();
                    DatabaseReference userRef = databaseReference.child("users").child(userId);

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String username = dataSnapshot.child("name").getValue(String.class);
                            String department = dataSnapshot.child("department").getValue(String.class);


                            editor.putString("name", username);
                            editor.putString("department", department != null ? department : "Department");
                            editor.apply();

                            Log.d(TAG, "Login Successful: USER_ID = " + userId + ", NAME = " + username + ", DEPARTMENT = " + department);
                            Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, HomePage.class);
                            startActivity(intent);
                            finish();
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Error fetching user details: " + databaseError.getMessage());
                            Toast.makeText(Login.this, "Error fetching user details", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e(TAG, "User is null after successful sign-in");
                    Toast.makeText(Login.this, "Login failed: User data is null", Toast.LENGTH_SHORT).show();
                }
            } else {
                handleLoginError(task.getException());
            }
        });
    }

    private void handleLoginError(Exception exception) {
        String errorMessage;
        if (exception instanceof FirebaseAuthInvalidUserException) {
            errorMessage = "No account found with this email.";
            email.requestFocus();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Incorrect password.";
            pw.requestFocus();
        } else {
            errorMessage = "Authentication failed: " + exception.getMessage();
        }
        Toast.makeText(Login.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            this.email.setError("Email is required");
            this.email.requestFocus();
            return false;
        } else if (!isValidEmail(email)) {
            this.email.setError("Enter a valid email");
            this.email.requestFocus();
            return false;
        } else if (password.isEmpty()) {
            this.pw.setError("Password is required");
            this.pw.requestFocus();
            return false;
        } else if (password.length() < 6) {
            this.pw.setError("Password must be at least 6 characters");
            this.pw.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+$");
        return pattern.matcher(email).matches();
    }
}
