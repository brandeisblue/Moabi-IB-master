package com.ivorybridge.moabi.util.wordcloud;

import android.graphics.Paint;
import android.text.style.LineHeightSpan;

import java.util.Random;

public class RandomLineHeightSpan implements LineHeightSpan {

    private final int height;

    RandomLineHeightSpan() {
        Random random = new Random();
        this.height = random.nextInt(24);
    }

    @Override
    public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v,
                             Paint.FontMetricsInt fm) {
        fm.bottom += height;
        fm.descent += height;
    }
}
