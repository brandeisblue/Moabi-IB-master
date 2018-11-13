package com.ivorybridge.moabi.util.wordcloud;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.ivorybridge.moabi.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import androidx.core.content.ContextCompat;

public class WordCloud extends AutoResizeTextView {

    private static final String TAG = WordCloud.class.getSimpleName();

    List<WordCloud> data;
    List<String> newData;
    List<SpannableString> spannableList;
    SpannableStringBuilder spannableStringBuilder;
    List<Float> frequencyList;
    List<Long> typeList;
    Context context;
    public Long EDUCATION_OR_CAREER = 0L;
    public Long SOCIAL = 1L;
    public Long RECREATION_OR_INTERESTS = 2L;
    public Long MIND_PHYSICAL_SPIRITUAL = 3L;
    public Long CHORE = 4L;

    public WordCloud(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void create(final List<WordCloudEntry> data) {

        StringBuilder stringBuilder = new StringBuilder();
        newData = new ArrayList<>();
        frequencyList = new ArrayList<>();
        typeList = new ArrayList<>();
        spannableStringBuilder = new SpannableStringBuilder("");

        Random rand = new Random();
        for (int i = 0; i < data.size(); i++) {

            int r = rand.nextInt(3) + 1;
            String newString = data.get(i).getEntryName();
            for (int j = 0; j < r; j++) {
                newString = newString + " ";
            }
            newData.add(newString);
            Float freq = (float) data.get(i).getNumberOfEntry();
            frequencyList.add(freq);
            typeList.add(data.get(i).getEntryType());
        }


        int commonDenomIndex = frequencyList.indexOf(Collections.max(frequencyList));
        float commonDenom = frequencyList.get(commonDenomIndex);
        int middleIndex = newData.size() / 2;
        String middleString = newData.get(middleIndex);
        float middleFrequency = frequencyList.get(middleIndex);
        String maxString = newData.get(commonDenomIndex);
        Long middleType = typeList.get(middleIndex);
        Long maxType = typeList.get(commonDenomIndex);
        /*
        newData.set(middleIndex, maxString);
        newData.set(commonDenomIndex, middleString);
        frequencyList.set(middleIndex, commonDenom);
        frequencyList.set(commonDenomIndex, middleFrequency);
        typeList.set(middleIndex, maxType);
        typeList.set(commonDenomIndex, middleType);*/

        for (int i = 0; i < frequencyList.size(); i++) {
            frequencyList.set(i, frequencyList.get(i) / commonDenom);
            spannableStringBuilder.append(newData.get(i));
        }

        Log.i(TAG, frequencyList.toString());

        int start = 0;
        int spanEnd;
        int charEnd;
        for (int i = 0; i < data.size(); i++) {
            charEnd = start + newData.get(i).trim().length();
            spanEnd = start + newData.get(i).length();
            int r = rand.nextInt(20) + 5;
            spannableStringBuilder.setSpan(new RelativeSizeSpan(frequencyList.get(i)), start, charEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (typeList.get(i).equals(EDUCATION_OR_CAREER)) {
                spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.activity_eduwork)), start, charEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (typeList.get(i).equals(SOCIAL)) {
                spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.activity_social)), start, charEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (typeList.get(i).equals(RECREATION_OR_INTERESTS)) {
                spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.activity_rec)), start, charEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (typeList.get(i).equals(MIND_PHYSICAL_SPIRITUAL)) {
                spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.activity_mbs)), start, charEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (typeList.get(i).equals(CHORE)) {
                spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.activity_chores)), start, charEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            //spannableStringBuilder.setSpan(new PaddingBackgroundColorSpan(ContextCompat.getColor(getContext(), R.color.white), r), start, charEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            //spannableStringBuilder.setSpan(new RandomLineHeightSpan(), start, charEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = spanEnd;
        }

        //this.setMovementMethod(LinkMovementMethod.getInstance());
        this.setHighlightColor(Color.TRANSPARENT);
        this.setText(spannableStringBuilder, BufferType.SPANNABLE);

        /*
        spannableList = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            spannableList.add((SpannableString) this.getText());
        }


        int start = 0;
        int end;
        for (int i = 0; i < spannableList.size(); i++) {
            end = start + newData.get(i).length();
            final int r = rand.nextInt(256);
            final int g = rand.nextInt(256);
            final int b = rand.nextInt(256);
            spannableList.get(i).setSpan(new ForegroundColorSpan(Color.rgb(r, g, b)), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            start = end + 1;
        }*/
        //this.setLineSpacing(0, 0.7f);
        this.requestLayout();
    }

    public void setRandomSize(int minSize, int maxSize) {
        int start = 0;
        int end;
        Random random = new Random();
        for (int i = 0; i < spannableList.size(); i++) {
            end = start + newData.get(i).length();
            int randomNum = random.nextInt((maxSize - minSize) + 1) + minSize;
            spannableList.get(i).setSpan(new AbsoluteSizeSpan(randomNum), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            start = end + 1;
        }
    }

    public void setOnWordClickListener(final WordCloudClick wordCloudClick) {

        int start = 0;
        int spanEnd;
        int charEnd;
        for (int i = 0; i < newData.size(); i++) {
            charEnd = start + newData.get(i).trim().length();
            spanEnd = start + newData.get(i).length();
            final int finalI = i;
            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    wordCloudClick.onWordClick(widget, finalI);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    //I repeat, do not enter anything in this, If you will do that, this will lead to a nuclear war
                }
            }, start, charEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = spanEnd;
        }
        this.setText(spannableStringBuilder);
        this.setMovementMethod(LinkMovementMethod.getInstance());
        this.setLinksClickable(true);
    }

    public void setCloudTextColor(String colorHexValue) {
        int start = 0;
        int end;
        for (int i = 0; i < spannableList.size(); i++) {
            end = start + newData.get(i).length();
            spannableList.get(i).setSpan(new ForegroundColorSpan(Color.parseColor(colorHexValue)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = end + 1;
        }
    }


    public void setRandomTextColor() {
        int start = 0;
        int end;
        Random rand = new Random();
        for (int i = 0; i < spannableList.size(); i++) {
            end = start + newData.get(i).length();
            final int r = rand.nextInt(256);
            final int g = rand.nextInt(256);
            final int b = rand.nextInt(256);
            spannableList.get(i).setSpan(new ForegroundColorSpan(Color.rgb(r, g, b)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = end + 1;
        }
    }

    public void setRandomFonts() {

        int start = 0;
        int spanEnd;
        int charEnd;
        Random rand = new Random();
        for (int i = 0; i < newData.size(); i++) {
            charEnd = start + newData.get(i).trim().length();
            spanEnd = start + newData.get(i).length();
            final int random = 1 + rand.nextInt(18);
            Typeface typefaceRandom = WordCloudUtils.getFont(context, random);
            spannableStringBuilder.setSpan(new CustomTypefaceSpan(typefaceRandom), start, charEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = spanEnd;
        }
        this.setText(spannableStringBuilder);
    }
}
