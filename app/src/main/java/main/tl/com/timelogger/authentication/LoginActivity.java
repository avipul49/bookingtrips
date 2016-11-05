package main.tl.com.timelogger.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import main.tl.com.timelogger.Application;
import main.tl.com.timelogger.R;
import main.tl.com.timelogger.model.User;
import main.tl.com.timelogger.util.LocalStorage;

public class LoginActivity extends BaseActivity {

    private EditText mEmailView;
    private EditText mPasswordView;
    private Firebase firebaseRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseRoot = Application.app.getFirebaseRoot();

        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        findViewById(R.id.email_sign_in_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        findViewById(R.id.email_sign_up_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        String token = LocalStorage.getString(this, getString(R.string.token));
        if (token != null) {
            autoLogin(token);
        }
    }

    private void autoLogin(String token) {
        showProgress(true);
        firebaseRoot.authWithCustomToken(token, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                fetchUserDetails(authData);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                showProgress(false);
            }
        });
    }

    private void attemptLogin() {
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            login(email, password);
        }
    }

    private void login(String email, String password) {
        showProgress(true);
        firebaseRoot.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(final AuthData authData) {
                fetchUserDetails(authData);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                showProgress(false);
                Toast.makeText(LoginActivity.this, firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserDetails(final AuthData authData) {
        LocalStorage.saveString(this, getString(R.string.token), authData.getToken());
        firebaseRoot.child("users").child(authData.getUid()).child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showProgress(false);
                setCurrentUser(dataSnapshot);
                gotoMainActivity(authData);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void setCurrentUser(DataSnapshot dataSnapshot) {
        User user = new User();
        user.setEmail((String) dataSnapshot.child("email").getValue());
        user.setImageURL((String) dataSnapshot.child("imageURL").getValue());
        user.setName((String) dataSnapshot.child("name").getValue());
        user.setUid((String) dataSnapshot.child("uid").getValue());
        User.setCurrentUser(user);
    }

}

