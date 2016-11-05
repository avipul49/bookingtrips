package main.tl.com.timelogger.trip;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private int CONTACT_PICKER_RESULT = 1000;
    private ProgressDialog progressDialog;

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
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
            }
        });
        progressDialog = new ProgressDialog(getActivity());
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
        String email = addUser.getText().toString();
        if (!email.isEmpty()) {
            progressDialog.show();
            addMember(email, "");
        }
    }

    private void addMember(final String email, final String name) {
        progressDialog.show();
        firebaseRoot.child("user_index").child(LocalStorage.escapeEmail(email)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    final String userId = dataSnapshot.getValue(String.class);
                    firebaseRoot.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                User user = dataSnapshot.child("profile").getValue(User.class);
                                addMemberToTheTrip(user, userId);
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "User does not exist.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            progressDialog.dismiss();
                        }
                    });
                } else {
                    //Toast.makeText(getContext(), "User does not exist.", Toast.LENGTH_SHORT).show();
                    firebaseRoot.createUser(email, "aaaa", new Firebase.ValueResultHandler<Map<String, Object>>() {
                        @Override
                        public void onSuccess(Map<String, Object> result) {
                            final String uid = (String) result.get("uid");
                            final User user = new User();
                            user.setName(name);
                            user.setEmail(email);
                            user.setUid(uid);
                            firebaseRoot.child("user_index").child(LocalStorage.escapeEmail(email)).setValue(uid);
                            firebaseRoot.authWithPassword(email, "aaaa", new Firebase.AuthResultHandler() {
                                @Override
                                public void onAuthenticated(AuthData authData) {
                                    user.setImageURL((String) authData.getProviderData().get("profileImageURL"));
                                    firebaseRoot.child("users").child(uid).child("profile").setValue(user);
                                    addMemberToTheTrip(user, uid);
                                }

                                @Override
                                public void onAuthenticationError(FirebaseError firebaseError) {
                                    progressDialog.dismiss();
                                }
                            });
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            Toast.makeText(getActivity(), firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.i("<><>", firebaseError.toString());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void addMemberToTheTrip(User user, String userId) {
        progressDialog.dismiss();
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
    }

    public interface OnUserListActionListener {
        void onSelectUser(User item);

        void deleteUser(User item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 1000:
                    Cursor cursor = null;
                    String email = "", name = "";
                    try {
                        Uri result = data.getData();
                        Log.v("<>-<>", "Got a contact result: " + result.toString());

                        // get the contact id from the Uri
                        String id = result.getLastPathSegment();

                        // query for everything email
                        cursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?", new String[]{id}, null);

                        int nameId = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

                        int emailIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);

                        // let's just get the first email
                        if (cursor.moveToFirst()) {
                            email = cursor.getString(emailIdx);
                            name = cursor.getString(nameId);
                            Log.v("<>-", "Got email: " + email);
                        } else {
                            Log.w("", "No results");
                        }
                    } catch (Exception e) {
                        Log.e("", "Failed to get email data", e);
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                       // Toast.makeText(getContext(), email + "  " + name, Toast.LENGTH_LONG).show();
                        if (email.length() == 0 && name.length() == 0) {
                            Toast.makeText(getContext(), "No Email for Selected Contact", Toast.LENGTH_LONG).show();
                        } else {
                            addMember(email, name);
                        }
                    }
                    break;
            }

        } else {
            Log.w("", "Warning: activity result not ok");
        }
    }

}
