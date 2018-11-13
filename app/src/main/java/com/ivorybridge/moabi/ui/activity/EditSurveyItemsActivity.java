package com.ivorybridge.moabi.ui.activity;

import android.os.Bundle;
import android.view.View;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.ui.recyclerviewitem.entryitem.EntryItemItem;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.util.Arrays;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class EditSurveyItemsActivity extends AppCompatActivity {

    @BindView(R.id.activity_connect_services_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_connect_services_vertical_recyclerview)
    RecyclerView recyclerView;
    // adapters
    private FastAdapter<IItem> fastAdapter;
    private ItemAdapter<EntryItemItem> userInputsInUseItemItemAdapter;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_services);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                //Intent intent = new Intent(EditSurveyItemsActivity.this, MainActivity.class);
                //startActivity(intent);
                //finish();
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

        // set up recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(fastAdapter);
        userInputsInUseItemItemAdapter.add(new EntryItemItem(this, getString(R.string.daily_review_camel_case)));
        userInputsInUseItemItemAdapter.add(new EntryItemItem(this, getString(R.string.mood_and_energy_camel_case)));
        userInputsInUseItemItemAdapter.add(new EntryItemItem(this, getString(R.string.stress_camel_case)));
        userInputsInUseItemItemAdapter.add(new EntryItemItem(this, getString(R.string.depression_phq9_camel_case)));
        userInputsInUseItemItemAdapter.add(new EntryItemItem(this, getString(R.string.anxiety_gad7_camel_case)));
        //userInputsInUseItemItemAdapter.add(new EntryItemItem(this, getString(R.string.pain_camel_case)));
        userInputsInUseItemItemAdapter.add(new EntryItemItem(this, getString(R.string.baactivity_camel_case)));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
