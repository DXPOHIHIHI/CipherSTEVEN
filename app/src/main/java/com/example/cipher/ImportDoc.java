package com.example.cipher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.Manifest;

import android.database.Cursor;
import android.provider.OpenableColumns;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        selectFileButton.setOnClickListener(view -> openFileChooser());
        uploadFileButton.setOnClickListener(view -> uploadFile());

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

    private void uploadFile() {
        if (fileUri != null) {
            StorageReference fileReference = storageReference.child("uploads/" + System.currentTimeMillis());

            fileReference.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    Toast.makeText(ImportDoc.this, "Upload successful", Toast.LENGTH_SHORT).show();
                    webView.loadUrl(uri.toString());
                });
            }).addOnFailureListener(e -> Toast.makeText(ImportDoc.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }
}
