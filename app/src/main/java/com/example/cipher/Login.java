package com.example.cipher;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.regex.Pattern;

public class Login extends AppCompatActivity {

    EditText email, pw;
    String userEmail, userPW;
    ImageButton btn;
    FirebaseAuth mAuth;

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

    private void authenticateUser(String userEmail, String userPW) {
        mAuth.signInWithEmailAndPassword(userEmail, userPW).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    String userId = user != null ? user.getUid() : null;
                    Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    // Redirect to main activity or another activity
                    Intent intent = new Intent(Login.this, HomePage.class); // Replace MainActivity.class with your main activity
                    startActivity(intent);
                    finish();
                } else {
                    handleLoginError(task.getException());
                }
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
}
