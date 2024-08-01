package com.example.cipher;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;

public class StorageActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> fileList;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        listView = findViewById(R.id.fileListView);
        fileList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList);
        listView.setAdapter(adapter);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            listUserFiles(userId);
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void listUserFiles(String userId) {
        StorageReference userStorageRef = storageReference.child("user/" + userId);
        userStorageRef.listAll().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ListResult result = task.getResult();
                fileList.clear(); // Clear existing items
                for (StorageReference fileRef : result.getItems()) {
                    fileList.add(fileRef.getName());
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Failed to list files", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
