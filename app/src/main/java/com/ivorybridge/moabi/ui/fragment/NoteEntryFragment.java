package com.ivorybridge.moabi.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.ui.activity.MainActivity;
import com.ivorybridge.moabi.ui.views.SubmitButton;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class NoteEntryFragment extends Fragment {

    private static final String TAG = NoteEntryFragment.class.getSimpleName();
    private DatabaseReference collectiveHashTagRef;
    private DatabaseReference personalHashTagRef;
    private DatabaseReference personalHashTagLast30DaysRef;
    private DatabaseReference activityHasDataRef;
    private FirebaseManager firebaseManager;
    @BindView(R.id.fragment_journal_entry_submitbutton)
    SubmitButton submitButton;

    public Long EDUCATION_CAREER = 0L;
    public Long SOCIAL = 1L;
    public Long RECREATION_OR_INTERESTS = 2L;
    public Long MIND_PHYSICAL_SPIRITUAL = 3L;
    public Long DAILY_RESPONSIBILITY = 4L;

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
        firebaseManager = new FirebaseManager();
        collectiveHashTagRef = firebaseManager.getCollectiveHashTagsRef();
        personalHashTagRef = firebaseManager.getPersonalHashTagTodayRef();
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
        View mView = inflater.inflate(R.layout.fragment_journal_entry, container, false);
        ButterKnife.bind(this, mView);
        final FragmentManager fragmentManager = getFragmentManager();
        final List<Fragment> fragments = new ArrayList<>();
        if (fragmentManager != null) {
            fragments.addAll(fragmentManager.getFragments());
        }
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime zdt = now.atZone(ZoneId.systemDefault());
        Long currentTime = zdt.toInstant().toEpochMilli();

        submitButton.setOnResultEndListener(new SubmitButton.OnResultEndListener() {
            @Override
            public void onResultEnd() {
                ViewPager viewPager = new ViewPager(getContext());
                if (getActivity() != null) {
                    viewPager = getActivity().findViewById(R.id.activity_make_entry_viewpager);
                }
                int currentItem = viewPager.getCurrentItem();
                if (fragments.size() == 1 || currentItem == fragments.size() - 1) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                } else {
                    viewPager.setCurrentItem(currentItem + 1, true);
                }
                submitButton.reset();
            }
        });

        return mView;

    }

    private String getCurrentTime() {
        DateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return df.format(Calendar.getInstance().getTime());
    }

    private Long getStartOfDay(String date) {

        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        return LocalDate.parse(date, dateFormatter)
                .atStartOfDay(ZoneOffset.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    private Long getEndOfDay(String date) {
        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        return LocalDate.parse(date, dateFormatter)
                .atStartOfDay(ZoneOffset.systemDefault())
                .plusDays(1L)
                .minusNanos(1L)
                .toInstant()
                .toEpochMilli();
    }

    private String setUpDatesForToday() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return mdformat.format(calendar.getTime());
    }
}