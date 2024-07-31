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

    Dialog dialog_send, dialog_receive;
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
                        handleScanResult(scannedContent);
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

    private void handleScanResult(String scannedContent) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = databaseReference.child("users").child(currentUserId).child("scannedDocs");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean found = false;
                for (DataSnapshot docSnapshot : dataSnapshot.getChildren()) {
                    if (docSnapshot.getKey().equals(scannedContent)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    showDialogReceive();
                } else {
                    showDialogSend();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ScanQR.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                isDialogDisplayed = false;
                mCodeScanner.startPreview();
            }
        });
    }

    private void showDialogReceive() {
        dialog_receive = new Dialog(ScanQR.this);
        dialog_receive.setContentView(R.layout.dialog_receive);
        dialog_receive.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_receive.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialogbox_bg));
        dialog_receive.setCancelable(false);

        Button cancelBtnReceive = dialog_receive.findViewById(R.id.cancelBtn);
        Button confirmBtnReceive = dialog_receive.findViewById(R.id.confirmBtn);
        Spinner spinner1 = dialog_receive.findViewById(R.id.spinner1);

        // Set choices for spinner1
        String[] spinnerItems = new String[]{"Read", "Signed", "Modified"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerItems);
        spinner1.setAdapter(spinnerAdapter);

        cancelBtnReceive.setOnClickListener(view -> {
            dialog_receive.dismiss();
            isDialogDisplayed = false;
            mCodeScanner.startPreview();
        });

        confirmBtnReceive.setOnClickListener(view -> {
            // Handle confirm action
            updateReceiver(spinner1);
            dialog_receive.dismiss();
            isDialogDisplayed = false;
            mCodeScanner.startPreview();
        });

        dialog_receive.show();
    }


    int editTextCounter = 0;

    private void showDialogSend() {
        dialog_send = new Dialog(ScanQR.this);
        dialog_send.setContentView(R.layout.dialog_send);
        dialog_send.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_send.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialogbox_bg));
        dialog_send.setCancelable(false);

        cancelBtn = dialog_send.findViewById(R.id.cancelBtn);
        confirmBtn = dialog_send.findViewById(R.id.confirmBtn);
        editEmail = dialog_send.findViewById(R.id.editEmail);
        editTitle = dialog_send.findViewById(R.id.editTitle); // Add this line
        plusIcon = dialog_send.findViewById(R.id.plusIcon);
        ConstraintLayout constraintLayout = dialog_send.findViewById(R.id.constraintLayout);

        cancelBtn.setOnClickListener(view -> {
            dialog_send.dismiss();
            isDialogDisplayed = false;
            mCodeScanner.startPreview();
        });

        confirmBtn.setOnClickListener(v -> {
            collectAndProcessEmails();
            dialog_send.dismiss();
            isDialogDisplayed = false;
            mCodeScanner.startPreview();
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

            editEmail = newEditText;

            editTextCounter++;
        });

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
                        String userId = userSnapshot.getKey();
                        updateRecipientData(userId, title);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Toast.makeText(ScanQR.this, "User not found for email: " + email, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ScanQR.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecipientData(String userId, String title) {
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
        documentRef.child("recipients").child(userId).child("status").setValue("not scanned");
        documentRef.child("recipients").child(userId).child("timestamp").setValue("N/A");

        // Adding scannedDocs to the recipient's node
        DatabaseReference recipientRef = databaseReference.child("users").child(userId).child("scannedDocs").child(scannedContent);
        recipientRef.child("action").setValue(null);
        recipientRef.child("timestamp").setValue("N/A");
        recipientRef.child("title").setValue(title);

        Toast.makeText(ScanQR.this, "Data sent successfully!", Toast.LENGTH_SHORT).show();
    }

    private void updateReceiver(Spinner spinner1) {
        if (spinner1 != null) {
            String spinnerAction = spinner1.getSelectedItem().toString();
            Log.d("SpinnerAction", spinnerAction + " chosen");

            // Get current user ID
            String currentUserId = mAuth.getCurrentUser().getUid();

            // Get references
            DatabaseReference currentUserRef = databaseReference.child("users").child(currentUserId);
            DatabaseReference scannedDocsRef = currentUserRef.child("scannedDocs").child(scannedContent);
            DatabaseReference documentRef = databaseReference.child("documents").child(scannedContent);

            // Get the name value
            currentUserRef.child("name").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String name = task.getResult().getValue(String.class);
                    Log.d("UserName", "Name: " + name);

                    // Update receiver node
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    scannedDocsRef.child("timestamp").setValue(timestamp);
                    scannedDocsRef.child("action").setValue(spinnerAction);

                    // Update documents node
                    documentRef.child("recipients").child(currentUserId).child("status").setValue(spinnerAction);
                    documentRef.child("recipients").child(currentUserId).child("timestamp").setValue(timestamp);
                    documentRef.child("recipients").child(currentUserId).child("name").setValue(name); // Set name value here

                } else {
                    Log.e("DatabaseError", "Error fetching name: " + task.getException().getMessage());
                }
            });

        } else {
            Log.e("updateReceiver", "spinner1 is null");
        }
    }


    private void setupPermissions() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest();
        }
    }

    private void makeRequest() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You need the camera permission to be able to use this app", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}
