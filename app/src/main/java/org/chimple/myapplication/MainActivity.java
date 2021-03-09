package org.chimple.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.chimple.myapplication.database.AppDatabase;
import org.chimple.myapplication.database.DbOperations;
import org.chimple.myapplication.database.FirebaseOperations;
import org.chimple.myapplication.database.Helper;

import static org.chimple.myapplication.database.Helper.EMAIL;
import static org.chimple.myapplication.database.Helper.PASSWORD;
import static org.chimple.myapplication.database.Helper.SHARED_PREF;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private FirebaseAuth mAuth = null;
    private AppDatabase mDb = null;
    private FirebaseOperations firebaseOperations = null;
    private static MainActivity activity = null;
    private Helper helper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        helper = Helper.getInstance(this, this.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE));
        mAuth = FirebaseAuth.getInstance();

        login("shyamal@amiti.in", "test1234");
    }

    private void auth() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            reload();
        } else {
            signIn();
        }
    }

    private void reload() {
        mAuth.getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseUser user = task.isSuccessful() ?
                        mAuth.getCurrentUser() : null;

                if (user != null) {
                    activity.init();
                } else {
                    signIn();
                }
            }
        });
    }

    private void signIn() {
        String email = this.helper.getSharedPreferences().getString(EMAIL, "");
        String password = this.helper.getSharedPreferences().getString(PASSWORD, "");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseUser user = task.isSuccessful() ?
                                mAuth.getCurrentUser() : null;
                        if (user != null) {
                            activity.init();
                        }
                    }
                });

    }

    private void init() {
        mDb = AppDatabase.getInstance(getApplicationContext());
        firebaseOperations = FirebaseOperations.getInstance(getApplicationContext(), DbOperations.getInstance(mDb));
    }

    public static void login(String email, String password) {
        activity.helper.getSharedPreferences().edit().putString(EMAIL, email).apply();
        activity.helper.getSharedPreferences().edit().putString(PASSWORD, password).apply();
        activity.auth();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: --------");
        if (mDb != null) {
            if (mDb.isOpen()) {
                mDb.close();
            }
            mDb = null;
        }
    }


}