package main.tl.com.timelogger.authentication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Map;

import main.tl.com.timelogger.Application;
import main.tl.com.timelogger.R;
import main.tl.com.timelogger.model.User;
import main.tl.com.timelogger.util.LocalStorage;

public class SignUpActivity extends BaseActivity {

    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mNameView;
    private Firebase firebaseRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        firebaseRoot = Application.app.getFirebaseRoot();

        mEmailView = (EditText) findViewById(R.id.email);
        mNameView = (EditText) findViewById(R.id.name);
        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptSignUp();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignUp();
            }
        });


    }

    private void attemptSignUp() {
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mNameView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String name = mNameView.getText().toString();
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(email)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            createUser(email, password, name);
        }
    }

    private void createUser(final String email, final String password, final String name) {
        showProgress(true);
        firebaseRoot.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                String uid = (String) result.get("uid");
                setUserProfile(uid, email, name, password);
            }

            @Override
            public void onError(FirebaseError firebaseError) {

            }
        });
    }

    private void setUserProfile(String uid, String email, final String name, String password) {
        final Firebase userNode = firebaseRoot.child("users").child(uid);
        userNode.child("profile").child("email").setValue(email);
        userNode.child("profile").child("name").setValue(name);
        userNode.child("profile").child("uid").setValue(uid);
        userNode.child("profile").child("isManager").setValue(false);
        userNode.child("profile").child("isAdmin").setValue(false);

        firebaseRoot.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                LocalStorage.saveString(SignUpActivity.this, getString(R.string.token), authData.getToken());
                showProgress(false);
                setCurrentUser(authData, name);
                userNode.child("profile").child("imageURL").setValue((String) authData.getProviderData().get("profileImageURL"));
                gotoMainActivity(authData);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {

            }
        });
    }

    private void setCurrentUser(AuthData authData, String name) {
        User user = new User();
        user.setName(name);
        user.setUid(authData.getUid());
        user.setEmail((String) authData.getProviderData().get("email"));
        user.setImageURL((String) authData.getProviderData().get("profileImageURL"));
        User.setCurrentUser(user);
    }


}

