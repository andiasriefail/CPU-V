package com.CPU.V.Vhynlain;

import android.hardware.Sensor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.Holder> {
    public interface OnClick {
        void onClick(int index);
    }

    private final List<Sensor> items;
    private final OnClick listener;

    public SensorAdapter(List<Sensor> items, OnClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Sensor s = items.get(position);
        h.label.setText(s.getVendor() + " \u00B7 " + SystemInfo.sensorType(s));
        h.value.setText(s.getName());
        h.itemView.setOnClickListener(v -> {
            int p = h.getBindingAdapterPosition();
            if (p != RecyclerView.NO_POSITION) {
                listener.onClick(p);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView label;
        final TextView value;

        Holder(View v) {
            super(v);
            label = v.findViewById(R.id.label);
            value = v.findViewById(R.id.value);
        }
    }
}
