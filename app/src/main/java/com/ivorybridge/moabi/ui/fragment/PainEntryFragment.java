package com.ivorybridge.moabi.ui.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.ui.views.DrawingView;
import com.ivorybridge.moabi.util.FormattedTime;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PainEntryFragment extends Fragment {

    private static final String TAG = PainEntryFragment.class.getSimpleName();
    static final String PHOTO_TAP_TOAST_STRING = "Photo Tap! X: %.2f %% Y:%.2f %% ID: %d";
    static final String SCALE_TOAST_STRING = "Scaled to: %.2ff";
    static final String FLING_LOG_STRING = "Fling velocityX: %.2f, velocityY: %.2f";
    @BindView(R.id.fragment_pain_entry_seekbar)
    IndicatorSeekBar seekBar;
    @BindView(R.id.fragment_pain_entry_body_imageview)
    PhotoView bodyImageView;
    /*
    @BindView(R.id.fragment_pain_entry_body_imageview)
    DrawImageView bodyImageView;*/
    @BindView(R.id.fragment_pain_entry_entry_framelayout)
    RelativeLayout bodyLayout;
    private FirebaseManager firebaseManager;
    private FormattedTime formattedTime;
    private boolean isTouching;
    private DrawingView drawingView;
    private Toast mCurrentToast;
    float downx = 0;
    float downy = 0;
    float upx = 0;
    float upy = 0;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        formattedTime = new FormattedTime();
        firebaseManager = new FirebaseManager();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_pain_entry, container, false);
        ButterKnife.bind(this, mView);
        //bodyImageView.setImageBitmap(alteredBitmap);
        //DrawingView drawingView = new DrawingView(getContext());
        //TouchView touchView = new TouchView(getContext());
        //bodyLayout.addView(drawingView);
        //bodyLayout.addView(touchView);
        bodyImageView.setVisibility(View.VISIBLE);
        bodyImageView.setOnPhotoTapListener(new PhotoTapListener());
        //bodyImageView.setZoomable(false);
        /*
        bodyImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // draw
                // set start coords
                float[] coors = getPointerCoords(bodyImageView, event);
                int xCoord = Integer.valueOf((int) coors[0]);
                int yCoord = Integer.valueOf((int) coors[1]);
                Log.i(TAG, "(" + xCoord + ", " + yCoord + ")");
                Toast.makeText(getContext(), "(" + xCoord + ", " + yCoord + ")", Toast.LENGTH_LONG).show();
                Log.i(TAG, Arrays.toString(coors));
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //bodyImageView.left = event.getX();
                    //bodyImageView.top = event.getY();
                    // set end coords
                } else {
                    //bodyImageView.right = event.getX();
                    //bodyImageView.bottom = event.getY();
                }
                // draw
                //bodyImageView.invalidate();
                //bodyImageView.drawRect = true;
                /*
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // TODO Auto-generated method stub
                        bodyImageView.left = event.getX();
                        bodyImageView.top = event.getY();
                        downx = event.getX();
                        downy = event.getY();
                        int[] viewCoords = new int[2];
                        bodyImageView.getLocationOnScreen(viewCoords);
                        isTouching = true;
                        Matrix inverse = new Matrix();
                        bodyImageView.getImageMatrix().invert(inverse);
                        float[] touchPoint = new float[]{event.getX(), event.getY()};
                        inverse.mapPoints(touchPoint);
                        int xCoord = Integer.valueOf((int) touchPoint[0]);
                        int yCoord = Integer.valueOf((int) touchPoint[1]);
                        Log.i(TAG, "(" + xCoord + ", " + yCoord + ")");
                        int xScreenCoord = viewCoords[0];
                        int yScreenCoord = viewCoords[1];
                        Log.i(TAG, "(" + xScreenCoord + ", " + yScreenCoord + ")");
                        break;
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        isTouching = false;
                        //invalidate();
                        break;
                }
                return true;
            }
        });*/

        seekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });
        return mView;
    }

    private static Bitmap getBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        Log.e(TAG, "getBitmap: 1");
        return bitmap;
    }

    final float[] getPointerCoords(ImageView view, MotionEvent e) {
        final int index = e.getActionIndex();
        final float[] coords = new float[]{e.getX(index), e.getY(index)};
        Matrix matrix = new Matrix();
        view.getImageMatrix().invert(matrix);
        matrix.postTranslate(view.getScrollX(), view.getScrollY());
        matrix.mapPoints(coords);
        return coords;
    }

    private class PhotoTapListener implements OnPhotoTapListener {

        @Override
        public void onPhotoTap(ImageView view, float x, float y) {
            float xPercentage = x * 100f;
            float yPercentage = y * 100f;
            showToast(String.format(Locale.US, PHOTO_TAP_TOAST_STRING, xPercentage, yPercentage, view == null ? 0 : view.getId()));
        }
    }

    private void showToast(CharSequence text) {
        if (mCurrentToast != null) {
            mCurrentToast.cancel();
        }
        mCurrentToast = Toast.makeText(getContext(), text, Toast.LENGTH_SHORT);
        mCurrentToast.show();
    }
}


