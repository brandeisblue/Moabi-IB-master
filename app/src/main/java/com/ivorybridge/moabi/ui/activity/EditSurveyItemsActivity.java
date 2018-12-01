package com.ivorybridge.moabi.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.ui.recyclerviewitem.entryitem.EntryItemItem;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class EditSurveyItemsActivity extends AppCompatActivity {

    private static final String TAG = EditSurveyItemsActivity.class.getSimpleName();
    @BindView(R.id.activity_connect_services_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_connect_services_vertical_recyclerview)
    RecyclerView recyclerView;
    // adapters
    private FastAdapter<IItem> fastAdapter;
    private ItemAdapter<EntryItemItem> userInputsInUseItemItemAdapter;
    private DataInUseViewModel dataInUseViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_services);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        dataInUseViewModel = ViewModelProviders.of(this).get(DataInUseViewModel.class);
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        boolean tut1Complete = getPrefs.getBoolean("tut_1_complete", false);
        if (!tut1Complete) {
            dataInUseViewModel.deleteAllInputs();
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.surveys_title));
        }

        // set up custom item adapter
        userInputsInUseItemItemAdapter = new ItemAdapter<>();

        // populate fast adapter with item adapters
        fastAdapter = FastAdapter.with(Arrays.asList(userInputsInUseItemItemAdapter));
        fastAdapter.withSelectable(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences getPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        boolean tut1Complete = getPrefs.getBoolean("tut_1_complete", false);
        boolean tut2Complete = getPrefs.getBoolean("tut_2_complete", false);
        boolean tut3Complete = getPrefs.getBoolean("tut_3_complete", false);
        boolean tut4Complete = getPrefs.getBoolean("tut_4_complete", false);
        Log.i(TAG, "Tutorial 1 - " + tut1Complete + " Tutorial 2 - " + tut2Complete
                + " Tutorial 3 - " + tut3Complete + " Tutorial 4 - " + tut4Complete);
        SharedPreferences.Editor e = getPrefs.edit();
        if (!tut2Complete) {
            Log.i(TAG, "Tutorial 1 not complete");
            e.putBoolean("tut_1_complete", false);
            dataInUseViewModel.deleteAllInputs();
            e.commit();
            Intent intent = new Intent(EditSurveyItemsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        } else {
            if (getIntent().getStringExtra("redirected_from") != null) {
                if (getIntent().getStringExtra("redirected_from").equals("makeEntryActivity")) {
                    Intent intent = new Intent(EditSurveyItemsActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                } else if (getIntent().getStringExtra("redirected_from").equals("settingsFragment")) {
                    Intent intent = new Intent(EditSurveyItemsActivity.this, SettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
            }
            //Intent intent = new Intent(EditSurveyItemsActivity.this, MainActivity.class);
            //startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // set up recyclerview
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
        recyclerView.setAdapter(fastAdapter);
        userInputsInUseItemItemAdapter.clear();
        userInputsInUseItemItemAdapter.add(new EntryItemItem(this, getString(R.string.daily_review_camel_case)));
        userInputsInUseItemItemAdapter.add(new EntryItemItem(this, getString(R.string.mood_and_energy_camel_case)));
        userInputsInUseItemItemAdapter.add(new EntryItemItem(this, getString(R.string.stress_camel_case)));
        userInputsInUseItemItemAdapter.add(new EntryItemItem(this, getString(R.string.depression_phq9_camel_case)));
        userInputsInUseItemItemAdapter.add(new EntryItemItem(this, getString(R.string.anxiety_gad7_camel_case)));
        //userInputsInUseItemItemAdapter.add(new EntryItemItem(this, getString(R.string.pain_camel_case)));
        userInputsInUseItemItemAdapter.add(new EntryItemItem(this, getString(R.string.baactivity_camel_case)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
