package com.example.cipher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ImportDoc extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    ImageButton toHome;
    Button selectFileButton, uploadFileButton;
    WebView webView;
    Uri fileUri;
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseAuth auth;
    FirebaseUser user;
    EditText recipientEmail;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_doc);

        // Request permissions if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_FILE_REQUEST);
        }

        toHome = findViewById(R.id.toHome);
        toHome.setOnClickListener(view -> {
            Intent intent = new Intent(ImportDoc.this, HomePage.class);
            startActivity(intent);
        });

        selectFileButton = findViewById(R.id.selectFileButton);
        uploadFileButton = findViewById(R.id.uploadFileButton);
        webView = findViewById(R.id.webView);
        recipientEmail = findViewById(R.id.recipientEmail);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        selectFileButton.setOnClickListener(view -> openFileChooser());
        uploadFileButton.setOnClickListener(view -> {
            String email = recipientEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                Toast.makeText(ImportDoc.this, "Checking recipient email...", Toast.LENGTH_SHORT).show();
                checkRecipientEmail(email);
            } else {
                Toast.makeText(ImportDoc.this, "No recipient email entered. Proceeding with upload...", Toast.LENGTH_SHORT).show();
                uploadFile();
            }
        });

        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Allow all file types
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();

            // Get the file name from the URI
            String fileName = getFileName(fileUri);
            Toast.makeText(this, "File Selected: " + fileName, Toast.LENGTH_LONG).show();
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void checkRecipientEmail(String email) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean emailFound = false;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userEmail = userSnapshot.child("email").getValue(String.class);
                    if (email.equals(userEmail)) {
                        emailFound = true;
                        break;
                    }
                }

                if (emailFound) {
                    Toast.makeText(ImportDoc.this, "Recipient Found", Toast.LENGTH_SHORT).show();
                    uploadFile();
                } else {
                    Toast.makeText(ImportDoc.this, "Recipient not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ImportDoc.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadFile() {
        if (fileUri != null && user != null) {
            String uid = user.getUid();
            String fileName = getFileName(fileUri);
            StorageReference userFolder = storageReference.child("user/" + uid + "/" + System.currentTimeMillis() + "_" + fileName);

            userFolder.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
                userFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                    Toast.makeText(ImportDoc.this, "Upload successful", Toast.LENGTH_SHORT).show();
                    webView.loadUrl(uri.toString());
                });
            }).addOnFailureListener(e -> Toast.makeText(ImportDoc.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "No file selected or user not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
}