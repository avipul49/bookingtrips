package main.tl.com.timelogger;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import main.tl.com.timelogger.model.TimeEntry;
import main.tl.com.timelogger.model.User;
import main.tl.com.timelogger.model.WeekDetail;
import main.tl.com.timelogger.new_entry.NewEntryActivity;

public class TimeListFragment extends Fragment {

    private static final String USER_ID = "userId";
    private static final String USER_NAME = "userName";

    private Date start, end;
    private List<TimeEntry> timeList;
    private List<TimeListAdapter.DisplayItem> displayItems;
    private String userId, userName;
    private TimeListAdapter adapter;
    private Firebase firebaseUserData;
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private RecyclerView recyclerView;

    private OnListFragmentInteractionListener mListener = new OnListFragmentInteractionListener() {
        @Override
        public void deleteTime(TimeEntry item) {
            firebaseUserData.child("log").child(item.getKey()).removeValue();
        }

        @Override
        public void editTime(TimeEntry mItem) {
            Intent intent = new Intent(getActivity(), NewEntryActivity.class);
            intent.putExtra("timeJson", new Gson().toJson(mItem));
            intent.putExtra("userId", userId);
            startActivity(intent);
        }
    };


    public TimeListFragment() {
    }

    public static TimeListFragment newInstance(String userId) {
        TimeListFragment fragment = new TimeListFragment();
        Bundle args = new Bundle();
        args.putString(USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    public static TimeListFragment newInstance(String userId, String userName) {
        TimeListFragment fragment = new TimeListFragment();
        Bundle args = new Bundle();
        args.putString(USER_ID, userId);
        args.putString(USER_NAME, userName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            userId = getArguments().getString(USER_ID);
            userName = getArguments().getString(USER_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_list, container, false);
        firebaseUserData = Application.app.getFirebaseRoot().child("users").child(userId);
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            firebaseUserData.child("log").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    updateTimeList(snapshot);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    System.out.println("The read failed: " + firebaseError.getMessage());
                }
            });
        }
        if (userId.equals(User.getCurrentUser().getUid())) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.time_log));
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(userName + "'s " + getString(R.string.time_log));
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void deleteTime(TimeEntry item);

        void editTime(TimeEntry mItem);
    }

    public List<TimeListAdapter.DisplayItem> getDisplayItems(List<TimeEntry> log) {
        List<TimeListAdapter.DisplayItem> displayItems = new ArrayList<>();
        WeekDetail currentWeek = null;
        Calendar cal = Calendar.getInstance();
        for (TimeEntry t : log) {
            try {
                Date current = dateFormat.parse(t.getDate());

                if (start == null || end == null || (current.after(start) && current.before(end))) {

                    cal.setTime(current);
                    int week = cal.get(Calendar.WEEK_OF_YEAR);
                    int year = cal.get(Calendar.YEAR);
                    if (currentWeek == null || week != currentWeek.getWeekOfYear() || year != currentWeek.getYear()) {
                        currentWeek = new WeekDetail();
                        currentWeek.setWeekOfYear(week);
                        currentWeek.setYear(year);
                        cal.add(Calendar.DAY_OF_WEEK,
                                cal.getFirstDayOfWeek() - cal.get(Calendar.DAY_OF_WEEK));
                        currentWeek.setStartDate(dateFormat.format(cal.getTime()));
                        cal.add(Calendar.DAY_OF_YEAR, 6);
                        currentWeek.setEndDate(dateFormat.format(cal.getTime()));
                        displayItems.add(currentWeek);
                    }
                    currentWeek.setTotalDistance(currentWeek.getTotalDistance() + t.getDistance());
                    currentWeek.setTotalTime(currentWeek.getTotalTime() + t.getTime());
                    currentWeek.setNumberOfEntries(currentWeek.getNumberOfEntries() + 1);
                    displayItems.add(t);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return displayItems;
    }

    public void filter(Date start, Date end) {
        this.start = start;
        this.end = end;
        displayItems.clear();
        displayItems.addAll(getDisplayItems(timeList));
        adapter.notifyDataSetChanged();
    }

    private void updateTimeList(DataSnapshot snapshot) {
        List<TimeEntry> timeList = new ArrayList<TimeEntry>();
        for (DataSnapshot ds : snapshot.getChildren()) {
            TimeEntry temp = ds.getValue(TimeEntry.class);
            temp.setKey(ds.getKey());
            timeList.add(temp);
        }
        Collections.sort(timeList, new Comparator<TimeEntry>() {
            @Override
            public int compare(TimeEntry t1, TimeEntry t2) {
                try {
                    return dateFormat.parse(t2.getDate()).compareTo(dateFormat.parse(t1.getDate()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
        TimeListFragment.this.timeList = timeList;
        TimeListFragment.this.displayItems = getDisplayItems(timeList);
        adapter = new TimeListAdapter(displayItems, mListener);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.time_log));
        }
    }
}
