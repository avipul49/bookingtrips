package main.tl.com.timelogger.trip;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import main.tl.com.timelogger.Application;
import main.tl.com.timelogger.R;
import main.tl.com.timelogger.authentication.LoginActivity;
import main.tl.com.timelogger.model.City;
import main.tl.com.timelogger.model.Message;
import main.tl.com.timelogger.model.Trip;
import main.tl.com.timelogger.model.User;
import main.tl.com.timelogger.view.CustomEditText;

public class TripDetails extends AppCompatActivity implements UserListFragment.OnUserListActionListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private Trip trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        if (User.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
        }
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(mViewPager);
        tabs.setIndicatorColor(getResources().getColor(R.color.colorPrimary));

        trip = new Gson().fromJson(getIntent().getStringExtra("trip"), Trip.class);

        setTitle(trip.getName());
//        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//        startActivityForResult(contactPickerIntent, 1000);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_trip_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.book) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSelectUser(User item) {

    }

    @Override
    public void deleteUser(User item) {
        Application.app.getFirebaseRoot().child("trips").child(trip.getKey()).child("members").child(item.getUid()).removeValue();
        Application.app.getFirebaseRoot().child("users").child(item.getKey()).child("trips").child(trip.getKey()).removeValue();
        finish();
    }

    public interface OnPlacesActionListener {
        void editPlace(City item);

        void deletePlace(City item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return PlacesFragment.newInstance(trip);
                case 1:
                    return new DiscussionFragment();
                case 2:
                    return UserListFragment.newInstance(trip);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Places";
                case 1:
                    return "Discussion";
                case 2:
                    return "Members";
            }
            return null;
        }
    }

    public static class DiscussionFragment extends Fragment implements CustomEditText.OnActionListener {
        RecyclerView rvMessages;
        String postId;
        ArrayList<Message> messages = new ArrayList<>();
        MessageAdapter adapter;
        private CustomEditText commentView;
        private ValueEventListener action;

        public DiscussionFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (getArguments() != null) {
                postId = getArguments().getString("id");
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_post_conversation, container, false);
            rvMessages = (RecyclerView) rootView.findViewById(R.id.rv_messages);
            commentView = (CustomEditText) rootView.findViewById(R.id.comment);
            commentView.setOnActionListener(this);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            //linearLayoutManager.setReverseLayout(true);
            rvMessages.setLayoutManager(linearLayoutManager);

            adapter = new MessageAdapter(getContext(), messages);
            rvMessages.setAdapter(adapter);

            final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            action = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    messages.clear();
                    long last = -1;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Message message = snapshot.getValue(Message.class);
                        //calendar.setTimeInMillis(message.getStartDate());
                        if (last != message.getDate() / (24 * 60 * 60 * 1000)) {
                            last = message.getDate() / (24 * 60 * 60 * 1000);
                            Message time = new Message();
                            time.setType("ACTION");
                            SimpleDateFormat outgoingFormat = new SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault());
                            time.setMessage(outgoingFormat.format(new Date(message.getDate())));
                            messages.add(time);
                        }
                        messages.add(message);
                    }
                    adapter.notifyDataSetChanged();
                    rvMessages.scrollToPosition(messages.size() - 1);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mDatabase.child("trips").child(((TripDetails) getActivity()).trip.getKey()).child("discussion").addValueEventListener(action);
            return rootView;
        }

        @Override
        public void onAction() {
            if (!commentView.getText().toString().isEmpty()) {
                //progressBar.setVisibility(View.VISIBLE);
                final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                Message message = new Message();
                message.setMessage(commentView.getText().toString());
                message.setDate(System.currentTimeMillis());
                message.setType("TEXT");
                message.setUserId(User.getCurrentUser().getUid());
                message.setUserName(User.getCurrentUser().getName());
                mDatabase.child("trips").child(((TripDetails) getActivity()).trip.getKey()).child("discussion").push().setValue(message);
                commentView.setText("");
            } else {
                Toast.makeText(getContext(), "Please type a message", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
