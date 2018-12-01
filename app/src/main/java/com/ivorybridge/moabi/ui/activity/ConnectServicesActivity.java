package com.ivorybridge.moabi.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.util.ConnectedService;
import com.ivorybridge.moabi.database.entity.util.DataInUseMediatorLiveData;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.network.auth.AuthStateManager;
import com.ivorybridge.moabi.network.auth.FitbitAuthTokenHandler;
import com.ivorybridge.moabi.network.auth.GoogleFitAPI;
import com.ivorybridge.moabi.service.MotionSensorService;
import com.ivorybridge.moabi.ui.recyclerviewitem.connectservicesactivity.ServiceItem;
import com.ivorybridge.moabi.util.FormattedTime;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * This class displays UI for ConnectServicesActivity, which displays a list of services
 * that users can connect to, such as Fitbit, GoogleFitAPI and built-in app usage tracker.
 */
public class ConnectServicesActivity extends AppCompatActivity implements
        EasyPermissions.PermissionCallbacks {

    private static final String TAG = ConnectServicesActivity.class.getSimpleName();
    SharedPreferences fitbitAuthStateSharedPref;
    AuthStateManager authStateManager;
    @BindView(R.id.activity_connect_services_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_connect_services_vertical_recyclerview)
    RecyclerView recyclerView;
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 6;
    private static final int FITBIT_PERMISSION_REQUEST_CODE = 0;
    private static final int REQUEST_LOCATION_PERMISSION_REQUEST_CODE = 12;
    // adapters
    private FastAdapter<IItem> mFastAdapter;
    private ItemAdapter<ServiceItem> mConnectServicesAdapter;
    private DataInUseViewModel dataInUseViewModel;
    private SharedPreferences notificationSharedPreferences;
    private SharedPreferences.Editor notificationSPEditor;
    private FormattedTime formattedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_services);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        Intent intent = this.getIntent();
        if (intent.hasExtra("redirected_from")) {
            if (intent.getStringExtra("redirected_from").equals("empty_view")) {
            }
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
        formattedTime = new FormattedTime();
        notificationSharedPreferences = getSharedPreferences(
                getString(R.string.com_ivorybridge_mobai_NOTIFICATION_SHARED_PREFERENCE),
                Context.MODE_PRIVATE);
        notificationSPEditor = notificationSharedPreferences.edit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.apps_and_services_title));
        }

        Log.i(TAG, "onCreate()");
        dataInUseViewModel = ViewModelProviders.of(this).get(DataInUseViewModel.class);

        // TODO - make sure you read intent to see which activity this activity is being directed from

        // set up custom item adapter
        mConnectServicesAdapter = new ItemAdapter<>();

        // populate fast adapter with item adapters
        mFastAdapter = FastAdapter.with(Arrays.asList(mConnectServicesAdapter));
        mFastAdapter.withSelectable(true);

        fitbitAuthStateSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        authStateManager = AuthStateManager.getInstance(this);

        DataInUseMediatorLiveData dataInUseMediatorLiveData = new DataInUseMediatorLiveData(
                dataInUseViewModel.getAllInputsInUse(), dataInUseViewModel.getAllConnectedServices());
        dataInUseMediatorLiveData.observe(this, new Observer<Pair<List<InputInUse>, List<ConnectedService>>>() {
            @Override
            public void onChanged(Pair<List<InputInUse>, List<ConnectedService>> listListPair) {
                Set<String> activitiesSet = new TreeSet<>();
                if (listListPair.first != null && listListPair.first.size() > 0 &&
                        listListPair.second != null && listListPair.second.size() > 0) {
                    for (InputInUse inputInUse : listListPair.first) {
                        if (inputInUse.isInUse()) {
                            for (ConnectedService connectedService : listListPair.second) {
                                if (connectedService.getName().equals(inputInUse.getName()) &&
                                        connectedService.isConnected()) {
                                    if (connectedService.getName().equals(getString(R.string.fitbit_camel_case))) {
                                        activitiesSet.add("3" + connectedService.getName());
                                        notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_source_notification),
                                                getString(R.string.fitbit_title));
                                        notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_activity_type_notification),
                                                getString(R.string.activity_steps_title));
                                        notificationSPEditor.commit();
                                    } else if (connectedService.getName().equals(getString(R.string.googlefit_camel_case))) {
                                        activitiesSet.add("2" + connectedService.getName());
                                        notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_source_notification),
                                                getString(R.string.googlefit_title));
                                        notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_activity_type_notification),
                                                getString(R.string.activity_steps_title));
                                        notificationSPEditor.commit();
                                    } else if (connectedService.getName().equals(getString(R.string.moabi_tracker_camel_case))) {
                                        activitiesSet.add("0" + connectedService.getName());
                                        notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_source_notification),
                                                getString(R.string.moabi_tracker_title));
                                        notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_activity_type_notification),
                                                getString(R.string.activity_steps_title));
                                        notificationSPEditor.commit();
                                    }
                                }
                            }
                        }
                    }
                }
                String[] activitiesArray = activitiesSet.toArray(new String[0]);
                if (activitiesArray.length < 1) {
                    notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_source_notification),
                            null);
                    notificationSPEditor.putString(getString(R.string.preference_fitness_tracker_activity_type_notification),
                            null);
                    notificationSPEditor.commit();
                    Intent intent = new Intent(ConnectServicesActivity.this, MotionSensorService.class);
                    stopService(intent);
                } else {
                    Intent intent = new Intent(ConnectServicesActivity.this, MotionSensorService.class);
                    startService(intent);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences getPrefs = androidx.preference.PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        boolean tut1Complete = getPrefs.getBoolean("tut_1_complete", false);
        boolean tut2Complete = getPrefs.getBoolean("tut_2_complete", false);
        boolean tut3Complete = getPrefs.getBoolean("tut_3_complete", false);
        boolean tut4Complete = getPrefs.getBoolean("tut_4_complete", false);
        Log.i(TAG, "Tutorial 1 - " + tut1Complete + " Tutorial 2 - " + tut2Complete
                + " Tutorial 3 - " + tut3Complete + " Tutorial 4 - " + tut4Complete);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mFastAdapter);
        mConnectServicesAdapter.clear();
        mConnectServicesAdapter.add(new ServiceItem(this, getString(R.string.fitbit_camel_case), ConnectServicesActivity.this));
        mConnectServicesAdapter.add(new ServiceItem(this, getString(R.string.moabi_tracker_camel_case), ConnectServicesActivity.this));
        mConnectServicesAdapter.add(new ServiceItem(this, getString(R.string.googlefit_camel_case), ConnectServicesActivity.this));
        mConnectServicesAdapter.add(new ServiceItem(this, getString(R.string.phone_usage_camel_case), ConnectServicesActivity.this));
        mConnectServicesAdapter.add(new ServiceItem(this, getString(R.string.weather_camel_case), ConnectServicesActivity.this));
        Uri redirectUri = getIntent().getData();
        Log.i(TAG, "onResume() - " + redirectUri);
        if (redirectUri != null) {
            Log.i(TAG, "Fitbit authorization request response is " + redirectUri.toString());
            getIntent().replaceExtras(new Bundle());
            getIntent().setAction("");
            getIntent().setData(null);
            getIntent().setFlags(0);
            FitbitAuthTokenHandler fitbitAuthTokenHandler = new FitbitAuthTokenHandler(this,
                    redirectUri.toString());
            this.finish();
            this.overridePendingTransition(0, 0);
            this.startActivity(getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        Intent intent = this.getIntent();
        /*
        if (intent.hasExtra("redirected_from")) {
            if (intent.getStringExtra("redirected_from").equals("empty_view")) {
                inflater.inflate(R.menu.activity_connect_services_menuitem, menu);
            }
        }*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        int id = item.getItemId();
        switch (id) {
            case R.id.activity_connect_services_menuitem_next:
                Intent intent = new Intent(ConnectServicesActivity.this, EditSurveyItemsActivity.class);
                startActivity(intent);
                break;
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "The request code is " + requestCode + "; the result code is " + resultCode);
        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                InputInUse inputInUse = new InputInUse();
                inputInUse.setType("tracker");
                inputInUse.setName(getString(R.string.googlefit_camel_case));
                inputInUse.setInUse(true);
                dataInUseViewModel.insert(inputInUse);
                ConnectedService connectedService = new ConnectedService();
                connectedService.setType("tracker");
                connectedService.setName(getString(R.string.googlefit_camel_case));
                connectedService.setConnected(true);
                dataInUseViewModel.insert(connectedService);
                GoogleFitAPI googleFitAPI = new GoogleFitAPI(getApplication());
                googleFitAPI.downloadData(formattedTime.getCurrentDateAsYYYYMMDD());
                Intent intent = new Intent(getApplicationContext(), MotionSensorService.class);
                startService(intent);
                Log.i(TAG, "Google Fit authentication successful");
            } else {
                InputInUse inputInUse = new InputInUse();
                inputInUse.setType("tracker");
                inputInUse.setName(getString(R.string.googlefit_camel_case));
                inputInUse.setInUse(false);
                dataInUseViewModel.insert(inputInUse);
                ConnectedService connectedService = new ConnectedService();
                connectedService.setType("tracker");
                connectedService.setName(getString(R.string.googlefit_camel_case));
                connectedService.setConnected(false);
                dataInUseViewModel.insert(connectedService);
                Log.i(TAG, "Google Fit authentication failed");
                Toast.makeText(getApplicationContext(), "Authentication failed\nPlease try again", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == FITBIT_PERMISSION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

            } else {

            }
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mFastAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_LOCATION_PERMISSION_REQUEST_CODE) {
            //displayConnectGoogleFitDialog();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        //Toast.makeText(this, getString(R.string.location_rationale), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences getPrefs = androidx.preference.PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        boolean tut1Complete = getPrefs.getBoolean("tut_1_complete", false);
        boolean tut2Complete = getPrefs.getBoolean("tut_2_complete", false);
        boolean tut3Complete = getPrefs.getBoolean("tut_3_complete", false);
        boolean tut4Complete = getPrefs.getBoolean("tut_4_complete", false);
        Log.i(TAG, "Tutorial 1 - " + tut1Complete + " Tutorial 2 - " + tut2Complete
                + " Tutorial 3 - " + tut3Complete + " Tutorial 4 - " + tut4Complete);
        SharedPreferences.Editor e = getPrefs.edit();
        if (!tut3Complete) {
            e.putBoolean("tut_2_complete", false);
            dataInUseViewModel.deleteAllInputs();
            e.commit();
            Intent intent = new Intent(ConnectServicesActivity.this, EditSurveyItemsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
