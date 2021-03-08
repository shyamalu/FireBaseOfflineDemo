package org.chimple.myapplication;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.chimple.myapplication.database.AppDatabase;
import org.chimple.myapplication.database.AppExecutors;
import org.chimple.myapplication.database.DbOperations;
import org.chimple.myapplication.database.FirebaseOperations;
import org.chimple.myapplication.model.School;
import org.chimple.myapplication.model.Section;
import org.chimple.myapplication.model.Student;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    private static MainActivity activityRef = null;

    private AppDatabase mDb;
    private FirebaseOperations firebaseOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.activityRef = this;

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