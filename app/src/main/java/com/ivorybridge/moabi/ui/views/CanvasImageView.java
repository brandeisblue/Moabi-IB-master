package com.ivorybridge.moabi.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.widget.ImageView;

public class CanvasImageView extends ImageView {

    private Paint p;

    public CanvasImageView(Context context) {
        super(context);
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0, 0, 20, 20, p);
    }
}
