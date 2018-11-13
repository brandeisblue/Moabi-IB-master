package com.ivorybridge.moabi.ui.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class ImageBarChartRenderer extends BarChartRenderer {

    private final Bitmap barImage;
    private float bestValue;

    public ImageBarChartRenderer(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler, Bitmap barImage, float bestValue) {
        super(chart, animator, viewPortHandler);
        this.barImage = barImage;
        this.bestValue = bestValue;
    }

    @Override
    public void drawData(Canvas c) {
        initBuffers();
        super.drawData(c);
    }

    @Override
    protected void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
        //initBuffers();
        super.drawDataSet(c, dataSet, index);
        drawBarImages(c, dataSet, index);
    }

    protected void drawBarImages(Canvas c, IBarDataSet dataSet, int index) {
        BarBuffer buffer = mBarBuffers[index];

        float left; //avoid allocation inside loop
        float right;
        float top;
        float bottom;

        final Bitmap scaledBarImage = scaleBarImage(buffer);

        int starWidth = scaledBarImage.getWidth();
        int starOffset = starWidth / 2;

        for (int j = 0; j < buffer.buffer.length * mAnimator.getPhaseX(); j += 4) {
            left = buffer.buffer[j];
            right = buffer.buffer[j + 2];
            top = buffer.buffer[j + 1];
            bottom = buffer.buffer[j + 3];

            float x = (left + right) / 2f;

            if (!mViewPortHandler.isInBoundsRight(x))
                break;

            if (!mViewPortHandler.isInBoundsY(top)
                    || !mViewPortHandler.isInBoundsLeft(x))
                continue;

            BarEntry entry = dataSet.getEntryForIndex(j / 4);
            float val = entry.getY();

            if (val == this.bestValue) {
                drawImage(c, scaledBarImage, x - starOffset, top);
            }
        }
    }

    private Bitmap scaleBarImage(BarBuffer buffer) {
        float firstLeft = buffer.buffer[0];
        float firstRight = buffer.buffer[2];
        int firstWidth = (int) Math.ceil(firstRight - firstLeft);
        return Bitmap.createScaledBitmap(barImage, firstWidth, firstWidth, false);
    }

    protected void drawImage(Canvas c, Bitmap image, float x, float y) {
        if (image != null) {
            c.drawBitmap(image, x, y, null);
        }
    }
}
