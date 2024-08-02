package com.example.cipher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.firebase.storage.StorageReference;
import java.util.List;

public class FileListAdapter extends BaseAdapter {

    private Context context;
    private List<StorageReference> fileList;
    private LayoutInflater inflater;

    public FileListAdapter(Context context, List<StorageReference> fileList) {
        this.context = context;
        this.fileList = fileList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public Object getItem(int position) {
        return fileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, parent, false);
        }

        TextView fileNameTextView = convertView.findViewById(R.id.fileName);
        ImageButton downloadButton = convertView.findViewById(R.id.downloadButton);

        StorageReference fileRef = fileList.get(position);
        fileNameTextView.setText(fileRef.getName());

        downloadButton.setOnClickListener(view -> {
            if (context instanceof StorageActivity) {
                ((StorageActivity) context).downloadFile(fileRef);
            }
        });

        return convertView;
    }
}
