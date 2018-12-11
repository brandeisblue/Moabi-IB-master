package com.ivorybridge.moabi.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.util.FormattedTime;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PainTextEntryFragment extends Fragment {

    private static final String TAG = PainTextEntryFragment.class.getSimpleName();
    @BindView(R.id.fragment_pain_entry_seekbar)
    IndicatorSeekBar seekBar;
    /*
    @BindView(R.id.fragment_pain_entry_body_imageview)
    DrawImageView bodyImageView;*/
    @BindView(R.id.fragment_pain_entry_entry_framelayout)
    RelativeLayout bodyLayout;
    private FirebaseManager firebaseManager;
    private FormattedTime formattedTime;
    private Toast mCurrentToast;


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
}


