package com.ivorybridge.moabi.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityFavorited;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityInLibrary;
import com.ivorybridge.moabi.database.entity.baactivity.BAActivityMediatorLiveData;
import com.ivorybridge.moabi.repository.BAActivityRepository;
import com.ivorybridge.moabi.ui.recyclerviewitem.baactivity.BAActivityEditActivityGridItem;
import com.ivorybridge.moabi.ui.util.GridSpacingItemDecoration;
import com.ivorybridge.moabi.viewmodel.BAActivityViewModel;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class EditActivitiesActivity extends AppCompatActivity {

    private static final String TAG = EditActivitiesActivity.class.getSimpleName();
    @BindView(R.id.activity_edit_activity_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_edit_activity_item_eduandcareer_textview)
    TextView educationCareerTextView;
    @BindView(R.id.activity_edit_activity_item_eduandcareer_recyclerview)
    RecyclerView educationCareerRecyclerView;
    @BindView(R.id.activity_edit_activity_item_relationships_textview)
    TextView socialTextView;
    @BindView(R.id.activity_edit_activity_item_relationships_recyclerview)
    RecyclerView socialRecyclerView;
    @BindView(R.id.activity_edit_activity_item_recandinterests_textview)
    TextView recreationInterestTextView;
    @BindView(R.id.activity_edit_activity_item_recandinterests_recyclerview)
    RecyclerView recreationInterestRecyclerView;
    @BindView(R.id.activity_edit_activity_item_mbs_textview)
    TextView mindBodySpiritTextView;
    @BindView(R.id.activity_edit_activity_item_mbs_recyclerview)
    RecyclerView mindBodySpiritRecyclerView;
    @BindView(R.id.activity_edit_activity_item_dr_textview)
    TextView dailyResponsibilityTextView;
    @BindView(R.id.activity_edit_activity_item_dr_recyclerview)
    RecyclerView dailyResponsibilityRecyclerView;
    private FastItemAdapter<BAActivityEditActivityGridItem> educationCareerAdapter;
    private FastItemAdapter<BAActivityEditActivityGridItem> socialAdapter;
    private FastItemAdapter<BAActivityEditActivityGridItem> recreationInterestAdapter;
    private FastItemAdapter<BAActivityEditActivityGridItem> mindBodySpiritAdapter;
    private FastItemAdapter<BAActivityEditActivityGridItem> dailyResponsibilityAdapter;
    //private Set<String> favoritedItems = new LinkedHashSet<>();
    private List<BAActivityEditActivityGridItem> favoritedItems;
    private BAActivityViewModel activityViewModel;
    public Long EDUCATION_CAREER = 0L;
    public Long SOCIAL = 1L;
    public Long RECREATION_OR_INTERESTS = 2L;
    public Long MIND_PHYSICAL_SPIRITUAL = 3L;
    public Long DAILY_RESPONSIBILITY = 4L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_activity);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.edit_activity_title));
        }

        activityViewModel = ViewModelProviders.of(this).get(BAActivityViewModel.class);
        favoritedItems = new ArrayList<>();
        educationCareerTextView.setText(getString(R.string.activity_edu_career_title));
        socialTextView.setText(getString(R.string.activity_relationships_title));
        recreationInterestTextView.setText(getString(R.string.activity_rec_interests_title));
        mindBodySpiritTextView.setText(getString(R.string.activity_mind_body_spirit_title));
        dailyResponsibilityTextView.setText(getString(R.string.activity_daily_responsibility_title));
        educationCareerAdapter = new FastItemAdapter<>();
        socialAdapter = new FastItemAdapter<>();
        recreationInterestAdapter = new FastItemAdapter<>();
        mindBodySpiritAdapter = new FastItemAdapter<>();
        dailyResponsibilityAdapter = new FastItemAdapter<>();
        //educationCareerAdapter.withSelectable(true);
        /*
        educationCareerAdapter.withOnClickListener(new OnClickListener<BAActivityEditActivityGridItem>() {
            @Override
            public boolean onClick(@Nullable View v, IAdapter<BAActivityEditActivityGridItem> adapter, BAActivityEditActivityGridItem item, int position) {
                if (v != null) {
                    //Log.i(TAG, item.getName() + ": " + v.isSelected());
                    if (v.isSelected()) {
                        v.setBackgroundColor(ContextCompat.getColor(EditActivitiesActivity.this, R.color.transparent_gray));
                        favoritedItems.add(item);
                        Log.i(TAG, item.getName() + ": " + item.isFavorited());
                    } else {
                        v.setBackgroundColor(0xFFFFFFFF);
                        if (favoritedItems.contains(item)) {
                            favoritedItems.remove(item);
                        }
                    }
                }
                return true;
            }
        });*/

        int spanCount = 4; // 5 columns
        int spacing = 20; // 20px

        GridLayoutManager gridLayoutManager1 = new GridLayoutManager(this, spanCount);
        GridLayoutManager gridLayoutManager2 = new GridLayoutManager(this, spanCount);
        GridLayoutManager gridLayoutManager3 = new GridLayoutManager(this, spanCount);
        GridLayoutManager gridLayoutManager4 = new GridLayoutManager(this, spanCount);
        GridLayoutManager gridLayoutManager5 = new GridLayoutManager(this, spanCount);
        educationCareerRecyclerView.setLayoutManager(gridLayoutManager1);
        educationCareerRecyclerView.setItemAnimator(null);
        educationCareerRecyclerView.addItemDecoration(
                new GridSpacingItemDecoration(spanCount, spacing, false));
        socialRecyclerView.setLayoutManager(gridLayoutManager2);
        socialRecyclerView.setItemAnimator(null);
        socialRecyclerView.addItemDecoration(new

                GridSpacingItemDecoration(spanCount, spacing, false));
        recreationInterestRecyclerView.setLayoutManager(gridLayoutManager3);
        recreationInterestRecyclerView.setItemAnimator(null);
        recreationInterestRecyclerView.addItemDecoration(new

                GridSpacingItemDecoration(spanCount, spacing, false));
        mindBodySpiritRecyclerView.setLayoutManager(gridLayoutManager4);
        mindBodySpiritRecyclerView.setItemAnimator(null);
        mindBodySpiritRecyclerView.addItemDecoration(new

                GridSpacingItemDecoration(spanCount, spacing, false));
        dailyResponsibilityRecyclerView.setLayoutManager(gridLayoutManager5);
        dailyResponsibilityRecyclerView.setItemAnimator(null);
        dailyResponsibilityRecyclerView.addItemDecoration(new

                GridSpacingItemDecoration(spanCount, spacing, false));

        educationCareerRecyclerView.setAdapter(educationCareerAdapter);
        socialRecyclerView.setAdapter(socialAdapter);
        recreationInterestRecyclerView.setAdapter(recreationInterestAdapter);
        mindBodySpiritRecyclerView.setAdapter(mindBodySpiritAdapter);
        dailyResponsibilityRecyclerView.setAdapter(dailyResponsibilityAdapter);

        activityViewModel = ViewModelProviders.of(this).get(BAActivityViewModel.class);

        BAActivityMediatorLiveData baActivityMediatorLiveData = new BAActivityMediatorLiveData(activityViewModel.getAllActivitiesInLibrary(), activityViewModel.getAllFavoritedActivities());
        baActivityMediatorLiveData.observe(this, new Observer<Pair<List<BAActivityInLibrary>, List<BAActivityFavorited>>>() {
            @Override
            public void onChanged
                    (@Nullable Pair<List<BAActivityInLibrary>, List<BAActivityFavorited>> listListPair) {
                if (listListPair != null) {
                    if (listListPair.first != null && listListPair.second != null) {
                        //Log.i(TAG, listListPair.first.toString());
                        //Log.i(TAG, listListPair.second.toString());
                        Collections.sort(listListPair.first);
                        Collections.sort(listListPair.second);
                        educationCareerAdapter.clear();
                        socialAdapter.clear();
                        recreationInterestAdapter.clear();
                        mindBodySpiritAdapter.clear();
                        dailyResponsibilityAdapter.clear();

                        for (BAActivityInLibrary activityInLibrary : listListPair.first) {
                            Boolean isSelected = false;
                            for (BAActivityFavorited activityFavorited : listListPair.second) {
                                if (activityInLibrary.getName().equals(activityFavorited.getName())) {
                                    isSelected = true;
                                }
                            }
                            BAActivityEditActivityGridItem item = new BAActivityEditActivityGridItem(activityInLibrary, isSelected, getApplication());
                            favoritedItems.add(item);
                            if (activityInLibrary.getActivtyType().equals(EDUCATION_CAREER)) {
                                educationCareerAdapter.add(item);
                            } else if (activityInLibrary.getActivtyType().equals(SOCIAL)) {
                                socialAdapter.add(item);
                            } else if (activityInLibrary.getActivtyType().equals(RECREATION_OR_INTERESTS)) {
                                recreationInterestAdapter.add(item);
                            } else if (activityInLibrary.getActivtyType().equals(MIND_PHYSICAL_SPIRITUAL)) {
                                mindBodySpiritAdapter.add(item);
                            } else if (activityInLibrary.getActivtyType().equals(DAILY_RESPONSIBILITY)) {
                                dailyResponsibilityAdapter.add(item);
                            }

                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_edit_activity_menuitems, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.activity_edit_activity_menuitem_clearselection:
                activityViewModel.deleteAllFavoritedActivities();
                break;
            case R.id.activity_edit_activity_menuitem_clear:
                activityViewModel.deleteAllActivitiesInLibrary();
                activityViewModel.deleteAllFavoritedActivities();
                //activityViewModel.initializeBAActivityInLibrary();
                //activityViewModel.initializeFavoritedActivities();
                break;
            case R.id.activity_edit_activity_menuitem_restore:
                activityViewModel.initializeFavoritedActivities();
                activityViewModel.initializeBAActivityInLibrary();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BAActivityRepository activityRepository = new BAActivityRepository(getApplication());
        Log.i(TAG, favoritedItems.toString());
        for (BAActivityEditActivityGridItem item : favoritedItems) {
            BAActivityFavorited favoritedActivity = new BAActivityFavorited(item.getActivityInLibrary().getName(),
                    item.getActivityInLibrary().getActivtyType(), item.getActivityInLibrary().getActivtyOrder(),
                    item.getActivityInLibrary().getResourceID());
            if (item.isFavorited()) {
                activityRepository.insertFavoritedActivity(favoritedActivity);
            } else {
                activityRepository.deleteFavoritedActivity(item.getName());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //activityViewModel.getAllFavoritedActivities().removeObservers(EditActivitiesActivity.this);
        //activityViewModel.getAllActivitiesInLibrary().removeObservers(EditActivitiesActivity.this);
    }
}
