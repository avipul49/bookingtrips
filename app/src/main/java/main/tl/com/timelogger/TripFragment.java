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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import main.tl.com.timelogger.model.Trip;
import main.tl.com.timelogger.model.User;
import main.tl.com.timelogger.new_entry.NewEntryActivity;
import main.tl.com.timelogger.trip.TripDetails;

public class TripFragment extends Fragment {

    private static final String USER_ID = "userId";
    private static final String USER_NAME = "userName";

    private Date start, end;
    private List<Trip> trips = new ArrayList<>();
    private List<Trip> allTrips = new ArrayList<>();

    private String userId, userName;
    private TripAdapter adapter;
    private Firebase firebaseUserData;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private RecyclerView recyclerView;

    private OnListFragmentInteractionListener mListener = new OnListFragmentInteractionListener() {
        @Override
        public void deleteTime(Trip item) {
            firebaseUserData.child("trip").child(item.getKey()).removeValue();
        }

        @Override
        public void editTime(Trip mItem) {
            Intent intent = new Intent(getActivity(), NewEntryActivity.class);
            intent.putExtra("timeJson", new Gson().toJson(mItem));
            intent.putExtra("userId", userId);
            startActivity(intent);
        }

        @Override
        public void onClick(Trip mItem) {
            Intent intent = new Intent(getActivity(), TripDetails.class);
            intent.putExtra("trip", new Gson().toJson(mItem));
            startActivity(intent);
        }
    };


    public TripFragment() {
    }

    public static TripFragment newInstance(String userId) {
        TripFragment fragment = new TripFragment();
        Bundle args = new Bundle();
        args.putString(USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    public static TripFragment newInstance(String userId, String userName) {
        TripFragment fragment = new TripFragment();
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
        Context context = view.getContext();
        recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        firebaseUserData.child("trips").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                updateTimeList(snapshot);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
        if (userId.equals(User.getCurrentUser().getUid())) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.time_log));
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(userName + "'s " + getString(R.string.time_log));
        }
        adapter = new TripAdapter(trips, mListener);
        recyclerView.setAdapter(adapter);
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
        void deleteTime(Trip item);

        void editTime(Trip mItem);

        void onClick(Trip mItem);
    }

    public void filter(Date start, Date end) {
        this.start = start;
        this.end = end;
        adapter.notifyDataSetChanged();
    }

    private void updateTimeList(DataSnapshot snapshot) {
        trips.clear();
        for (DataSnapshot ds : snapshot.getChildren()) {
            Trip temp = ds.getValue(Trip.class);
            temp.setKey(ds.getKey());
            trips.add(temp);
        }
        Collections.sort(trips, new Comparator<Trip>() {
            @Override
            public int compare(Trip t1, Trip t2) {
                try {
                    return dateFormat.parse(t2.getStartDate()).compareTo(dateFormat.parse(t1.getStartDate()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
        allTrips.addAll(trips);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.time_log));
        }
    }
}
