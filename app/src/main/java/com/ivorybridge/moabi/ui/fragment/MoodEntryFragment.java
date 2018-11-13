package com.ivorybridge.moabi.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.moodandenergy.MoodAndEnergy;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.ui.activity.MainActivity;
import com.ivorybridge.moabi.ui.views.SubmitButton;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;
import com.ivorybridge.moabi.viewmodel.MoodAndEnergyViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MoodEntryFragment extends Fragment {

    private static final String TAG = MoodEntryFragment.class.getSimpleName();
    private Context mContext;
    private SharedPreferences moodSharedPreferences;
    private static final String MOOD_TOKEN_PREFERENCES = "MoodTokenPrefs";
    private int mShortAnimationDuration;
    private LinearLayout mExpandedLL;
    private ImageView mExpandedImageView;
    private TextView mExpandedTextView;
    private SubmitButton mSubmitButton;
    private View mContainerView;
    private AnimatorSet mCurrentAnimator;
    private FirebaseManager firebaseManager;
    private DatabaseReference moodAndEnergyHasDataRef;
    private FormattedTime formattedTime;
    private ViewGroup mContainer;
    @BindView(R.id.fragment_mood_entry_submitbutton)
    Button submitButton;
    private List<String> userInputsInUseList;
    private DataInUseViewModel dataInUseViewModel;

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
        mContext = context;
        firebaseManager = new FirebaseManager();
        moodAndEnergyHasDataRef = firebaseManager.getMoodAndEnergyDayWithDataRef();
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
        moodSharedPreferences = mContext.getSharedPreferences(
                MOOD_TOKEN_PREFERENCES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = moodSharedPreferences.edit();
        View mView = inflater.inflate(R.layout.fragment_mood_entry, container, false);
        ButterKnife.bind(this, mView);
        mContainer = container;
        formattedTime = new FormattedTime();
        userInputsInUseList = new ArrayList<>();
        mContainerView = mView.findViewById(R.id.fragment_mood_entry_container);
        mExpandedLL = (LinearLayout) mView.findViewById(R.id.fragment_mood_entry_expanded_linearlayout);
        mExpandedImageView = (ImageView) mView.findViewById(R.id.fragment_mood_entry_expanded_imageview);
        mExpandedTextView = (TextView) mView.findViewById(R.id.fragment_mood_entry_expanded_textview);
        final RelativeLayout emojiViewGroup = mView.findViewById(R.id.fragment_mood_entry_emoji_relativelayout);
        TextView promptTextView = (TextView) mView.findViewById(R.id.fragment_mood_entry_prompt_textview);
        LinearLayout alertLL = (LinearLayout) mView.findViewById(R.id.fragment_mood_entry_alert_linearlayout);
        LinearLayout excitedLL = (LinearLayout) mView.findViewById(R.id.fragment_mood_entry_excited_linearlayout);
        LinearLayout okayLL = (LinearLayout) mView.findViewById(R.id.fragment_mood_entry_okay_linearlayout);
        LinearLayout happyLL = (LinearLayout) mView.findViewById(R.id.fragment_mood_entry_happy_linearlayout);
        LinearLayout calmLL = (LinearLayout) mView.findViewById(R.id.fragment_mood_entry_calm_linearlayout);
        LinearLayout tiredLL = (LinearLayout) mView.findViewById(R.id.fragment_mood_entry_tired_linearlayout);
        LinearLayout boredLL = (LinearLayout) mView.findViewById(R.id.fragment_mood_entry_bored_linearlayout);
        LinearLayout unhappyLL = (LinearLayout) mView.findViewById(R.id.fragment_mood_entry_unhappy_linearlayout);
        LinearLayout tenseLL = (LinearLayout) mView.findViewById(R.id.fragment_mood_entry_tense_linearlayout);
        ImageView alertIV = (ImageView) mView.findViewById(R.id.fragment_mood_entry_alert_imageview);
        ImageView excitedIV = (ImageView) mView.findViewById(R.id.fragment_mood_entry_excited_imageview);
        ImageView happyIV = (ImageView) mView.findViewById(R.id.fragment_mood_entry_happy_imageview);
        ImageView calmIV = (ImageView) mView.findViewById(R.id.fragment_mood_entry_calm_imageview);
        ImageView tiredIV = (ImageView) mView.findViewById(R.id.fragment_mood_entry_tired_imageview);
        ImageView boredIV = (ImageView) mView.findViewById(R.id.fragment_mood_entry_bored_imageview);
        ImageView unhappyIV = (ImageView) mView.findViewById(R.id.fragment_mood_entry_unhappy_imageview);
        ImageView tenseIV = (ImageView) mView.findViewById(R.id.fragment_mood_entry_tense_imageview);
        TextView alertTV = (TextView) mView.findViewById(R.id.fragment_mood_entry_alert_textview);
        TextView excitedTV = (TextView) mView.findViewById(R.id.fragment_mood_entry_excited_textview);
        TextView happyTV = (TextView) mView.findViewById(R.id.fragment_mood_entry_happy_textview);
        TextView calmTV = (TextView) mView.findViewById(R.id.fragment_mood_entry_calm_textview);
        TextView tiredTV = (TextView) mView.findViewById(R.id.fragment_mood_entry_tired_textview);
        TextView boredTV = (TextView) mView.findViewById(R.id.fragment_mood_entry_bored_textview);
        TextView unhappyTV = (TextView) mView.findViewById(R.id.fragment_mood_entry_unhappy_textview);
        TextView tenseTV = (TextView) mView.findViewById(R.id.fragment_mood_entry_tense_textview);
        if (getActivity() != null) {
            alertLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomImageFromThumb(v, 0);
                }
            });
            excitedLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomImageFromThumb(v, 1);
                }
            });
            happyLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomImageFromThumb(v, 2);
                }
            });
            calmLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomImageFromThumb(v, 3);
                }
            });
            tiredLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomImageFromThumb(v, 4);
                }
            });
            boredLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomImageFromThumb(v, 5);
                }
            });
            unhappyLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomImageFromThumb(v, 6);
                }
            });
            tenseLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomImageFromThumb(v, 7);
                }
            });
            okayLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    zoomImageFromThumb(v, 8);
                }
            });
        }

        return mView;
    }

    private void zoomImageFromThumb(final View viewToZoom, int imageIndex) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        MoodAndEnergyViewModel moodAndEnergyViewModel =
                ViewModelProviders.of(getActivity()).get(MoodAndEnergyViewModel.class);
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        Long mood = 0L;
        Long energyLevel = 0L;

        switch (imageIndex) {
            case 0:
                mExpandedImageView.setImageResource(R.drawable.ic_emoji_alert);
                mExpandedTextView.setText(getString(R.string.mood_alert));
                mood = 2L;
                energyLevel = 3L;
                break;
            case 1:
                mExpandedImageView.setImageResource(R.drawable.ic_emoji_excited);
                mExpandedTextView.setText(getString(R.string.mood_excited));
                mood = 3L;
                energyLevel = 3L;
                break;
            case 2:
                mExpandedImageView.setImageResource(R.drawable.ic_emoji_happy);
                mExpandedTextView.setText(getString(R.string.mood_happy));
                mood = 3L;
                energyLevel = 2L;
                break;
            case 3:
                mExpandedImageView.setImageResource(R.drawable.ic_emoji_calm);
                mExpandedTextView.setText(getString(R.string.mood_calm));
                mood = 3L;
                energyLevel = 1L;
                break;
            case 4:
                mExpandedImageView.setImageResource(R.drawable.ic_emoji_tired);
                mExpandedTextView.setText(getString(R.string.mood_tired));
                mood = 2L;
                energyLevel = 1L;
                break;
            case 5:
                mExpandedImageView.setImageResource(R.drawable.ic_emoji_bored);
                mExpandedTextView.setText(getString(R.string.mood_bored));
                mood = 1L;
                energyLevel = 1L;
                break;
            case 6:
                mExpandedImageView.setImageResource(R.drawable.ic_emoji_sad);
                mExpandedTextView.setText(getString(R.string.mood_unhappy));
                mood = 1L;
                energyLevel = 2L;
                break;
            case 7:
                mExpandedImageView.setImageResource(R.drawable.ic_emoji_angry);
                mExpandedTextView.setText(getString(R.string.mood_tense));
                mood = 1L;
                energyLevel = 3L;
                break;
            case 8:
                mExpandedImageView.setImageResource(R.drawable.ic_emoji_okay);
                mExpandedTextView.setText(getString(R.string.mood_okay));
                mood = 2L;
                energyLevel = 2L;
                break;
        }

        //final Map<String, Object> moodResults = new HashMap<>();
        final String currentTime = getCurrentTime();
        //moodResults.put(getString(R.string.mood_camel_case), mood);
        //moodResults.put("energyLevel", energyLevel);
        final MoodAndEnergy latestMoodAndEnergy = new MoodAndEnergy(formattedTime.getCurrentTimeInMilliSecs(), mood, energyLevel);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        viewToZoom.getGlobalVisibleRect(startBounds);
        mContainerView.getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        mContainerView.setAlpha(0.01f);
        mExpandedLL.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mExpandedLL.getLayoutParams();
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mExpandedLL.setLayoutParams(layoutParams);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        mExpandedLL.setPivotX(0f);
        mExpandedLL.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(mExpandedLL, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(mExpandedLL, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(mExpandedLL, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(mExpandedLL,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;
        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        mExpandedLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelAnimation(startBounds, startScaleFinal);
            }
        });
        final FragmentManager fragmentManager = getFragmentManager();
        final List<Fragment> fragments = fragmentManager.getFragments();
        dataInUseViewModel = ViewModelProviders.of(this).get(DataInUseViewModel.class);
        dataInUseViewModel.getAllInputsInUse().observe(this, new Observer<List<InputInUse>>() {
            @Override
            public void onChanged(List<InputInUse> inputInUses) {
                if (inputInUses != null) {
                    Log.i(TAG, inputInUses.toString());
                    for (InputInUse inUse: inputInUses) {
                        if (inUse.getName().equals(getString(R.string.mood_and_energy_camel_case))
                                || inUse.getName().equals(getString(R.string.baactivity_camel_case))
                                || inUse.getName().equals(getString(R.string.stress_camel_case))
                                || inUse.getName().equals(getString(R.string.pain_camel_case))
                                || inUse.getName().equals(getString(R.string.daily_review_camel_case))
                                || inUse.getName().equals(getString(R.string.depression_phq9_camel_case))
                                || inUse.getName().equals(getString(R.string.anxiety_gad7_camel_case))) {
                            Boolean isChecked = (Boolean) inUse.isInUse();
                            if (isChecked != null) {
                                if (isChecked) {
                                    userInputsInUseList.add(inUse.getName());
                                }
                            }
                        }
                    }
                    Log.i(TAG, userInputsInUseList.toString());
                    submitButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //TODO - if getActivities for today (start of day till now) isn't null, update daysWithData
                            latestMoodAndEnergy.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                moodAndEnergyHasDataRef.setValue(true);
                                firebaseManager.getMoodAndEnergyLevelTodayRef().child(formattedTime.getCurrentTimeAsHHMM())
                                        .setValue(latestMoodAndEnergy);
                                firebaseManager.getMoodAndEnergyLast30DaysTodayRef()
                                        .child(formattedTime.getCurrentTimeAsHHMM()).setValue(latestMoodAndEnergy);
                            }
                            moodAndEnergyViewModel.insert(latestMoodAndEnergy, formattedTime.getCurrentDateAsYYYYMMDD());
                            moodAndEnergyViewModel.getEntries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), 365),
                                    formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(getViewLifecycleOwner(), new Observer<List<MoodAndEnergy>>() {
                                @Override
                                public void onChanged(@Nullable List<MoodAndEnergy> moodAndEnergyList) {
                                    if (moodAndEnergyList != null) {
                                        if (moodAndEnergyList.size() > 0) {
                                            moodAndEnergyViewModel.processMood(moodAndEnergyList);
                                        }
                                    }
                                }
                            });
                            ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.activity_make_entry_viewpager);
                            int currentItem = viewPager.getCurrentItem();
                            Log.i(TAG, "# of fragments: " + userInputsInUseList.size() + ", " + "current position: " + currentItem);
                            if (userInputsInUseList.size() == 1 || currentItem == userInputsInUseList.size() - 1) {
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                                cancelAnimation(startBounds, startScale);
                            } else {
                                viewPager.setCurrentItem(currentItem + 1, true);
                                cancelAnimation(startBounds, startScale);
                            }
                        }
                    });
                }
            }
        });

        /*
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moodAndEnergyHasDataRef.setValue(true);
                latestMoodAndEnergy.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                firebaseManager.getMoodAndEnergyLevelTodayRef().child(formattedTime.getCurrentTimeAsHHMM())
                        .setValue(latestMoodAndEnergy);
                firebaseManager.getMoodAndEnergyLast30DaysTodayRef()
                        .child(formattedTime.getCurrentTimeAsHHMM()).setValue(latestMoodAndEnergy);
                moodAndEnergyViewModel.insert(latestMoodAndEnergy, formattedTime.getCurrentDateAsYYYYMMDD());
                moodAndEnergyViewModel.getEntries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), 365),
                        formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(getViewLifecycleOwner(), new Observer<List<MoodAndEnergy>>() {
                    @Override
                    public void onChanged(@Nullable List<MoodAndEnergy> moodAndEnergyList) {
                        if (moodAndEnergyList != null) {
                            if (moodAndEnergyList.size() > 0) {
                                moodAndEnergyViewModel.processMood(moodAndEnergyList);
                            }
                        }
                    }
                });
                if (getActivity() != null) {
                    ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.activity_make_entry_viewpager);
                    int currentItem = viewPager.getCurrentItem();
                    if (fragments.size() == 1 || currentItem == fragments.size() - 1) {
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                    } else {
                        viewPager.setCurrentItem(currentItem + 1, true);
                    }
                }
            }
        });
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSubmitButton.doResult(true);
                moodAndEnergyHasDataRef.setValue(true);
                latestMoodAndEnergy.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                firebaseManager.getMoodAndEnergyLevelTodayRef().child(formattedTime.getCurrentTimeAsHHMM())
                        .setValue(latestMoodAndEnergy);
                firebaseManager.getMoodAndEnergyLast30DaysTodayRef()
                        .child(formattedTime.getCurrentTimeAsHHMM()).setValue(latestMoodAndEnergy);
                moodAndEnergyViewModel.insert(latestMoodAndEnergy, formattedTime.getCurrentDateAsYYYYMMDD());
                moodAndEnergyViewModel.getEntries(formattedTime.getStartOfDayBeforeSpecifiedNumberOfDays(formattedTime.getCurrentDateAsYYYYMMDD(), 365),
                        formattedTime.getEndOfDay(formattedTime.getCurrentDateAsYYYYMMDD())).observe(getViewLifecycleOwner(), new Observer<List<MoodAndEnergy>>() {
                    @Override
                    public void onChanged(@Nullable List<MoodAndEnergy> moodAndEnergyList) {
                        if (moodAndEnergyList != null) {
                            if (moodAndEnergyList.size() > 0) {
                                moodAndEnergyViewModel.processMood(moodAndEnergyList);
                            }
                        }
                    }
                });
                //setLineChartForMoodAndEnergy(7);
            }

        });

        mSubmitButton.setOnResultEndListener(new SubmitButton.OnResultEndListener() {
            @Override
            public void onResultEnd() {
                //FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                //fragmentTransaction.replace(mContainer.getId(), fragments.get(1));
                //fragmentTransaction.commit();
                if (getActivity() != null) {
                    ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.activity_make_entry_viewpager);
                    int currentItem = viewPager.getCurrentItem();
                    if (fragments.size() == 1 || currentItem == fragments.size() - 1) {
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);

                    } else {
                        viewPager.setCurrentItem(currentItem + 1, true);
                    }
                }
            }
        });*/
    }

    private void cancelAnimation(Rect startBounds, float startScaleFinal) {
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        //mSubmitButton.reset();

        // Animate the four positioning/sizing properties in parallel,
        // back to their original values.
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator
                .ofFloat(mExpandedLL, View.X, startBounds.left))
                .with(ObjectAnimator
                        .ofFloat(mExpandedLL,
                                View.Y, startBounds.top))
                .with(ObjectAnimator
                        .ofFloat(mExpandedLL,
                                View.SCALE_X, startScaleFinal))
                .with(ObjectAnimator
                        .ofFloat(mExpandedLL,
                                View.SCALE_Y, startScaleFinal));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mContainerView.setAlpha(1f);
                mExpandedLL.setVisibility(View.GONE);
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mContainerView.setAlpha(1f);
                mExpandedLL.setVisibility(View.GONE);
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;
    }

    private String getCurrentTime() {
        DateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return df.format(Calendar.getInstance().getTime());
    }

    private String setUpDatesForToday() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return mdformat.format(calendar.getTime());
    }
}
