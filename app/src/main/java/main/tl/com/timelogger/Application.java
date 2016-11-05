package main.tl.com.timelogger;

import com.firebase.client.Firebase;

public class Application extends android.app.Application {
    private Firebase firebaseRoot;
    public static Application app;

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        firebaseRoot = new Firebase(getString(R.string.firebase_root));
        app = this;
    }

    public Firebase getFirebaseRoot() {
        return firebaseRoot;
    }
}
