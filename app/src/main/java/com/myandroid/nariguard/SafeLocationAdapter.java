package com.myandroid.nariguard;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class SafeLocationAdapter
        extends RecyclerView.Adapter<SafeLocationAdapter.ViewHolder> {

    private ArrayList<SafeLocation> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SafeLocation location);
    }

    public SafeLocationAdapter(ArrayList<SafeLocation> list,
                               OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_safe_location, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        SafeLocation location = list.get(position);

        holder.tvPlaceName.setText(location.name);


        holder.tvDistance.setText(
                String.format(Locale.getDefault(),
                        "%.2f km away", location.distance)
        );


        if (location.isPolice()) {
            holder.imgType.setImageResource(R.drawable.police_siren);
        } else if ("hospital".equalsIgnoreCase(location.type)) {
            holder.imgType.setImageResource(R.drawable.hospital_logo);
        } else {
            holder.imgType.setImageResource(R.drawable.location);
        }


        holder.itemView.setOnClickListener(v ->
                listener.onItemClick(location)
        );


        holder.btnNavigate.setOnClickListener(v -> {

            Context context = v.getContext();

            Uri uri = Uri.parse(
                    "google.navigation:q="
                            + location.lat + "," + location.lon);

            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");

            PackageManager pm = context.getPackageManager();

            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent);
            } else {
                // ✅ fallback
                Intent browserIntent =
                        new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://maps.google.com/?q="
                                        + location.lat + "," + location.lon));
                context.startActivity(browserIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgType;
        TextView tvPlaceName, tvDistance;
        Button btnNavigate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgType = itemView.findViewById(R.id.imgType);
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            btnNavigate = itemView.findViewById(R.id.btnNavigate);
        }
    }
}