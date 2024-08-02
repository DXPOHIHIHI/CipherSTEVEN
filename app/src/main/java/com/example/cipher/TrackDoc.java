package com.example.cipher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrackDoc extends AppCompatActivity {

    private static final String TAG = "TrackDoc";

    private RecyclerView recyclerView;
    private DocumentAdapter adapter;
    private List<Document> documentList = new ArrayList<>();
    private DatabaseReference userRef;
    private DatabaseReference documentRef;

    ImageButton tohome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_doc);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tohome = findViewById(R.id.toHome);
        tohome.setOnClickListener(view -> {
            Intent intent = new Intent(TrackDoc.this, HomePage.class);
            startActivity(intent);
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        userRef = database.getReference("users");
        documentRef = database.getReference("documents");

        Intent intent = getIntent();
        String userId = intent.getStringExtra("USER_ID");

        if (userId != null) {
            adapter = new DocumentAdapter(documentList,this, userId);
            recyclerView.setAdapter(adapter);
            fetchUserScannedDocs(userId);
        } else {
            Log.e(TAG, "No USER_ID provided");
        }
    }

    private void fetchUserScannedDocs(String userId) {
        userRef.child(userId).child("scannedDocs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String docTitle = snapshot.getKey();
                    fetchAllDocuments(userId, docTitle);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching user scanned docs", databaseError.toException());
            }
        });
    }

    private void fetchAllDocuments(String userId, String title) {
        documentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String docId = snapshot.getKey();

                        if (docId != null && docId.equals(title)) {
                            try {
                                String documentTitle = snapshot.child("title").getValue(String.class);
                                String senderName = snapshot.child("sender").child("name").getValue(String.class);
                                String senderEmail = snapshot.child("sender").child("email").getValue(String.class);
                                Long senderTimestamp = getLongValue(snapshot.child("sender").child("timestamp"));
                                String senderStatus = snapshot.child("sender").child("status").getValue(String.class);
                                String senderId = snapshot.child("sender").child("senderId").getValue(String.class);
                                List<Recipient> recipients = new ArrayList<>();
                                DataSnapshot recipientsSnapshot = snapshot.child("recipients");

                                if (recipientsSnapshot.exists()) {
                                    for (DataSnapshot recipientSnapshot : recipientsSnapshot.getChildren()) {
                                        try {
                                            String recipientName = recipientSnapshot.child("name").getValue(String.class);
                                            String recipientStatus = recipientSnapshot.child("status").getValue(String.class);
                                            Long recipientTimestamp = getLongValue(recipientSnapshot.child("timestamp"));
                                            String recipientEmail = recipientSnapshot.child("email").getValue(String.class);
                                            String recipientUserId = recipientSnapshot.getKey(); // Get the recipient's key

                                            if (recipientName != null && recipientStatus != null) {
                                                recipients.add(new Recipient(recipientName, recipientStatus, recipientEmail, recipientTimestamp, recipientUserId));
                                            } else {
                                                Log.e(TAG, "Recipient data is incomplete for recipient ID: " + recipientSnapshot.getKey());
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error processing recipient data", e);
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "No recipients found for document ID: " + docId);
                                }

                                Document document = new Document(docId, documentTitle, senderName, senderEmail, senderTimestamp, senderStatus, senderId, recipients);
                                documentList.add(document);

                            } catch (Exception e) {
                                Log.e(TAG, "Error processing document data", e);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();

                } catch (Exception e) {
                    Log.e(TAG, "Error fetching document details", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching document details", databaseError.toException());
            }
        });
    }

    private Long getLongValue(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            Object value = dataSnapshot.getValue();
            if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof String) {
                String stringValue = (String) value;
                if ("N/A".equals(stringValue)) {
                    Log.w(TAG, "Timestamp value is 'N/A', returning null.");
                    return null;
                }
                try {
                    return Long.parseLong(stringValue);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing timestamp string to long", e);
                }
            } else {
                Log.e(TAG, "Unexpected data type for timestamp: " + value);
            }
        } else {
            Log.e(TAG, "DataSnapshot does not exist");
        }
        return null;
    }
}
