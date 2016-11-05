package main.tl.com.timelogger.trip;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import main.tl.com.timelogger.Application;
import main.tl.com.timelogger.R;
import main.tl.com.timelogger.helper.OnStartDragListener;
import main.tl.com.timelogger.helper.SimpleItemTouchHelperCallback;
import main.tl.com.timelogger.model.City;
import main.tl.com.timelogger.model.Trip;
import main.tl.com.timelogger.model.User;
import main.tl.com.timelogger.new_place.NewPlaceActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by vipulmittal on 14/10/16.
 */
public class PlacesFragment extends Fragment implements OnStartDragListener, TripDetails.OnPlacesActionListener, SuggestionsAdapter.SelectOption {
    private ItemTouchHelper mItemTouchHelper;

    public PlacesFragment() {
    }

    @Override
    public void editPlace(City item) {
        Intent intent = new Intent(getActivity(), NewPlaceActivity.class);
        intent.putExtra("city", new Gson().toJson(item));
        intent.putExtra("userId", User.getCurrentUser().getUid());
        intent.putExtra("trip", new Gson().toJson(trip));
        intent.putExtra("order", item.getOrder());
        getActivity().startActivity(intent);
    }

    @Override
    public void deletePlace(City item) {
        Application.app.getFirebaseRoot().child("trips").child(trip.getKey()).child("places").child(item.getKey()).removeValue();
    }

    Trip trip;
    ArrayList<City> cities = new ArrayList<>();
    PlaceAdapter adapter;
    private SlidingUpPanelLayout slidingPaneLayout;
    private ProgressBar progressBar;
    private TextView amount;
    private RecyclerView rvSuggestions;
    private SuggestionsAdapter suggestionsAdapter;
    private ArrayList<Trip> suggestions = new ArrayList<>();

    public static PlacesFragment newInstance(Trip trip) {
        PlacesFragment fragment = new PlacesFragment();
        Bundle bundle = new Bundle();
        bundle.putString("trip", new Gson().toJson(trip));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            trip = new Gson().fromJson(getArguments().getString("trip"), Trip.class);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_trip_details, container, false);
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
        adapter = new PlaceAdapter(getActivity(), cities, trip, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        }, this, this);
        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.rv_places);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), NewPlaceActivity.class);
                intent.putExtra("userId", User.getCurrentUser().getUid());
                intent.putExtra("trip", new Gson().toJson(trip));
                intent.putExtra("order", cities.size() + 1);
                getActivity().startActivity(intent);
            }
        });
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("trips").child(trip.getKey()).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                trip.setPersonsCount((int) dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
        mDatabase.child("trips").child(trip.getKey()).child("places").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cities.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    City temp = ds.getValue(City.class);
                    temp.setKey(ds.getKey());
                    cities.add(temp);
                }
                Collections.sort(cities);
                trip.setCities(cities);
//                try {
//                    if (trip.getCities().size() > 0)
//                        run(new Gson().toJson(trip));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(rv);
        slidingPaneLayout = (SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout);
        progressBar = (ProgressBar) rootView.findViewById(R.id.hotel_refresh);
        amount = (TextView) rootView.findViewById(R.id.amount);
        rvSuggestions = (RecyclerView) rootView.findViewById(R.id.suggestions);
        rvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        suggestionsAdapter = new SuggestionsAdapter(getActivity(), suggestions, this);
        rvSuggestions.setAdapter(suggestionsAdapter);

        return rootView;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    void run(String data) throws IOException {
        amount.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        RequestBody body = RequestBody.create(JSON, data);
        Log.i("<>-", "<>- " + data);
        String url = "http://bookingtrips-env.us-west-2.elasticbeanstalk.com/booking/findsolutions";
        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("<>-", "<>- " + e.getMessage());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String temp = response.body().string();
                try {
                    final Trip[] returnedTrips = new Gson().fromJson(temp, Trip[].class);
                    if (getActivity() == null) {
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            trip.getCities().clear();
                            int i = 0;
                            for (City city : returnedTrips[0].getCities()) {
                                city.setOrder(i++);
                            }
                            trip.getCities().addAll(returnedTrips[0].getCities());
                            amount.setText(String.format("$ %.2f", trip.getTotalCost()));
                            suggestions.clear();
                            suggestions.addAll(Arrays.asList(returnedTrips));
                            amount.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                            suggestionsAdapter.notifyDataSetChanged();
                        }
                    });
                } catch (Exception ex) {
                }
            }
        });
    }

    @Override
    public void onSelectedOption(Trip trip) {
        this.trip.getCities().clear();
        int i = 0;
        for (City city : trip.getCities()) {
            city.setOrder(i++);
        }
        this.trip.getCities().addAll(trip.getCities());
        amount.setText(String.format("$ %.2f", this.trip.getTotalCost()));
        amount.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
        suggestionsAdapter.notifyDataSetChanged();
        slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }
}
