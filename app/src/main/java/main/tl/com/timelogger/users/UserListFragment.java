package main.tl.com.timelogger.users;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

import main.tl.com.timelogger.Application;
import main.tl.com.timelogger.R;
import main.tl.com.timelogger.model.User;

public class UserListFragment extends android.app.Fragment {
    private Firebase firebaseRoot;
    private List<User> userList = new ArrayList<>();
    private UserAdapter adapter;

    public UserListFragment() {
    }

    public static UserListFragment newInstance() {
        UserListFragment fragment = new UserListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        firebaseRoot = Application.app.getFirebaseRoot();
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            adapter = new UserAdapter(userList, (OnUserListActionListener) getActivity());
            recyclerView.setAdapter(adapter);
            firebaseRoot.child("users").addValueEventListener(new ValueEventListener() {
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.users));

        return view;
    }

    private void updateTimeList(DataSnapshot snapshot) {
        userList.clear();
        for (DataSnapshot ds : snapshot.getChildren()) {
            User temp = ds.child("profile").getValue(User.class);
            temp.setManager(ds.child("profile").hasChild("isManager") && (boolean) ds.child("profile").child("isManager").getValue());
            temp.setAdmin(ds.child("profile").hasChild("isAdmin") && (boolean) ds.child("profile").child("isAdmin").getValue());
            temp.setKey(ds.getKey());
            userList.add(temp);
        }
        adapter.notifyDataSetChanged();
    }

    public interface OnUserListActionListener {
        void onSelectUser(User item);

        void deleteUser(User item);
    }

}
