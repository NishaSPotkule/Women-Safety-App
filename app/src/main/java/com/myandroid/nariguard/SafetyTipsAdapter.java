package com.myandroid.nariguard;

import android.app.Dialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SafetyTipsAdapter extends RecyclerView.Adapter<SafetyTipsAdapter.ViewHolder> {

    private List<SafetyModel> list;

    public SafetyTipsAdapter(List<SafetyModel> list) {
        this.list = list;
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

        SafetyModel model = list.get(position);

        // ⚠️ MUST MATCH item_safety_tips.xml
        holder.title.setText(model.getTitle());

        holder.itemView.setOnClickListener(v -> {

            Dialog dialog = new Dialog(holder.itemView.getContext());
            dialog.setContentView(R.layout.dialog_safety_tip);

            TextView dialogTitle = dialog.findViewById(R.id.tvTopicTitle);
            LinearLayout tipsLayout = dialog.findViewById(R.id.layoutTips);
            TextView btnAcknowledge = dialog.findViewById(R.id.btnAcknowledge);

            dialogTitle.setText(model.getTitle());
            tipsLayout.removeAllViews();

            List<String> tips = model.getTips();

            for (int i = 0; i < tips.size(); i++) {

                String tip = tips.get(i);

                LinearLayout container = new LinearLayout(holder.itemView.getContext());
                container.setOrientation(LinearLayout.VERTICAL);
                container.setPadding(0, 25, 0, 25);

                TextView tipTitle = new TextView(holder.itemView.getContext());
                tipTitle.setText("• " + tip);
                tipTitle.setTextSize(20);
                tipTitle.setTextColor(Color.parseColor("#FFE7EC"));

                TextView desc = new TextView(holder.itemView.getContext());
                desc.setText("Follow this safety practice carefully for better protection.");
                desc.setTextSize(15);
                desc.setTextColor(Color.parseColor("#9F8F94"));
                desc.setPadding(0, 10, 0, 0);

                container.addView(tipTitle);
                container.addView(desc);

                tipsLayout.addView(container);

                // Divider except last item
                if (i != tips.size() - 1) {
                    View divider = new View(holder.itemView.getContext());
                    divider.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                    ));
                    divider.setBackgroundColor(Color.parseColor("#1B1517"));

                    tipsLayout.addView(divider);
                }
            }


            if (btnAcknowledge != null) {
                btnAcknowledge.setOnClickListener(v1 -> dialog.dismiss());
            }

            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            title = itemView.findViewById(R.id.tvCategory);
        }
    }
}