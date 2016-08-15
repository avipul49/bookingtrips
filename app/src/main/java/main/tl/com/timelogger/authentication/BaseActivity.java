package main.tl.com.timelogger.authentication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.firebase.client.AuthData;

import main.tl.com.timelogger.MainActivity;

/**
 * Created by vipulmittal on 30/06/16.
 */
public class BaseActivity extends AppCompatActivity {
    protected View mProgressView;
    protected View mLoginFormView;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    protected void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    protected boolean isEmailValid(String email) {
        return email.contains("@");
    }

    protected boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    protected void gotoMainActivity(AuthData authData) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("user_id", authData.getUid());
        startActivity(intent);
        finish();
    }
}
