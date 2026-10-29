package com.CPU.V.Vhynlain;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

public class TouchView extends View {
    public interface Listener {
        void onCount(int count);
    }

    private static final int[] COLORS = {
            0xFFEF5350, 0xFF66BB6A, 0xFF42A5F5, 0xFFFFCA28, 0xFFAB47BC,
            0xFF26C6DA, 0xFFFF7043, 0xFF9CCC65, 0xFFEC407A, 0xFF8D6E63
    };

    private final SparseArray<PointF> points = new SparseArray<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Listener listener;

    public TouchView(Context context) {
        this(context, null);
    }

    public TouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListener(Listener l) {
        listener = l;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getActionMasked();
        points.clear();
        if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL) {
            for (int i = 0; i < e.getPointerCount(); i++) {
                if (action == MotionEvent.ACTION_POINTER_UP && i == e.getActionIndex()) {
                    continue;
                }
                points.put(e.getPointerId(i), new PointF(e.getX(i), e.getY(i)));
            }
        }
        if (listener != null) {
            listener.onCount(points.size());
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float radius = 48 * getResources().getDisplayMetrics().density;
        for (int i = 0; i < points.size(); i++) {
            PointF p = points.valueAt(i);
            paint.setColor(COLORS[points.keyAt(i) % COLORS.length]);
            canvas.drawCircle(p.x, p.y, radius, paint);
        }
    }
}
