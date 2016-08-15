package main.tl.com.timelogger.new_entry;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import main.tl.com.timelogger.Application;
import main.tl.com.timelogger.R;
import main.tl.com.timelogger.model.TimeEntry;
import main.tl.com.timelogger.view.SublimePickerFragment;

public class NewEntryFragment extends Fragment {
    private static final String USER_ID = "userId";
    private static final String TIME = "time";

    private String userId;
    private View mProgressView;
    private View mAddFormView;
    private EditText mDateEditText;
    private EditText mDistanceEditText;
    private EditText mTimeEditText;
    private TimeEntry timeToEdit;
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    public NewEntryFragment() {
    }

    public static NewEntryFragment newInstance(String userId, String timeJson) {
        NewEntryFragment fragment = new NewEntryFragment();
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
                timeToEdit = new Gson().fromJson(getArguments().getString(TIME), TimeEntry.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_new_entry, container, false);
        Button addButton = (Button) view.findViewById(R.id.add);
        mProgressView = view.findViewById(R.id.add_progress);
        mAddFormView = view.findViewById(R.id.add_form);
        mDateEditText = (EditText) view.findViewById(R.id.date);
        mDistanceEditText = (EditText) view.findViewById(R.id.distance);
        mTimeEditText = (EditText) view.findViewById(R.id.speed);
        mDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewEntry();

            }
        });

        if (timeToEdit != null) {
            mDateEditText.setText(timeToEdit.getDate());
            mDistanceEditText.setText(timeToEdit.getDistance() + "");
            mTimeEditText.setText(timeToEdit.getTime() + "");
        }
        return view;
    }

    private void createNewEntry() {
        mDateEditText.setError(null);
        mDistanceEditText.setError(null);
        mTimeEditText.setError(null);

        String date = mDateEditText.getText().toString();
        String distance = mDistanceEditText.getText().toString();
        String time = mTimeEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(date)) {
            mDateEditText.setError(getString(R.string.error_field_required));
            focusView = mDateEditText;
            cancel = true;
        }

        if (TextUtils.isEmpty(distance)) {
            mDistanceEditText.setError(getString(R.string.error_field_required));
            focusView = mDistanceEditText;
            cancel = true;
        }
        if (TextUtils.isEmpty(time)) {
            mTimeEditText.setError(getString(R.string.error_field_required));
            focusView = mTimeEditText;
            cancel = true;
        } else if(Double.parseDouble(time) == 0){
            mTimeEditText.setError(getString(R.string.error_time_can_not_be_zero));
            focusView = mTimeEditText;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            Firebase userLog = Application.app.getFirebaseRoot().child("users").child(userId).child("log");
            showProgress(true);
            if (timeToEdit != null) {
                userLog.child(timeToEdit.getKey()).removeValue();
            }
            TimeEntry timeEntry = new TimeEntry(mDateEditText.getText().toString(), Double.parseDouble(mDistanceEditText.getText().toString()), Double.parseDouble(mTimeEditText.getText().toString()));
            userLog.push().setValue(timeEntry, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    getActivity().finish();
                }
            });
        }
    }

    private void showDatePicker() {
        SublimePickerFragment pickerFrag = new SublimePickerFragment();
        pickerFrag.setCallback(new SublimePickerFragment.Callback() {
            @Override
            public void onCancelled() {

            }

            @Override
            public void onDateTimeRecurrenceSet(SelectedDate selectedDate, int hourOfDay, int minute, SublimeRecurrencePicker.RecurrenceOption recurrenceOption, String recurrenceRule) {
                mDateEditText.setText(dateFormat.format(selectedDate.getFirstDate().getTime()));
            }
        });

        SublimeOptions options = new SublimeOptions();
        options.setPickerToShow(SublimeOptions.Picker.DATE_PICKER);
        options.setDisplayOptions(SublimeOptions.ACTIVATE_DATE_PICKER);
        options.setCanPickDateRange(false);
        Bundle bundle = new Bundle();
        bundle.putParcelable("SUBLIME_OPTIONS", options);
        pickerFrag.setArguments(bundle);
        pickerFrag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        pickerFrag.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), "SUBLIME_PICKER");
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
