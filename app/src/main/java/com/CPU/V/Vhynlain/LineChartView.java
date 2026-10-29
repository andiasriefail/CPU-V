package com.CPU.V.Vhynlain;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.Locale;

public class LineChartView extends View {
    private static final int MAX_POINTS = 60;

    private final ArrayList<float[]> points = new ArrayList<>();
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint grid = new Paint();
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int[] colors = new int[3];
    private final float density;
    private boolean fixed;
    private float fixedMin;
    private float fixedMax;

    public LineChartView(Context context) {
        this(context, null);
    }

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = context.getResources().getDisplayMetrics().density;
        int on = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, 0xFF888888);
        colors[0] = MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary, 0xFF00695C);
        colors[1] = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary, 0xFFEF6C00);
        colors[2] = 0xFF7E57C2;
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(2 * density);
        line.setStrokeJoin(Paint.Join.ROUND);
        fill.setStyle(Paint.Style.FILL);
        grid.setColor((on & 0x00FFFFFF) | 0x33000000);
        grid.setStrokeWidth(density);
        text.setColor(on);
        text.setTextSize(11 * density);
    }

    public void setFixedRange(float min, float max) {
        fixed = true;
        fixedMin = min;
        fixedMax = max;
    }

    public void add(float... values) {
        points.add(values.clone());
        if (points.size() > MAX_POINTS) {
            points.remove(0);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();
        float left = 8 * density;
        float right = 8 * density;
        float top = 18 * density;
        float bottom = 8 * density;
        float cw = w - left - right;
        float ch = h - top - bottom;

        for (int i = 0; i <= 4; i++) {
            float y = top + ch * i / 4f;
            canvas.drawLine(left, y, w - right, y, grid);
        }
        if (points.isEmpty()) {
            return;
        }

        int series = 1;
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        for (float[] p : points) {
            series = Math.max(series, Math.min(3, p.length));
            for (int s = 0; s < Math.min(3, p.length); s++) {
                min = Math.min(min, p[s]);
                max = Math.max(max, p[s]);
            }
        }
        if (fixed) {
            min = fixedMin;
            max = fixedMax;
        }
        if (max - min < 1e-6f) {
            max = min + 1f;
        }

        int size = points.size();
        for (int s = 0; s < series; s++) {
            Path path = new Path();
            float firstX = 0;
            float lastX = 0;
            for (int k = 0; k < size; k++) {
                float[] p = points.get(k);
                float v = s < p.length ? p[s] : 0f;
                float x = left + cw * (k + MAX_POINTS - size) / (MAX_POINTS - 1f);
                float y = top + ch * (1f - (v - min) / (max - min));
                if (k == 0) {
                    path.moveTo(x, y);
                    firstX = x;
                } else {
                    path.lineTo(x, y);
                }
                lastX = x;
            }
            if (s == 0 && size > 1) {
                Path area = new Path(path);
                area.lineTo(lastX, top + ch);
                area.lineTo(firstX, top + ch);
                area.close();
                fill.setColor((colors[0] & 0x00FFFFFF) | 0x33000000);
                canvas.drawPath(area, fill);
            }
            line.setColor(colors[s]);
            canvas.drawPath(path, line);
        }

        canvas.drawText(String.format(Locale.US, "%.2f", max), left, 12 * density, text);
        canvas.drawText(String.format(Locale.US, "%.2f", min), left, h - 2 * density, text);
    }
}
