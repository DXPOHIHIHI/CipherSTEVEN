package com.example.cipher;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    EditText email, name, pw;
    String userEmail, userName, userPW;
    ImageButton btn, backbtn;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        databaseReference = FirebaseDatabase.getInstance("https://cipher-8035c-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();
        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.registeremail);
        name = findViewById(R.id.registername);
        pw = findViewById(R.id.registerpassword);
        btn = findViewById(R.id.loginbutton2);
        backbtn = findViewById(R.id.backbutton);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userEmail = email.getText().toString().trim();
                userName = name.getText().toString().trim();
                userPW = pw.getText().toString().trim();

                if (userEmail.isEmpty()) {
                    email.setError("Email is required");
                } else if (!isValidEmail(userEmail)) {
                    email.setError("Enter a valid email");
                } else if (userName.isEmpty()) {
                    name.setError("Name is required");
                } else if (userPW.isEmpty()) {
                    pw.setError("Password is required");
                } else if (userPW.length() < 6) {
                    pw.setError("Password must be at least 6 characters");
                } else {
                    authenticationCheck();
                }
            }
        });

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate back to the Login activity
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
                finish(); // Optional: Call this if you want to remove Register activity from back stack
            }
        });
    }

    private void authenticationCheck() {
        mAuth.createUserWithEmailAndPassword(userEmail, userPW).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Register.this, "Created Successfully", Toast.LENGTH_SHORT).show();

                    String currentUserID = mAuth.getCurrentUser().getUid();
                    HashMap<String, Object> userdataMap = new HashMap<>();
                    userdataMap.put("email", userEmail); // Use string values
                    userdataMap.put("name", userName);   // Use string values

                    databaseReference.child("users").child(currentUserID).updateChildren(userdataMap);
                } else {
                    Toast.makeText(Register.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+$");
        return pattern.matcher(email).matches();
    }
}

//{
//  /* Visit https://firebase.google.com/docs/database/security to learn more about security rules. */
//  "rules": {
//    ".read": false,
//    ".write": false
//  }
//}
