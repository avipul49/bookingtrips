package main.tl.com.timelogger.new_place;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import main.tl.com.timelogger.R;
import main.tl.com.timelogger.model.City;
import main.tl.com.timelogger.model.Destination;
import main.tl.com.timelogger.model.Message;
import main.tl.com.timelogger.model.Trip;
import main.tl.com.timelogger.model.User;
import main.tl.com.timelogger.view.SublimePickerFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewPlaceFragment extends Fragment {
    private static final String USER_ID = "userId";
    private static final String TIME = "time";
    private static final String ORDER = "order";
    private static final String CITY = "city";

    private String userId;
    private View mProgressView;
    private View mAddFormView;
    private EditText mDateEditText;
    private AutoCompleteTextView mNameEditText;
    private Trip trip;
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private City city = new City();
    private AutocompleteAdapter autocompleteAdapter;
    private ArrayList<Destination> destinations;
    private boolean inProgress = false;
    private Destination selectedDestination;
    private NumberPicker np;
    private CheckBox mFlexibleCB;
    private int order;
    private boolean editing;

    public NewPlaceFragment() {
    }

    public static NewPlaceFragment newInstance(String userId, String timeJson, int order, String cityJson) {
        NewPlaceFragment fragment = new NewPlaceFragment();
        Bundle args = new Bundle();
        args.putString(USER_ID, userId);
        args.putString(TIME, timeJson);
        args.putInt(ORDER, order);
        args.putString(CITY, cityJson);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            userId = getArguments().getString(USER_ID);
            order = getArguments().getInt(ORDER);
            if (getArguments().getString(TIME) != null)
                trip = new Gson().fromJson(getArguments().getString(TIME), Trip.class);
            if (getArguments().getString(CITY) != null) {
                city = new Gson().fromJson(getArguments().getString(CITY), City.class);
                editing = true;
            }

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
        mDateEditText = (EditText) view.findViewById(R.id.startDate);
        mNameEditText = (AutoCompleteTextView) view.findViewById(R.id.name);
        destinations = new ArrayList<>();
        autocompleteAdapter = new AutocompleteAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, destinations);
        mNameEditText.setAdapter(autocompleteAdapter);
        mFlexibleCB = (CheckBox) view.findViewById(R.id.flexible);
        mNameEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() < 3) {
                    return;
                }
                if (inProgress) {
                    return;
                }
//                inProgress = true;
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            NewPlaceFragment.this.run();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, 300);
            }
        });
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
        mNameEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedDestination = destinations.get(i);
                mNameEditText.setText(selectedDestination.getName());
            }
        });

        np = (NumberPicker) view.findViewById(R.id.np);
        np.setMinValue(0);
        np.setMaxValue(10);
        np.setWrapSelectorWheel(true);

        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

            }
        });
        setDividerColor(np, getResources().getColor(R.color.colorAccent));
        if (editing) {
            mNameEditText.setText(city.getName());
            np.setValue(city.getDays());
            mFlexibleCB.setChecked(city.isFlexible());
        }
        return view;
    }

    private void createNewEntry() {
        mDateEditText.setError(null);
        mNameEditText.setError(null);

        String date = mDateEditText.getText().toString();
        String name = mNameEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;
//        if (TextUtils.isEmpty(date)) {
//            mDateEditText.setError(getString(R.string.error_field_required));
//            focusView = mDateEditText;
//            cancel = true;
//        }

        if (TextUtils.isEmpty(name)) {
            mNameEditText.setError(getString(R.string.error_field_required));
            focusView = mNameEditText;
            cancel = true;
        }

//        if (selectedDestination == null) {
//            mNameEditText.setError("Please select a destination");
//            focusView = mNameEditText;
//            cancel = true;
//        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

            DatabaseReference userLog = mDatabase.child("trips").child(trip.getKey()).child("places");
            showProgress(true);
            if (trip != null) {
                userLog.child(trip.getKey()).removeValue();
            }
            city.setDays(np.getValue());
            city.setName(mNameEditText.getText().toString());
            city.setUserId(User.getCurrentUser().getUid());
            city.setUserName(User.getCurrentUser().getName());
            city.setFlexible(mFlexibleCB.isChecked());
//            city.setCityId(selectedDestination.getDestId());
            city.setOrder(order);
            if (editing) {
                userLog.child(city.getKey()).setValue(city, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            mDatabase.child("trips").child(databaseReference.getKey()).child("members").push().setValue(User.getCurrentUser());
                            Message message = new Message();
                            message.setMessage(city.getName() + " was updated by " + User.getCurrentUser().getName());
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
            } else {
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

    static class AutocompleteAdapter extends ArrayAdapter<Destination> {

        List<Destination> destinations;
        private Context context;

        public AutocompleteAdapter(Context context, int resource, List<Destination> destinations) {
            super(context, resource, destinations);
            this.destinations = destinations;
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            }
            ((TextView) convertView).setText(destinations.get(position).getName());
            return convertView;
        }

        @Override
        public Destination getItem(int position) {
            return destinations.get(position);
        }

        @Override
        public int getCount() {
            return destinations.size();
        }
    }

    OkHttpClient client = new OkHttpClient();

    void run() throws IOException {
        String text = mNameEditText.getText().toString();
        if (text.length() < 3) {
            destinations.clear();
            autocompleteAdapter.notifyDataSetChanged();
            inProgress = false;
            return;
        }
        String url = "https://distribution-xml.booking.com/json/bookings.autocomplete?text=%s&languagecode=en";
        Request request = new Request.Builder()
                .url(String.format(url, text)).addHeader("Authorization", "Basic aGFja2VyMjQwOjZQSmZ5UUZMbjQ=")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("<>-", "<>- " + e.getMessage());
                inProgress = false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                inProgress = false;
                String temp = response.body().string();
                final Destination[] destinations = new Gson().fromJson(temp, Destination[].class);
                NewPlaceFragment.this.destinations.clear();
                for (Destination destination : destinations) {
                    if (destination.getDestType().equals("city"))
                        NewPlaceFragment.this.destinations.add(destination);
                }
                if (getActivity() != null)
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            autocompleteAdapter.notifyDataSetChanged();
                        }
                    });

            }
        });
    }

    private void setDividerColor(NumberPicker picker, int color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
