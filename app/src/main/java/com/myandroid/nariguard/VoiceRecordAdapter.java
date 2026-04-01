package com.myandroid.nariguard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class VoiceRecordAdapter
        extends RecyclerView.Adapter<VoiceRecordAdapter.ViewHolder> {

    private final ArrayList<VoiceRecord> list;
    private final VoiceRecordActivity activity;

    public VoiceRecordAdapter(ArrayList<VoiceRecord> list, VoiceRecordActivity activity) {
        this.list = list;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voice_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        VoiceRecord record = list.get(position);
        holder.tvName.setText(record.name);
        holder.tvTimestamp.setText(record.timestamp);

        holder.btnPlay.setOnClickListener(v -> activity.playRecording(record.path));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTimestamp;
        ImageButton btnPlay;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            btnPlay = itemView.findViewById(R.id.btnPlay);
        }
    }
}