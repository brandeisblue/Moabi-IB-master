package com.ivorybridge.moabi.ui.recyclerviewitem.entryitem;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;
import com.ivorybridge.moabi.R;
import com.ivorybridge.moabi.database.entity.util.InputInUse;
import com.ivorybridge.moabi.database.firebase.FirebaseManager;
import com.ivorybridge.moabi.repository.DataInUseRepository;
import com.ivorybridge.moabi.viewmodel.DataInUseViewModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProviders;
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
            if (itemType.equals(itemView.getContext().getString(R.string.mood_and_energy_camel_case))) {
                iconImageView.setImageResource(R.drawable.ic_emotion);
                descriptionTextView.setText(itemView.getContext().getString(R.string.mood_and_energy_desc));
                titleTextView.setText(itemView.getContext().getString(R.string.mood_and_energy_title));
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
                        for (InputInUse inputInUse: inputInUses) {
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
            /*
            dataInUseViewModel.getAllInputsInUse().observe(item.activity, new Observer<List<InputInUse>>() {
                @Override
                public void onChanged(List<InputInUse> inputInUses) {
                    if (inputInUses != null && inputInUses.size() > 0) {
                        for (InputInUse inputInUse: inputInUses) {
                            if (inputInUse.getName().equals(itemType)) {
                                checkBox.setChecked(inputInUse.isInUse());
                            }
                        }
                    }
                }
            });*/
            /*
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                inputsInUseValueEventListener = firebaseManager.getInputsInUseRef().addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(itemType).getKey() != null) {
                            Boolean isInUse = (Boolean) dataSnapshot.child(itemType).getValue();
                            if (isInUse != null) {
                                checkBox.setChecked(isInUse);
                            } else {
                                checkBox.setChecked(false, true);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }*/
        }

        @Override
        public void unbindView(EntryItemItem item) {
            //personalCustomUserInputsInUseRef.removeEventListener(valueEventListener);
        }
    }
}
