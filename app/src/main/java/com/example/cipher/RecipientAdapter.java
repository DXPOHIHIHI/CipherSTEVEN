package com.example.cipher;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecipientAdapter extends RecyclerView.Adapter<RecipientAdapter.RecipientViewHolder> {

    private List<Recipient> recipientList;
    private Context context;
    private String userId;
    private OnSpinnerItemSelectedListener onSpinnerItemSelectedListener;

    public interface OnSpinnerItemSelectedListener {
        void onSpinnerItemSelected(String spinnerData, String userId);
    }

    public RecipientAdapter(List<Recipient> recipientList, Context context, String userId) {
        this.recipientList = recipientList != null ? recipientList : new ArrayList<>();
        this.context = context;
        this.userId = userId;
    }

    public void setOnSpinnerItemSelectedListener(OnSpinnerItemSelectedListener listener) {
        this.onSpinnerItemSelectedListener = listener;
    }

    @NonNull
    @Override
    public RecipientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipients, parent, false);
        return new RecipientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipientViewHolder holder, int position) {
        Recipient recipient = recipientList.get(position);

        holder.recipientName.setText(recipient.getName());
        holder.recipientStatus.setText(recipient.getStatus());

        Long timestamp = recipient.getTimestamp();
        if (timestamp != null) {
            holder.recipientTimestamp.setText(formatTimestamp(timestamp));
        } else {
            holder.recipientTimestamp.setText("Pending Time Received");
        }

        if (userId != null && userId.equals(recipient.getUserId())) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    context, R.array.recipient_status_array, android.R.layout.simple_spinner_item
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.recipientSpinner.setAdapter(adapter);
            holder.recipientSpinner.setVisibility(View.VISIBLE);
        } else {
            holder.recipientSpinner.setVisibility(View.GONE);
        }

        holder.recipientSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                if (onSpinnerItemSelectedListener != null) {
                    onSpinnerItemSelectedListener.onSpinnerItemSelected(selectedItem, userId);
                }
                Log.d("Spinner", "Selected Item: " + selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipientList.size();
    }

    public static class RecipientViewHolder extends RecyclerView.ViewHolder {
        TextView recipientName;
        TextView recipientStatus;
        TextView recipientTimestamp;
        Spinner recipientSpinner;

        public RecipientViewHolder(@NonNull View itemView) {
            super(itemView);
            recipientName = itemView.findViewById(R.id.recipientName);
            recipientStatus = itemView.findViewById(R.id.recipientStatus);
            recipientTimestamp = itemView.findViewById(R.id.recipientTimestamp);
            recipientSpinner = itemView.findViewById(R.id.recipientSpinner);
        }
    }

    private String formatTimestamp(Long timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
