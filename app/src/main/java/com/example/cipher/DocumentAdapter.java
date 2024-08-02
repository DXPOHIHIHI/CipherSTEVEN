package com.example.cipher;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private List<Document> documentList;
    private Context context;
    private String userId;

    public DocumentAdapter(List<Document> documentList, Context context, String userId) {
        this.documentList = documentList != null ? documentList : new ArrayList<>();
        this.context = context;
        this.userId = userId;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleitemlayout, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        Document document = documentList.get(position);

        if (document == null) {
            Log.w("DocumentAdapter", "Document at position " + position + " is null");
            return;
        }

        holder.documentName.setText(document.getDocumentName());
        holder.documentTitle.setText(document.getTitle());
        holder.senderName.setText(document.getSenderName());
        holder.senderEmail.setText(document.getSenderEmail());
        holder.senderDate.setText(formatTimestamp(document.getSenderTimestamp()));
        holder.senderStatus.setText(document.getSenderStatus());

        // Check if the document's sender status is "Successfully"
        if ("Successfully".equals(document.getSenderStatus())) {
            holder.documentButton.setVisibility(View.GONE);
        } else {
            holder.documentButton.setVisibility(View.VISIBLE);
        }

        if (userId.equals(document.getSenderUserId())) {
            holder.documentButton.setVisibility(View.GONE);
        } else {
            holder.documentButton.setVisibility(View.VISIBLE);
        }

        List<Recipient> recipients = document.getRecipients() != null ? document.getRecipients() : new ArrayList<>();
        RecipientAdapter recipientAdapter = new RecipientAdapter(recipients, context, userId);
        recipientAdapter.setOnSpinnerItemSelectedListener(new RecipientAdapter.OnSpinnerItemSelectedListener() {
            @Override
            public void onSpinnerItemSelected(String spinnerData, String recipientUserId) {
                holder.documentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateDocuments(recipientUserId, spinnerData, document.getDocumentName());
                        Toast.makeText(context, "Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        holder.recipientRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.recipientRecyclerView.setAdapter(recipientAdapter);
    }


    @Override
    public int getItemCount() {
        return documentList.size();
    }

    public static class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView documentName;
        TextView documentTitle;
        TextView senderName;
        TextView senderEmail;
        TextView senderDate;
        TextView senderStatus;
        RecyclerView recipientRecyclerView;
        Button documentButton;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            documentName = itemView.findViewById(R.id.documentName);
            documentTitle = itemView.findViewById(R.id.documentTitle);
            senderName = itemView.findViewById(R.id.senderName);
            senderEmail = itemView.findViewById(R.id.senderEmail);
            senderDate = itemView.findViewById(R.id.senderDate);
            senderStatus = itemView.findViewById(R.id.senderStatus);
            recipientRecyclerView = itemView.findViewById(R.id.recipientRecyclerView);
            documentButton = itemView.findViewById(R.id.documentButton);
        }
    }

    private String formatTimestamp(Long timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void updateDocuments(String recipientUserId, String dropdownData, String docId) {
        DatabaseReference documentRef = FirebaseDatabase.getInstance().getReference("documents").child(docId);
        DatabaseReference recipientsRef = documentRef.child("recipients");
        DatabaseReference senderRef = documentRef.child("sender");

        recipientsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean allStatusUpdated = true;

                for (DataSnapshot recipientSnapshot : dataSnapshot.getChildren()) {
                    String key = recipientSnapshot.getKey();
                    String status = recipientSnapshot.child("status").getValue(String.class);

                    if (key.equals(recipientUserId)) {
                        recipientSnapshot.getRef().child("status").setValue(dropdownData);
                        recipientSnapshot.getRef().child("timestamp").setValue(System.currentTimeMillis());

                        Log.d("DocumentUpdate", "Recipient " + key + " updated with status: " + dropdownData);
                    }

                    if (!status.equals("Read") && !status.equals("Modified") && !status.equals("Signed")) {
                        allStatusUpdated = false;
                    }
                }

                if (allStatusUpdated) {
                    senderRef.child("status").setValue("Successfully");
                    Log.d("DocumentUpdate", "All recipients updated. Sender status set to 'Successfully'");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("DatabaseError", databaseError.getMessage());
            }
        });
    }
}
