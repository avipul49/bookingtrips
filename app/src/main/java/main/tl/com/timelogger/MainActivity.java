package main.tl.com.timelogger;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.firebase.client.Firebase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import main.tl.com.timelogger.authentication.LoginActivity;
import main.tl.com.timelogger.model.User;
import main.tl.com.timelogger.new_entry.NewEntryActivity;
import main.tl.com.timelogger.report.ReportFragment;
import main.tl.com.timelogger.users.UserListFragment;
import main.tl.com.timelogger.util.ImageLoaderUtil;
import main.tl.com.timelogger.util.LocalStorage;
import main.tl.com.timelogger.view.SublimePickerFragment;
import main.tl.com.timelogger.view.TextViewWithDrawableClick;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, UserListFragment.OnUserListActionListener {
    private Firebase firebaseRoot;
    private DrawerLayout drawer;
    private TimeListFragment timeListFragment;
    private ReportFragment reportFragment;

    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private TextViewWithDrawableClick filterStatus;
    private int currentSelectedItem = R.id.list;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private User displayedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (User.getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        firebaseRoot = Application.app.getFirebaseRoot();


        filterStatus = (TextViewWithDrawableClick) findViewById(R.id.filter_dates);
        filterStatus.setDrawableClickListener(new TextViewWithDrawableClick.DrawableClickListener() {
            @Override
            public void onClick(DrawablePosition target) {
                removeFilterStatus();
            }
        });

        setupDrawer();

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewEntryActivity();
            }
        });

        setupInitialFragment(savedInstanceState);
        displayedUser = User.getCurrentUser();
    }

    private void removeFilterStatus() {
        if (getFragmentManager().getBackStackEntryCount() == 1) {
            reportFragment.filter(null, null);
        } else {
            timeListFragment.filter(null, null);
        }
        filterStatus.setVisibility(View.GONE);
    }

    private void setupInitialFragment(Bundle savedInstanceState) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (savedInstanceState == null) {
            timeListFragment = TimeListFragment.newInstance(User.getCurrentUser().getUid());
            fragmentTransaction.add(R.id.fragment_container, timeListFragment, "timeListFragment");
            fragmentTransaction.commit();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int fragmentCount = getFragmentManager().getBackStackEntryCount();
                if (fragmentCount == 0) {
                    currentSelectedItem = R.id.list;
                    navigationView.setCheckedItem(R.id.list);
                    getSupportActionBar().setTitle(getString(R.string.time_log));
                    displayedUser = User.getCurrentUser();
                } else {
                    if (getFragmentManager().getBackStackEntryAt(fragmentCount - 1).getName().equals("users")) {
                        getSupportActionBar().setTitle(getString(R.string.users));
                        displayedUser = User.getCurrentUser();
                    }
                }
            }
        });
    }

    private void setupDrawer() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderEmail = (TextView) headerView.findViewById(R.id.email);
        ImageView userImage = (ImageView) headerView.findViewById(R.id.imageView);
        TextView navHeaderName = (TextView) headerView.findViewById(R.id.name);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navHeaderEmail.setText(User.getCurrentUser().getEmail());
        navHeaderName.setText(User.getCurrentUser().getName());
        ImageLoaderUtil.displayImage(this, User.getCurrentUser().getImageURL(), userImage);
        navigationView.setNavigationItemSelectedListener(this);
        if (User.getCurrentUser().isAdmin() || User.getCurrentUser().isManager()) {
            navigationView.inflateMenu(R.menu.manager_activity_main_drawer);
        } else {
            navigationView.inflateMenu(R.menu.activity_main_drawer);
        }
        navigationView.setCheckedItem(R.id.list);
    }

    private void showNewEntryActivity() {
        Intent intent = new Intent(this, NewEntryActivity.class);
        intent.putExtra("userId", User.getCurrentUser().getUid());
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        displayedUser = User.getCurrentUser();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        int id = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        filterStatus.setVisibility(View.GONE);
        if (id == currentSelectedItem) {
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        switch (id) {
            case R.id.users:
                getFragmentManager().popBackStack();
                fragmentTransaction.add(R.id.fragment_container, UserListFragment.newInstance(), "users");
                fragmentTransaction.addToBackStack("users");
                fragmentTransaction.commit();
                break;
            case R.id.list:
                getFragmentManager().popBackStack();
                break;
            case R.id.report:
                getFragmentManager().popBackStack();
                reportFragment = ReportFragment.newInstance(User.getCurrentUser().getUid());
                fragmentTransaction.add(R.id.fragment_container, reportFragment, "report");
                fragmentTransaction.addToBackStack("report");
                fragmentTransaction.commit();
                break;
            case R.id.new_entry:
                showNewEntryActivity();
                break;
            case R.id.logout:
                logout();
                break;
            case R.id.filter:
                if (currentSelectedItem != R.id.users) {
                    showDateRangePicker();
                    drawer.closeDrawer(GravityCompat.START);
                    return false;
                }
                break;
        }
        currentSelectedItem = id;

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        LocalStorage.removeKey(this, getString(R.string.token));
        firebaseRoot.unauth();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showDateRangePicker() {
        SublimePickerFragment pickerFrag = new SublimePickerFragment();
        pickerFrag.setCallback(new SublimePickerFragment.Callback() {
            @Override
            public void onCancelled() {

            }

            @Override
            public void onDateTimeRecurrenceSet(SelectedDate selectedDate, int hourOfDay, int minute, SublimeRecurrencePicker.RecurrenceOption recurrenceOption, String recurrenceRule) {
                filterStatus.setText(dateFormat.format(selectedDate.getFirstDate().getTime()) + " to " + dateFormat.format(selectedDate.getSecondDate().getTime()));
                if (getFragmentManager().getBackStackEntryCount() == 1) {
                    reportFragment.filter(selectedDate.getFirstDate().getTime(), selectedDate.getSecondDate().getTime());
                } else {
                    timeListFragment.filter(selectedDate.getFirstDate().getTime(), selectedDate.getSecondDate().getTime());
                }
                filterStatus.setVisibility(View.VISIBLE);
            }
        });

        SublimeOptions options = new SublimeOptions();
        options.setPickerToShow(SublimeOptions.Picker.DATE_PICKER);
        options.setDisplayOptions(SublimeOptions.ACTIVATE_DATE_PICKER);
        options.setCanPickDateRange(true);
        Bundle bundle = new Bundle();
        bundle.putParcelable("SUBLIME_OPTIONS", options);
        pickerFrag.setArguments(bundle);
        pickerFrag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        pickerFrag.show(getSupportFragmentManager(), "SUBLIME_PICKER");
    }

    @Override
    public void onSelectUser(User item) {
        displayedUser = item;
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        TimeListFragment timeListFragment = TimeListFragment.newInstance(item.getUid(), item.getName());
        fragmentTransaction.add(R.id.fragment_container, timeListFragment, "usertimeListFragment");
        fragmentTransaction.addToBackStack("userTimeEntry");
        fragmentTransaction.commit();
    }

    @Override
    public void deleteUser(User item) {
        firebaseRoot.child("users").child(item.getKey()).removeValue();
    }
}
