package com.ivorybridge.moabi.ui.recyclerviewitem.entryitem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.DataInUseRepository;
import com.ivorybridge.moabi.ui.activity.ConnectServicesActivity;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.refactor.library.SmoothCheckBox;

public class EntryItemItem extends AbstractItem<EntryItemItem, EntryItemItem.ViewHolder> {

    private static final String TAG = EntryItemItem.class.getSimpleName();
    private AppCompatActivity activity;
    private String mitemTitle;

    public EntryItemItem(AppCompatActivity activity, String item) {
        this.activity = activity;
        mitemTitle = item;
    }

    @Override
    public int getType() {
        return R.id.userinputsinuse_item;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.rv_item_connectservices_rv_item;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new EntryItemItem.ViewHolder(v);
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<EntryItemItem> {

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
        private SharedPreferences customItemSharedPreferences;
        private static final String CUSTOM_ITEM_TOKEN_PREFERENCES = "CustomItemTokenPrefs";
        private ValueEventListener inputsInUseValueEventListener;
        private DataInUseRepository dataInUseRepository;
        private DataInUseViewModel dataInUseViewModel;
        private FirebaseManager firebaseManager;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void bindView(final EntryItemItem item, List<Object> payloads) {

            customItemSharedPreferences = itemView.getContext().getSharedPreferences(
                    CUSTOM_ITEM_TOKEN_PREFERENCES, Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = customItemSharedPreferences.edit();
            firebaseManager = new FirebaseManager();
            dataInUseRepository = new DataInUseRepository(item.activity.getApplication());
            final String itemType = item.mitemTitle;
            initializeCheckBox(item, itemType, checkBox);
            SharedPreferences getPrefs = PreferenceManager
                    .getDefaultSharedPreferences(itemView.getContext());
            SharedPreferences.Editor e = getPrefs.edit();
            boolean tut1Complete = getPrefs.getBoolean("tut_1_complete", false);
            if (itemType.equals(itemView.getContext().getString(R.string.mood_and_energy_camel_case))) {
                iconImageView.setImageResource(R.drawable.ic_emotion);
                descriptionTextView.setText(itemView.getContext().getString(R.string.mood_and_energy_desc));
                titleTextView.setText(itemView.getContext().getString(R.string.mood_and_energy_title));
                if (!tut1Complete) {
                    TapTargetView.showFor(item.activity, TapTarget.forView(itemView.getRootView().findViewById(R.id.activity_connected_services_recyclerview_item_cardview),
                            itemView.getContext().getString(R.string.tutorial_edit_entry_title),
                            itemView.getContext().getString(R.string.tutorial_edit_entry_msg))
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
                                    inputInUse.setType("survey");
                                    inputInUse.setName(itemType);
                                    inputInUse.setInUse(true);
                                    dataInUseRepository.insert(inputInUse);
                                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                                        firebaseManager.getInputsInUseRef().child(itemType).setValue(true);
                                    }
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent tutorialIntent = new Intent(item.activity, ConnectServicesActivity.class);
                                            itemView.getContext().startActivity(tutorialIntent);
                                        }
                                    }, 300);
                                }
                            });
                }
            } else if (itemType.equals(itemView.getContext().getString(R.string.baactivity_camel_case))) {
                iconImageView.setImageResource(R.drawable.ic_physical_activity_black);
                descriptionTextView.setText(itemView.getContext().getString(R.string.baactivity_desc));
                titleTextView.setText(itemView.getContext().getString(R.string.baactivity_title));
            } else if (itemType.equals(itemView.getContext().getString(R.string.stress_camel_case))) {
                iconImageView.setImageResource(R.drawable.ic_stress);
                descriptionTextView.setText(itemView.getContext().getString(R.string.stress_desc));
                titleTextView.setText(itemView.getContext().getString(R.string.stress_title));
            } else if (itemType.equals(itemView.getContext().getString(R.string.pain_camel_case))) {
                iconImageView.setImageResource(R.drawable.ic_pain_black);
                descriptionTextView.setText(itemView.getContext().getString(R.string.pain_desc));
                titleTextView.setText(itemView.getContext().getString(R.string.pain_title));
            } else if (itemType.equals(itemView.getContext().getString(R.string.daily_review_camel_case))) {
                iconImageView.setImageResource(R.drawable.ic_review_black);
                descriptionTextView.setText(itemView.getContext().getString(R.string.daily_review_desc));
                titleTextView.setText(itemView.getContext().getString(R.string.daily_review_title));
            } else if (itemType.equals(itemView.getContext().getString(R.string.depression_phq9_camel_case))) {
                iconImageView.setImageResource(R.drawable.ic_depression_rain_black);
                descriptionTextView.setText(itemView.getContext().getString(R.string.depression_phq9_dsc));
                titleTextView.setText(itemView.getContext().getString(R.string.depression_phq9_title));
            } else if (itemType.equals(itemView.getContext().getString(R.string.anxiety_gad7_camel_case))) {
                iconImageView.setImageResource(R.drawable.ic_anxiety_insomnia_black);
                descriptionTextView.setText(itemView.getContext().getString(R.string.anxiety_gad7_dsc));
                titleTextView.setText(itemView.getContext().getString(R.string.anxiety_gad7_title));
            }

            checkBox.setEnabled(false);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBox.isChecked()) {
                        checkBox.setChecked(false, true);
                        InputInUse inputInUse = new InputInUse();
                        inputInUse.setType("survey");
                        inputInUse.setName(itemType);
                        inputInUse.setInUse(false);
                        dataInUseRepository.insert(inputInUse);
                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            firebaseManager.getInputsInUseRef().child(itemType).setValue(false);
                        }
                    } else {
                        InputInUse inputInUse = new InputInUse();
                        inputInUse.setType("survey");
                        inputInUse.setName(itemType);
                        inputInUse.setInUse(true);
                        dataInUseRepository.insert(inputInUse);
                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            firebaseManager.getInputsInUseRef().child(itemType).setValue(true);
                        }
                        checkBox.setChecked(true, true);
                    }
                }
            });
        }

        private void initializeCheckBox(final EntryItemItem item, final String itemType, final SmoothCheckBox checkBox) {
            // if the user has decided to use the service, enable the checkbox. else, disable it.
            // if the user has connected to the service, enable the checkbox. else, disable it.
            dataInUseViewModel = ViewModelProviders.of(item.activity).get(DataInUseViewModel.class);
            Handler handler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<InputInUse> inputInUses = dataInUseViewModel.getAllInputsInUseNow();
                    if (inputInUses != null && inputInUses.size() > 0) {
                        for (InputInUse inputInUse : inputInUses) {
                            if (inputInUse.getName().equals(itemType)) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        checkBox.setChecked(inputInUse.isInUse());
                                    }
                                });
                            }
                        }
                    }
                }
            }).start();
        }

        @Override
        public void unbindView(EntryItemItem item) {
            //personalCustomUserInputsInUseRef.removeEventListener(valueEventListener);
        }
    }
}
