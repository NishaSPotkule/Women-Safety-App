package com.myandroid.nariguard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SafetyTipsAdapter extends RecyclerView.Adapter<SafetyTipsAdapter.ViewHolder> {

    private List<SafetyModel> tipsList;

    public SafetyTipsAdapter(List<SafetyModel> tipsList) {
        this.tipsList = tipsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_safety_tips, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SafetyModel model = tipsList.get(position);
        holder.title.setText(model.getTitle());

        // Remove any existing tips
        holder.tipsLayout.removeAllViews();

        // Add all tips as TextViews
        for (String tip : model.getTips()) {
            TextView tv = new TextView(holder.itemView.getContext());
            tv.setText("• " + tip);
            tv.setPadding(10, 5, 10, 5);
            holder.tipsLayout.addView(tv);
        }

        // Expand/collapse on title click
        holder.title.setOnClickListener(v -> {
            if (holder.tipsLayout.getVisibility() == View.GONE) {
                holder.tipsLayout.setVisibility(View.VISIBLE);
            } else {
                holder.tipsLayout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tipsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        LinearLayout tipsLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTopicTitle);
            tipsLayout = itemView.findViewById(R.id.layoutTips);
        }
    }
}
