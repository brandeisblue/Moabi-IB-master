package com.ivorybridge.moabi.ui.recyclerviewitem.connectservicesactivity;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.Process;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.StackingBehavior;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.builtinfitness.BuiltInActivitySummary;
import com.ivorybridge.moabi.database.entity.util.ConnectedService;
import com.ivorybridge.moabi.database.entity.util.DataInUseMediatorLiveData;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.network.auth.FitbitAuthorizationRequest;
import com.ivorybridge.moabi.network.auth.GoogleFitAPI;
import com.ivorybridge.moabi.repository.BuiltInFitnessRepository;
import com.ivorybridge.moabi.repository.DataInUseRepository;
import com.ivorybridge.moabi.service.MotionSensorService;
import com.ivorybridge.moabi.ui.activity.MainActivity;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.util.Pair;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.refactor.library.SmoothCheckBox;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

public class ServiceItem extends AbstractItem<ServiceItem, ServiceItem.ViewHolder> {

    private static final String TAG = ServiceItem.class.getSimpleName();
    private Context mContext;
    private String itemType;
    private AppCompatActivity mActivity;

    public ServiceItem(Context context, String itemType, AppCompatActivity activity) {
        mContext = context;
        this.itemType = itemType;
        mActivity = activity;
    }

    @Override
    public int getType() {
        return R.id.connectservices_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_connectservices_rv_item;
    }

    @NonNull
    @Override
    public ServiceItem.ViewHolder getViewHolder(@NonNull View v) {
        return new ServiceItem.ViewHolder(v);
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<ServiceItem> {

        @BindView(R.id.activity_connected_services_recyclerview_item_icon_imageview)
        ImageView iconImageView;
        @BindView(R.id.activity_connected_services_recyclerview_item_cardview)
        CardView cardView;
        @BindView(R.id.activity_connected_services_recyclerview_item_description_textview)
        TextView descriptionTextView;
        @BindView(R.id.activity_connected_services_recyclerview_item_title_textview)
        TextView titleTextView;
        @BindView(R.id.activity_connected_services_recyclerview_item_connected_linearlayout)
        LinearLayout connectedLinearLayout;
        @BindView(R.id.activity_connected_services_recyclerview_item_selected_checkbox)
        SmoothCheckBox checkBox;
        private SharedPreferences usageSharedPreferences;
        private static final String USAGE_PREFERENCES = "UsagePref";
        private FirebaseManager firebaseManager;
        private FitbitAuthorizationRequest fitbitAuthRequestMaker;
        private static final int REQUEST_LOCATION_PERMISSION_REQUEST_CODE = 12;
        private DataInUseRepository dataInUseRepository;
        private BuiltInFitnessRepository builtInFitnessRepository;
        private DataInUseViewModel dataInUseViewModel;
        private FormattedTime formattedTime;
        private SharedPreferences.Editor notificationSPEditor;
        private TapTargetView tapTargetView;

        private ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(@NonNull final ServiceItem item, List<Object> payloads) {
            firebaseManager = new FirebaseManager();
            usageSharedPreferences = item.mContext.getSharedPreferences(USAGE_PREFERENCES, Context.MODE_PRIVATE);
            final SharedPreferences.Editor usageEditor = usageSharedPreferences.edit();
            final Boolean shouldDisplayUsagePrompt = usageSharedPreferences.getBoolean("should_display_usage_permission_permission_prompt", true);
            checkBox.setEnabled(false);
            //inputInUseViewModel = ViewModelProviders.
            dataInUseRepository = new DataInUseRepository(item.mActivity.getApplication());
            builtInFitnessRepository = new BuiltInFitnessRepository(item.mActivity.getApplication());
            formattedTime = new FormattedTime();
            SharedPreferences getPrefs = PreferenceManager
                    .getDefaultSharedPreferences(itemView.getContext());
            SharedPreferences.Editor e = getPrefs.edit();
            boolean tut3Complete = getPrefs.getBoolean("tut_3_complete", false);
            SharedPreferences notificationSharedPreferences = itemView.getContext().getSharedPreferences(
                    itemView.getContext().getString(R.string.com_ivorybridge_mobai_NOTIFICATION_SHARED_PREFERENCE),
                    Context.MODE_PRIVATE);
            notificationSPEditor = notificationSharedPreferences.edit();
            // set each item's image and text
            final String itemType = item.itemType;
            if (itemType.equals(itemView.getContext().getString(R.string.phone_usage_camel_case))) {
                iconImageView.setImageResource(R.drawable.ic_appusage);
                descriptionTextView.setText(R.string.phone_usage_description);
                titleTextView.setText(R.string.phone_usage_title);
            } else if (itemType.equals(itemView.getContext().getString(R.string.fitbit_camel_case))) {
                iconImageView.setImageResource(R.drawable.ic_fitbit_logo);
                descriptionTextView.setText(R.string.fitbit_description);
                titleTextView.setText(R.string.fitbit_title);
            } else if (itemType.equals(itemView.getContext().getString(R.string.googlefit_camel_case))) {
                iconImageView.setImageResource(R.drawable.ic_googlefit);
                descriptionTextView.setText(R.string.googlefit_description);
                titleTextView.setText(R.string.googlefit_title);
            } else if (itemType.equals(itemView.getContext().getString(R.string.weather_camel_case))) {
                iconImageView.setImageResource(R.drawable.ic_partly_cloudy);
                descriptionTextView.setText(R.string.weather_description);
                titleTextView.setText(R.string.weather_title);
            } else if (itemType.equals(itemView.getContext().getString(R.string.moabi_tracker_camel_case))) {
                iconImageView.setImageResource(R.drawable.ic_monogram_colored);
                descriptionTextView.setText(R.string.moabi_tracker_description);
                titleTextView.setText(R.string.moabi_tracker_title);
                if (!tut3Complete) {
                    tapTargetView = TapTargetView.showFor(item.mActivity, TapTarget.forView(itemView.getRootView().findViewById(R.id.activity_connected_services_recyclerview_item_cardview),
                            itemView.getContext().getString(R.string.tutorial_connect_services_title),
                            "")
                                    .outerCircleColor(R.color.colorPrimary)
                                    .outerCircleAlpha(0.7f)
                                    .targetCircleColor(R.color.white)
                                    .titleTextSize(16)
                                    //.titleTextColor(R.color.colorPrimary)      // Specify the color of the title text
                                    .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                                    //.descriptionTextColor(R.color.white)  // Specify the color of the description text
                                    .textColor(R.color.white)
                                    .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                    .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                                    .drawShadow(true)                   // Whether to draw a drop shadow or not
                                    .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                    .tintTarget(false)                   // Whether to tint the target view's color
                                    .transparentTarget(true)          // Specify whether the target is transparent (displays the content underneath)
                                    //.icon(ContextCompat.getDrawable(this, R.drawable.bg_rectangle_rounded_white))           // Specify a custom drawable to draw as the target
                                    .targetRadius(96),                  // Specify the target radius (in dp)
                            new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                                @Override
                                public void onTargetClick(TapTargetView view) {
                                    super.onTargetClick(view);      // This call is optional
                                    checkBox.setChecked(true, true);
                                    InputInUse inputInUse = new InputInUse();
                                    inputInUse.setType("tracker");
                                    inputInUse.setName(itemView.getContext().getString(R.string.moabi_tracker_camel_case));
                                    inputInUse.setInUse(true);
                                    dataInUseRepository.insert(inputInUse);
                                    ConnectedService connectedService = new ConnectedService();
                                    connectedService.setType("tracker");
                                    connectedService.setName(itemView.getContext().getString(R.string.moabi_tracker_camel_case));
                                    connectedService.setConnected(true);
                                    dataInUseRepository.insert(connectedService);
                                    e.putBoolean("tut_3_complete", true);
                                    boolean success = e.commit();
                                    if (success) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                BuiltInActivitySummary today = builtInFitnessRepository.getNow(formattedTime.getCurrentDateAsYYYYMMDD());
                                                if (today == null) {
                                                    BuiltInActivitySummary activitySummary = new BuiltInActivitySummary();
                                                    activitySummary.setSteps(0L);
                                                    activitySummary.setDistance(0d);
                                                    activitySummary.setActiveMinutes(0L);
                                                    activitySummary.setSedentaryMinutes(0L);
                                                    activitySummary.setCalories(0d);
                                                    activitySummary.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                                                    activitySummary.setLastSensorTimeStamp(formattedTime.getCurrentTimeInMilliSecs());
                                                    activitySummary.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                                                    activitySummary.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                                                    builtInFitnessRepository.insert(activitySummary, formattedTime.getCurrentDateAsYYYYMMDD());
                                                    notificationSPEditor.putBoolean(itemView.getContext().getString(R.string.preference_fitness_tracker_notification), true);
                                                    notificationSPEditor.putString(itemView.getContext().getString(R.string.preference_fitness_tracker_source_notification),
                                                            itemView.getContext().getString(R.string.moabi_tracker_title));
                                                    notificationSPEditor.putString(itemView.getContext().getString(R.string.preference_fitness_tracker_activity_type_notification),
                                                            itemView.getContext().getString(R.string.activity_steps_title));
                                                    boolean writeSuccess = notificationSPEditor.commit();
                                                    if (writeSuccess) {
                                                        Intent intent = new Intent(itemView.getContext(), MotionSensorService.class);
                                                        itemView.getContext().startService(intent);
                                                    }
                                                }
                                            }
                                        }).start();
                                    }
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent tutorialIntent = new Intent(item.mActivity, MainActivity.class);
                                            tutorialIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                            itemView.getContext().startActivity(tutorialIntent);
                                        }
                                    }, 300);
                                }
                            });
                }
            }
            initializeCheckBox(item, itemType, checkBox);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBox.isChecked()) {
                        checkBox.setChecked(false, true);
                        InputInUse inputInUse = new InputInUse();
                        inputInUse.setType("tracker");
                        inputInUse.setName(itemType);
                        inputInUse.setInUse(false);
                        ConnectedService connectedService = new ConnectedService();
                        connectedService.setType("tracker");
                        connectedService.setName(itemType);
                        connectedService.setConnected(false);
                        boolean inputInsert = dataInUseRepository.insert(inputInUse);
                        boolean serviceInsert = dataInUseRepository.insert(connectedService);
                        if (inputInsert && serviceInsert) {
                            //resetTrackerNotification();
                        }
                    } else {
                        if (item.mContext != null && item.mActivity != null) {
                            if (itemType.equals(itemView.getContext().getString(R.string.phone_usage_camel_case))) {
                                if (isUsagePermissionGranted(item.mContext)) {
                                    InputInUse inputInUse = new InputInUse();
                                    inputInUse.setType("tracker");
                                    inputInUse.setName(itemView.getContext().getString(R.string.phone_usage_camel_case));
                                    inputInUse.setInUse(true);
                                    dataInUseRepository.insert(inputInUse);
                                    ConnectedService connectedService = new ConnectedService();
                                    connectedService.setType("tracker");
                                    connectedService.setName(itemView.getContext().getString(R.string.phone_usage_camel_case));
                                    connectedService.setConnected(true);
                                    dataInUseRepository.insert(connectedService);
                                } else {
                                    displayTrackAppUsageDialog(item, checkBox, usageEditor);
                                }
                            } else if (itemType.equals(itemView.getContext().getString(R.string.fitbit_camel_case))) {
                                displayConnectFitbitDialog(item, checkBox);
                            } else if (itemType.equals(itemView.getContext().getString(R.string.googlefit_camel_case))) {
                                if (isFitInstalled(item)) {
                                    requestLocationPermissionForGoogleFit(item, checkBox);
                                } else {
                                    displayInstallGoogleFitDialog(item);
                                }
                            } else if (itemType.equals(itemView.getContext().getString(R.string.weather_camel_case))) {
                                String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                                if (EasyPermissions.hasPermissions(item.mContext, perms)) {
                                    InputInUse inputInUse = new InputInUse();
                                    inputInUse.setType("tracker");
                                    inputInUse.setName(itemView.getContext().getString(R.string.weather_camel_case));
                                    inputInUse.setInUse(true);
                                    dataInUseRepository.insert(inputInUse);
                                    ConnectedService connectedService = new ConnectedService();
                                    connectedService.setType("tracker");
                                    connectedService.setName(itemView.getContext().getString(R.string.weather_camel_case));
                                    connectedService.setConnected(true);
                                    dataInUseRepository.insert(connectedService);
                                } else {
                                    EasyPermissions.requestPermissions(item.mActivity, itemView.getContext().getString(R.string.location_rationale),
                                            REQUEST_LOCATION_PERMISSION_REQUEST_CODE, perms);
                                }
                            } else if (itemType.equals(itemView.getContext().getString(R.string.moabi_tracker_camel_case))) {
                                InputInUse inputInUse = new InputInUse();
                                inputInUse.setType("tracker");
                                inputInUse.setName(itemView.getContext().getString(R.string.moabi_tracker_camel_case));
                                inputInUse.setInUse(true);
                                dataInUseRepository.insert(inputInUse);
                                ConnectedService connectedService = new ConnectedService();
                                connectedService.setType("tracker");
                                connectedService.setName(itemView.getContext().getString(R.string.moabi_tracker_camel_case));
                                connectedService.setConnected(true);
                                dataInUseRepository.insert(connectedService);
                                /*
                                notificationSPEditor.putString(itemView.getContext().getString(R.string.preference_fitness_tracker_source_notification),
                                        itemView.getContext().getString(R.string.moabi_tracker_title));
                                notificationSPEditor.putString(itemView.getContext().getString(R.string.preference_fitness_tracker_activity_type_notification),
                                        itemView.getContext().getString(R.string.activity_steps_title));
                                boolean success = notificationSPEditor.commit();*/
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        BuiltInActivitySummary today = builtInFitnessRepository.getNow(formattedTime.getCurrentDateAsYYYYMMDD());
                                        if (today == null) {
                                            BuiltInActivitySummary activitySummary = new BuiltInActivitySummary();
                                            activitySummary.setSteps(0L);
                                            activitySummary.setDistance(0d);
                                            activitySummary.setActiveMinutes(0L);
                                            activitySummary.setSedentaryMinutes(0L);
                                            activitySummary.setCalories(0d);
                                            activitySummary.setTimeOfEntry(formattedTime.getCurrentTimeInMilliSecs());
                                            activitySummary.setLastSensorTimeStamp(formattedTime.getCurrentTimeInMilliSecs());
                                            activitySummary.setDate(formattedTime.getCurrentDateAsYYYYMMDD());
                                            activitySummary.setDateInLong(formattedTime.getCurrentTimeInMilliSecs());
                                            builtInFitnessRepository.insert(activitySummary, formattedTime.getCurrentDateAsYYYYMMDD());
                                            Intent intent = new Intent(itemView.getContext(), MotionSensorService.class);
                                            itemView.getContext().startService(intent);
                                        }
                                    }
                                }).start();
                            }
                        }
                    }
                }
            });
        }

        private void initializeCheckBox(final ServiceItem item, final String itemType, final SmoothCheckBox checkBox) {
            // if the user has decided to use the service, enable the checkbox. else, disable it.
            // if the user has connected to the service, enable the checkbox. else, disable it.
            dataInUseViewModel = ViewModelProviders.of(item.mActivity).get(DataInUseViewModel.class);
            DataInUseMediatorLiveData dataInUseMediatorLiveData = new DataInUseMediatorLiveData(dataInUseViewModel.getAllInputsInUse(), dataInUseViewModel.getAllConnectedServices());
            dataInUseMediatorLiveData.observe(item.mActivity, new Observer<Pair<List<InputInUse>, List<ConnectedService>>>() {
                @Override
                public void onChanged(Pair<List<InputInUse>, List<ConnectedService>> listListPair) {
                    if (listListPair.first != null && listListPair.second != null &&
                            listListPair.first.size() > 0 && listListPair.second.size() > 0) {
                        for (InputInUse inputInUse : listListPair.first) {
                            if (inputInUse.getName().equals(itemType) && inputInUse.isInUse()) {
                                for (ConnectedService connectedService : listListPair.second) {
                                    if (connectedService.getName().equals(itemType)) {
                                        checkBox.setChecked(connectedService.isConnected());
                                    }
                                }
                            }

                        }
                    }
                }
            });
        }

        @Override
        public void unbindView(@NonNull ServiceItem item) {
            if (tapTargetView != null) {
                tapTargetView.dismiss(true);
            }
        }

        private void displayTrackAppUsageDialog(final ServiceItem item, final SmoothCheckBox box, final SharedPreferences.Editor usageEditor) {
            new MaterialDialog.Builder(item.mContext)
                    .title(itemView.getContext().getString(R.string.phone_usage_title))
                    .content(itemView.getContext().getString(R.string.phone_usage_permission_title))
                    .positiveText(itemView.getContext().getString(R.string.go_to_settings_title))
                    .negativeText(itemView.getContext().getString(R.string.dismiss_title))
                    .limitIconToDefaultSize()
                    .iconRes(R.drawable.ic_appusage)
                    .stackingBehavior(StackingBehavior.ADAPTIVE)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            item.mContext.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (dialog.isPromptCheckBoxChecked()) {
                                usageEditor.putBoolean("should_display_usage_permission_permission_prompt", false);
                                usageEditor.commit();
                            }
                            InputInUse inputInUse = new InputInUse();
                            inputInUse.setType("tracker");
                            inputInUse.setName(itemView.getContext().getString(R.string.phone_usage_camel_case));
                            inputInUse.setInUse(false);
                            dataInUseRepository.insert(inputInUse);
                            ConnectedService connectedService = new ConnectedService();
                            connectedService.setType("tracker");
                            connectedService.setName(itemView.getContext().getString(R.string.phone_usage_camel_case));
                            connectedService.setConnected(false);
                            dataInUseRepository.insert(connectedService);
                            dialog.dismiss();
                        }
                    })
                    /*
                    .checkBoxPrompt(itemView.getContext().getString(R.string.dont_ask_again_title), false, new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                usageEditor.putBoolean("should_display_usage_permission_permission_prompt", false);
                                usageEditor.commit();
                            }
                        }
                    })*/
                    .show();
        }

        private void displayConnectFitbitDialog(final ServiceItem item, final SmoothCheckBox box) {
            new MaterialDialog.Builder(item.mActivity)
                    .title(itemView.getContext().getString(R.string.fitbit_title))
                    .content(R.string.fitbit_permission_prompt)
                    .limitIconToDefaultSize()
                    .positiveText(R.string.dialog_positive_text)
                    .negativeText(R.string.dialog_negative_text)
                    .autoDismiss(true)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            fitbitAuthRequestMaker = new FitbitAuthorizationRequest(item.mContext);
                            fitbitAuthRequestMaker.makeRequest();
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            //mFitbitEditor.putBoolean("isFitbitConnected", false);
                            //mFitbitEditor.apply();
                            InputInUse inputInUse = new InputInUse();
                            inputInUse.setType("tracker");
                            inputInUse.setName(itemView.getContext().getString(R.string.fitbit_camel_case));
                            inputInUse.setInUse(false);
                            dataInUseRepository.insert(inputInUse);
                            ConnectedService connectedService = new ConnectedService();
                            connectedService.setType("tracker");
                            connectedService.setName(itemView.getContext().getString(R.string.fitbit_camel_case));
                            connectedService.setConnected(false);
                            dataInUseRepository.insert(connectedService);
                            item.mActivity.finish();
                            item.mActivity.overridePendingTransition(0, 0);
                            item.mActivity.startActivity(item.mActivity.getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        }
                    })
                    .cancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.dismiss();
                        }
                    })
                    .iconRes(R.drawable.ic_fitbit_logo)
                    .show();
        }

        private void displayInstallGoogleFitDialog(final ServiceItem item) {
            new MaterialDialog.Builder(item.mActivity)
                    .title(itemView.getContext().getString(R.string.googlefit_title))
                    .content(R.string.googlefit_install_prompt)
                    .limitIconToDefaultSize()
                    .positiveText(R.string.dialog_positive_text)
                    .negativeText(R.string.dialog_negative_text)
                    .autoDismiss(true)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            askToInstallGoogleFit(item);
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            InputInUse inputInUse = new InputInUse();
                            inputInUse.setType("tracker");
                            inputInUse.setName(itemView.getContext().getString(R.string.googlefit_camel_case));
                            inputInUse.setInUse(false);
                            dataInUseRepository.insert(inputInUse);
                            ConnectedService connectedService = new ConnectedService();
                            connectedService.setType("tracker");
                            connectedService.setName(itemView.getContext().getString(R.string.googlefit_camel_case));
                            connectedService.setConnected(false);
                            dataInUseRepository.insert(connectedService);
                            dialog.dismiss();
                            item.mActivity.finish();
                            item.mActivity.overridePendingTransition(0, 0);
                            item.mActivity.startActivity(item.mActivity.getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        }
                    })
                    .cancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.dismiss();
                        }
                    })
                    .iconRes(R.drawable.ic_googlefit)
                    .show();
        }

        private void displayConnectGoogleFitDialog(final ServiceItem item, final SmoothCheckBox box) {
            new MaterialDialog.Builder(item.mActivity)
                    .title(itemView.getContext().getString(R.string.googlefit_title))
                    .content(R.string.googlefit_permission_prompt)
                    .limitIconToDefaultSize()
                    .positiveText(R.string.dialog_positive_text)
                    .negativeText(R.string.dialog_negative_text)
                    .autoDismiss(true)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            GoogleFitAPI gFit = new GoogleFitAPI(item.mActivity);
                            gFit.requestPermission();
                            dialog.dismiss();
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            InputInUse inputInUse = new InputInUse();
                            inputInUse.setType("tracker");
                            inputInUse.setName(itemView.getContext().getString(R.string.googlefit_camel_case));
                            inputInUse.setInUse(false);
                            dataInUseRepository.insert(inputInUse);
                            ConnectedService connectedService = new ConnectedService();
                            connectedService.setType("tracker");
                            connectedService.setName(itemView.getContext().getString(R.string.googlefit_camel_case));
                            connectedService.setConnected(false);
                            dataInUseRepository.insert(connectedService);
                            dialog.dismiss();
                            item.mActivity.finish();
                            item.mActivity.overridePendingTransition(0, 0);
                            item.mActivity.startActivity(item.mActivity.getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        }
                    })
                    .cancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.dismiss();
                        }
                    })
                    .iconRes(R.drawable.ic_googlefit)
                    .show();
        }

        private boolean isUsagePermissionGranted(Context context) {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = -1;
            try {
                if (appOps != null) {
                    mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, Process.myUid(), context.getPackageName());
                }
                return mode == MODE_ALLOWED;
            } catch (NullPointerException e) {
                e.printStackTrace();
                return false;
            }
        }

        private void requestLocationPermissionForGoogleFit(ServiceItem item, SmoothCheckBox box) {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
            if (EasyPermissions.hasPermissions(item.mContext, perms)) {
                displayConnectGoogleFitDialog(item, box);
            } else {
                EasyPermissions.requestPermissions(item.mActivity, itemView.getContext().getString(R.string.location_rationale),
                        REQUEST_LOCATION_PERMISSION_REQUEST_CODE, perms);
            }
        }

        @CheckResult
        private Boolean isFitInstalled(ServiceItem item) {
            String PACKAGE_NAME = "com.google.android.apps.fitness";
            try {
                item.mActivity.getPackageManager().getPackageInfo(PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }

        private void askToInstallGoogleFit(ServiceItem item) {
            String PACKAGE_NAME = "com.google.android.apps.fitness";
            try {
                item.mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PACKAGE_NAME)));
            } catch (android.content.ActivityNotFoundException e) {
                item.mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + PACKAGE_NAME)));
            }
        }
    }
}

