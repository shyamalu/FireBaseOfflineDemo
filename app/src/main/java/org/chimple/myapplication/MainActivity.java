package org.chimple.myapplication;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.chimple.myapplication.database.AppDatabase;
import org.chimple.myapplication.database.DbOperations;
import org.chimple.myapplication.database.FirebaseOperations;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    private AppDatabase mDb;
    private FirebaseOperations firebaseOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDb = AppDatabase.getInstance(getApplicationContext());
        firebaseOperations = FirebaseOperations.getInstance(DbOperations.getInstance(mDb));
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