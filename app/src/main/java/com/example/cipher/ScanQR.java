package com.example.cipher;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScanQR extends AppCompatActivity {

    private CodeScanner mCodeScanner;
    private String scannedContent;
    public static final int CAMERA_REQUEST_CODE = 101;

    private ImageButton toHome;
    private TextView scanTxt;

    Dialog dialog_send;
    Button cancelBtn, confirmBtn;
    EditText editEmail, editTitle;
    Spinner spinner1;
    ImageView plusIcon;

    private boolean isDialogDisplayed = false;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.scanqr);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Spinner dropdown = findViewById(R.id.spinner1);
        String[] items = new String[]{"Read", "Modify", "Sign", "Send"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        toHome = findViewById(R.id.toHome);
        scanTxt = findViewById(R.id.scanTxt);

        toHome.setOnClickListener(view -> {
            Intent intent = new Intent(ScanQR.this, HomePage.class);
            startActivity(intent);
        });

        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(() -> {
                    if (!isDialogDisplayed) {
                        isDialogDisplayed = true;
                        scannedContent = result.getText();
                        scanTxt.setText(scannedContent); // Set the scanned content to the TextView
                        showDialog();
                    }
                });
            }
        });

        scannerView.setOnClickListener(view -> mCodeScanner.startPreview());

        // Check and request camera permission
        setupPermissions();

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance("https://cipher-8035c-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();
    }

    // Initialize a counter to track the number of added EditTexts
    int editTextCounter = 0;

    private void showDialog() {
        // Stop the scanner to prevent further scans
        mCodeScanner.stopPreview();

        // Initialize and configure the dialog
        dialog_send = new Dialog(ScanQR.this);
        dialog_send.setContentView(R.layout.dialog_send);
        dialog_send.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_send.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialogbox_bg));
        dialog_send.setCancelable(false);

        // Find and configure dialog buttons and edit text
        cancelBtn = dialog_send.findViewById(R.id.cancelBtn);
        confirmBtn = dialog_send.findViewById(R.id.confirmBtn);
        editEmail = dialog_send.findViewById(R.id.editEmail);
        editTitle = dialog_send.findViewById(R.id.editTitle); // Add this line
        plusIcon = dialog_send.findViewById(R.id.plusIcon);
        ConstraintLayout constraintLayout = dialog_send.findViewById(R.id.constraintLayout);

        // Set click listeners
        cancelBtn.setOnClickListener(view -> {
            Log.d("ScanQR", "Cancel button clicked");
            dialog_send.dismiss();
            isDialogDisplayed = false;
            // Restart the camera preview after dismissing the dialog
            mCodeScanner.startPreview();
        });

        confirmBtn.setOnClickListener(v -> {
            collectAndProcessEmails();
        });

        plusIcon.setOnClickListener(view -> {
            // Create new EditText
            EditText newEditText = new EditText(ScanQR.this);
            newEditText.setId(View.generateViewId());
            newEditText.setLayoutParams(new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            ));
            newEditText.setHint("Email");
            newEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            newEditText.setBackgroundColor(getResources().getColor(android.R.color.white));
            newEditText.setEms(10);

            // Add the new EditText to the ConstraintLayout
            constraintLayout.addView(newEditText);

            // Adjust constraints
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);

            if (editTextCounter == 0) {
                constraintSet.connect(newEditText.getId(), ConstraintSet.TOP, R.id.editEmail, ConstraintSet.BOTTOM, 20);
            } else {
                constraintSet.connect(newEditText.getId(), ConstraintSet.TOP, editEmail.getId(), ConstraintSet.BOTTOM, 20);
            }
            constraintSet.connect(newEditText.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0);
            constraintSet.connect(newEditText.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0);

            constraintSet.applyTo(constraintLayout);

            // Update editEmail reference to the newly added EditText
            editEmail = newEditText;

            // Increment the counter
            editTextCounter++;
        });

        // Show the dialog
        dialog_send.show();
    }


    private void collectAndProcessEmails() {
        String title = editTitle.getText().toString().trim();
        ConstraintLayout constraintLayout = dialog_send.findViewById(R.id.constraintLayout);
        for (int i = 0; i < constraintLayout.getChildCount(); i++) {
            View view = constraintLayout.getChildAt(i);
            if (view instanceof EditText) {
                EditText editText = (EditText) view;
                String email = editText.getText().toString().trim();
                if (!email.isEmpty()) {
                    // Process the email and title
                    findUserIdByEmail(email, title);
                }
            }
        }
    }

    private void findUserIdByEmail(String email, String title) {
        DatabaseReference usersRef = databaseReference.child("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean found = false;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userEmail = userSnapshot.child("email").getValue(String.class);
                    if (userEmail != null && userEmail.equals(email)) {
                        String recipientId = userSnapshot.getKey();
                        Toast.makeText(ScanQR.this, "User ID: " + recipientId, Toast.LENGTH_LONG).show();
                        found = true;
                        addScannedDocument(recipientId, title);
                        break;
                    }
                }
                if (!found) {
                    Toast.makeText(ScanQR.this, "Email not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ScanQR.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addScannedDocument(String recipientId, String title) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        String action = "Send"; // Automatically set action to "Send"
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        DatabaseReference currentUserRef = databaseReference.child("users").child(currentUserId).child("scannedDocs").child(scannedContent);
        currentUserRef.child("timestamp").setValue(timestamp);
        currentUserRef.child("action").setValue(action);
        currentUserRef.child("title").setValue(title);

        DatabaseReference documentRef = databaseReference.child("documents").child(scannedContent);
        documentRef.child("createdBy").setValue(currentUserId);
        documentRef.child("title").setValue(title); // Add title here
        documentRef.child("recipients").child(recipientId).child("status").setValue("not scanned");
        documentRef.child("recipients").child(recipientId).child("timestamp").setValue("N/A");

        // Adding scannedDocs to the recipient's node
        DatabaseReference recipientRef = databaseReference.child("users").child(recipientId).child("scannedDocs").child(scannedContent);
        recipientRef.child("action").setValue(null);
        recipientRef.child("timestamp").setValue("N/A");
        recipientRef.child("title").setValue(title); // Add title here

        Toast.makeText(ScanQR.this, "Document scanned and recorded successfully", Toast.LENGTH_SHORT).show();
        dialog_send.dismiss();
        isDialogDisplayed = false;
        // Restart the camera preview after dismissing the dialog
        mCodeScanner.startPreview();
    }




    @Override
    protected void onResume() {
        super.onResume();
        if (!isDialogDisplayed) {
            mCodeScanner.startPreview();
        }
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    private void setupPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            makeRequest();
        } else {
            // Permission already granted, start scanner
            mCodeScanner.startPreview();
        }
    }

    private void makeRequest() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start scanner
                mCodeScanner.startPreview();
            } else {
                // Permission denied
                Toast.makeText(this, "You need camera permission to use the scanner", Toast.LENGTH_SHORT).show();
            }
        }
    }
}