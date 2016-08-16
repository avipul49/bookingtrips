package main.tl.com.timelogger.trip;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import main.tl.com.timelogger.Application;
import main.tl.com.timelogger.R;
import main.tl.com.timelogger.model.Message;
import main.tl.com.timelogger.model.Trip;
import main.tl.com.timelogger.model.User;
import main.tl.com.timelogger.util.LocalStorage;
import main.tl.com.timelogger.view.CustomEditText;

public class UserListFragment extends Fragment implements CustomEditText.OnActionListener {
    private Firebase firebaseRoot;
    private List<User> userList = new ArrayList<>();
    private UserAdapter adapter;
    Trip trip;
    private CustomEditText addUser;

    public UserListFragment() {
    }

    public static UserListFragment newInstance(Trip trip) {
        UserListFragment fragment = new UserListFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        addUser = (CustomEditText) view.findViewById(R.id.add);
        addUser.setOnActionListener(this);
        firebaseRoot = Application.app.getFirebaseRoot();
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        adapter = new UserAdapter(userList, (OnUserListActionListener) getActivity());
        recyclerView.setAdapter(adapter);
        firebaseRoot.child("trips").child(trip.getKey()).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                updateTimeList(snapshot);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

        return view;
    }

    private void updateTimeList(DataSnapshot snapshot) {
        userList.clear();
        for (DataSnapshot ds : snapshot.getChildren()) {
            User temp = ds.getValue(User.class);
//            temp.setManager(ds.child("profile").hasChild("isManager") && (boolean) ds.child("profile").child("isManager").getValue());
//            temp.setAdmin(ds.child("profile").hasChild("isAdmin") && (boolean) ds.child("profile").child("isAdmin").getValue());
            temp.setKey(ds.getKey());
            userList.add(temp);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAction() {
        if (!addUser.getText().toString().isEmpty()) {

            firebaseRoot.child("user_index").child(LocalStorage.escapeEmail(addUser.getText().toString())).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        final String userId = dataSnapshot.getValue(String.class);
                        firebaseRoot.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    User user = dataSnapshot.child("profile").getValue(User.class);
                                    firebaseRoot.child("trips").child(trip.getKey()).child("members").child(userId).setValue(user);
                                    firebaseRoot.child("users").child(userId).child("trips").child(trip.getKey()).setValue(trip);
                                    addUser.setText("");
                                    Message message = new Message();
                                    message.setMessage(user.getName() + " was added.");
                                    message.setDate(System.currentTimeMillis());
                                    message.setType("ACTION");
                                    message.setUserId(User.getCurrentUser().getUid());
                                    message.setUserName(User.getCurrentUser().getName());
                                    firebaseRoot.child("trips").child(trip.getKey()).child("discussion").push().setValue(message, new Firebase.CompletionListener() {
                                        @Override
                                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {

                                        }
                                    });

                                    userList.add(user);
                                    adapter.notifyDataSetChanged();

                                } else {
                                    Toast.makeText(getContext(), "User does not exist.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "User does not exist.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    public interface OnUserListActionListener {
        void onSelectUser(User item);

        void deleteUser(User item);
    }

}
