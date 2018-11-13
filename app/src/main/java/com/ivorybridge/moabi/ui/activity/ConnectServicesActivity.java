package com.ivorybridge.moabi.ui.activity;

import android.app.Activity;
import android.content.DialogInterface;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.util.ConnectedService;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.network.auth.AuthStateManager;
import com.ivorybridge.moabi.network.auth.FitbitAuthTokenHandler;
import com.ivorybridge.moabi.network.auth.GoogleFitService;
import com.ivorybridge.moabi.ui.recyclerviewitem.connectservicesactivity.ServiceItem;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * This class displays UI for ConnectServicesActivity, which displays a list of services
 * that users can connect to, such as Fitbit, GoogleFitService and built-in app usage tracker.
 *
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


    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

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

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mFastAdapter);

        mConnectServicesAdapter.add(new ServiceItem(this, getString(R.string.fitbit_camel_case), ConnectServicesActivity.this));
        mConnectServicesAdapter.add(new ServiceItem(this, getString(R.string.moabi_tracker_camel_case), ConnectServicesActivity.this));
        mConnectServicesAdapter.add(new ServiceItem(this, getString(R.string.googlefit_camel_case), ConnectServicesActivity.this));
        mConnectServicesAdapter.add(new ServiceItem(this, getString(R.string.phone_usage_camel_case), ConnectServicesActivity.this));
        mConnectServicesAdapter.add(new ServiceItem(this, getString(R.string.weather_camel_case), ConnectServicesActivity.this));

        fitbitAuthStateSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        authStateManager = AuthStateManager.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        Intent intent = this.getIntent();
        if (intent.hasExtra("redirected_from")) {
            if (intent.getStringExtra("redirected_from").equals("empty_view")) {
                inflater.inflate(R.menu.activity_connect_services_menuitem, menu);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.activity_connect_services_menuitem_next:
                Intent intent = new Intent(ConnectServicesActivity.this, EditSurveyItemsActivity.class);
                startActivity(intent);
                break;
        }
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
            displayConnectGoogleFitDialog();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "Google Fit requires location permission to provide fitness data", Toast.LENGTH_LONG).show();
    }

    private void displayConnectGoogleFitDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.googlefit_permission_title)
                .content(R.string.googlefit_permission_prompt)
                .limitIconToDefaultSize()
                .positiveText(R.string.dialog_positive_text)
                .negativeText(R.string.dialog_negative_text)
                .autoDismiss(true)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        GoogleFitService gFit = new GoogleFitService(ConnectServicesActivity.this);
                        gFit.requestPermission();
                        dialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
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
                        dialog.dismiss();
                        ConnectServicesActivity.this.finish();
                        ConnectServicesActivity.this.overridePendingTransition(0, 0);
                        ConnectServicesActivity.this.startActivity(ConnectServicesActivity.this.getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
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
}
