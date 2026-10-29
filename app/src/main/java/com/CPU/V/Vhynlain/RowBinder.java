package com.CPU.V.Vhynlain;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public final class RowBinder {
    private RowBinder() {
    }

    public static void bind(LinearLayout parent, List<String[]> rows) {
        if (parent.getChildCount() != rows.size()) {
            parent.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            for (int i = 0; i < rows.size(); i++) {
                inflater.inflate(R.layout.item_row, parent, true);
            }
        }
        for (int i = 0; i < rows.size(); i++) {
            View v = parent.getChildAt(i);
            ((TextView) v.findViewById(R.id.label)).setText(rows.get(i)[0]);
            ((TextView) v.findViewById(R.id.value)).setText(rows.get(i)[1]);
            final String text = rows.get(i)[0] + ": " + rows.get(i)[1];
            v.setOnLongClickListener(view -> {
                android.content.ClipboardManager cm = (android.content.ClipboardManager) view.getContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(android.content.ClipData.newPlainText("CPU-V", text));
                android.widget.Toast.makeText(view.getContext(), R.string.copied, android.widget.Toast.LENGTH_SHORT).show();
                return true;
            });
        }
    }
}
