package main.tl.com.timelogger.new_place;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import main.tl.com.timelogger.R;
import main.tl.com.timelogger.model.City;
import main.tl.com.timelogger.model.Message;
import main.tl.com.timelogger.model.Trip;
import main.tl.com.timelogger.model.User;
import main.tl.com.timelogger.view.SublimePickerFragment;

public class NewPlaceFragment extends Fragment {
    private static final String USER_ID = "userId";
    private static final String TIME = "time";

    private String userId;
    private View mProgressView;
    private View mAddFormView;
    private EditText mDateEditText;
    private EditText mNameEditText;
    private Trip trip;
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private City city = new City();

    public NewPlaceFragment() {
    }

    public static NewPlaceFragment newInstance(String userId, String timeJson) {
        NewPlaceFragment fragment = new NewPlaceFragment();
        Bundle args = new Bundle();
        args.putString(USER_ID, userId);
        args.putString(TIME, timeJson);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            userId = getArguments().getString(USER_ID);
            if (getArguments().getString(TIME) != null)
                trip = new Gson().fromJson(getArguments().getString(TIME), Trip.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_new_city, container, false);
        Button addButton = (Button) view.findViewById(R.id.add);
        mProgressView = view.findViewById(R.id.add_progress);
        mAddFormView = view.findViewById(R.id.add_form);
        mDateEditText = (EditText) view.findViewById(R.id.date);
        mNameEditText = (EditText) view.findViewById(R.id.name);
        mDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateRangePicker();
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewEntry();

            }
        });

        return view;
    }

    private void createNewEntry() {
        mDateEditText.setError(null);
        mNameEditText.setError(null);

        String date = mDateEditText.getText().toString();
        String distance = mNameEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(date)) {
            mDateEditText.setError(getString(R.string.error_field_required));
            focusView = mDateEditText;
            cancel = true;
        }

        if (TextUtils.isEmpty(distance)) {
            mNameEditText.setError(getString(R.string.error_field_required));
            focusView = mNameEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

            DatabaseReference userLog = mDatabase.child("trips").child(trip.getKey()).child("places");
            showProgress(true);
            if (trip != null) {
                userLog.child(trip.getKey()).removeValue();
            }

            city.setName(mNameEditText.getText().toString());
            city.setUserId(User.getCurrentUser().getUid());
            city.setUserName(User.getCurrentUser().getName());
            userLog.push().setValue(city, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        mDatabase.child("trips").child(databaseReference.getKey()).child("members").push().setValue(User.getCurrentUser());
                        Message message = new Message();
                        message.setMessage(city.getName() + " was added by " + User.getCurrentUser().getName());
                        message.setDate(System.currentTimeMillis());
                        message.setType("ACTION");
                        message.setUserId(User.getCurrentUser().getUid());
                        message.setUserName(User.getCurrentUser().getName());
                        mDatabase.child("trips").child(trip.getKey()).child("discussion").push().setValue(message, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                getActivity().finish();
                            }
                        });
                    } else {
                        getActivity().finish();
                    }

                }
            });
        }
    }

    private void showDateRangePicker() {
        SublimePickerFragment pickerFrag = new SublimePickerFragment();
        pickerFrag.setCallback(new SublimePickerFragment.Callback() {
            @Override
            public void onCancelled() {

            }

            @Override
            public void onDateTimeRecurrenceSet(SelectedDate selectedDate, int hourOfDay, int minute, SublimeRecurrencePicker.RecurrenceOption recurrenceOption, String recurrenceRule) {
                city.setStartDate(dateFormat.format(selectedDate.getFirstDate().getTime()));
                city.setEndDate(dateFormat.format(selectedDate.getSecondDate().getTime()));
                mDateEditText.setText(dateFormat.format(selectedDate.getFirstDate().getTime()) + " to " + dateFormat.format(selectedDate.getSecondDate().getTime()));
            }
        });

        SublimeOptions options = new SublimeOptions();
        options.setPickerToShow(SublimeOptions.Picker.DATE_PICKER);
        options.setDisplayOptions(SublimeOptions.ACTIVATE_DATE_PICKER);
        options.setCanPickDateRange(true);
        Bundle bundle = new Bundle();
        bundle.putParcelable("SUBLIME_OPTIONS", options);
        pickerFrag.setArguments(bundle);
        //pickerFrag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        pickerFrag.show(((NewPlaceActivity) getActivity()).getSupportFragmentManager(), "SUBLIME_PICKER");
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mAddFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mAddFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mAddFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mAddFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
