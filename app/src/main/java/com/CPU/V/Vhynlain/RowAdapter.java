package com.CPU.V.Vhynlain;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RowAdapter extends RecyclerView.Adapter<RowAdapter.Holder> {
    public interface OnClick {
        void onClick(int index);
    }

    private List<String[]> rows = new ArrayList<>();
    private final OnClick listener;

    public RowAdapter(OnClick listener) {
        this.listener = listener;
    }

    public void setRows(List<String[]> newRows) {
        rows = newRows;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        h.label.setText(rows.get(position)[0]);
        h.value.setText(rows.get(position)[1]);
        if (listener != null) {
            h.itemView.setOnClickListener(v -> {
                int p = h.getBindingAdapterPosition();
                if (p != RecyclerView.NO_POSITION) {
                    listener.onClick(p);
                }
            });
        } else {
            h.itemView.setOnClickListener(null);
            h.itemView.setClickable(false);
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
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
