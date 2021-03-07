package org.chimple.myapplication;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import org.chimple.myapplication.model.School;
import org.chimple.myapplication.model.Section;
import org.chimple.myapplication.model.Student;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db = null;
    private DocumentReference schoolRef = null;
    private CollectionReference sectionCollectionRef = null;
    private CollectionReference studentCollectionRef = null;

    public static String SCHOOL_COLLECTION = "School";
    public static String SECTION_COLLECTION = "Section";
    public static String STUDENT_COLLECTION = "Student";
    private static final long DEFAULT_CACHE_SIZE_BYTES = 100 * 1024 * 1024; // 100 MB

    private static final String CACHE = "CACHE";
    private static final String SERVER = "SERVER";

    private String TAG = MainActivity.class.getSimpleName();

    private ListenerRegistration schoolListener;
    private ListenerRegistration sectionListener;
    private Map<String, ListenerRegistration> studentListeners = new HashMap<String, ListenerRegistration>();

    private Map<String, Section> sections = new HashMap<String, Section>();
    private Map<String, Student> students = new HashMap<String, Student>();
    private Map<String, Map<String, Student>> sectionStudents = new HashMap<String, Map<String, Student>>();
    private static MainActivity activityRef = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.activityRef = this;
        this.setup();
        MainActivity.activityRef.schoolListener = this.initSchoolSync("000Test");
    }

    public void setup() {
        this.db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(DEFAULT_CACHE_SIZE_BYTES)
                .build();
        db.setFirestoreSettings(settings);
    }

    private ListenerRegistration initSchoolSync(String schoolId) {
        this.schoolRef = db.collection(SCHOOL_COLLECTION)
                .document(schoolId);

        this.schoolListener = this.schoolRef.addSnapshotListener(
                new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                        @Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        String source = documentSnapshot.getMetadata().isFromCache() ?
                                CACHE : SERVER;

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            Log.d(TAG, source + " Current data: " + documentSnapshot.getData());
                            School school = documentSnapshot.toObject(School.class);
                            Log.d(TAG, "school" + school.getName());

                            if(MainActivity.activityRef.sectionListener == null) {
                                MainActivity.activityRef.sectionListener = MainActivity.activityRef.initSectionSync(schoolId);
                            }
                        } else {
                            Log.d(TAG, source + " Current data: null");
                            if (MainActivity.activityRef.sectionListener != null) {
                                MainActivity.activityRef.sectionListener.remove();
                                MainActivity.activityRef.sectionListener = null;
                            }
                        }
                    }
                }
        );

        return schoolListener;
    }

    private ListenerRegistration initSectionSync(String schoolId) {
        this.sectionCollectionRef = db.collection(SCHOOL_COLLECTION + "/" + schoolId + "/" + SECTION_COLLECTION);

        ListenerRegistration sectionListener = this.sectionCollectionRef.addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Log.d(TAG, "New Section: " + dc.getDocument().getData());
                                    MainActivity.activityRef.createSection(schoolId, dc.getDocument());
                                    break;
                                case MODIFIED:
                                    Log.d(TAG, "Modified Section: " + dc.getDocument().getData());
                                    MainActivity.activityRef.createSection(schoolId, dc.getDocument());
                                    break;
                                case REMOVED:
                                    Log.d(TAG, "Removed Section: " + dc.getDocument().getData());
                                    MainActivity.activityRef.removeSection(schoolId, dc.getDocument());
                                    break;
                            }
                        }

                        if (queryDocumentSnapshots != null) {
                            for (QueryDocumentSnapshot snapshots : queryDocumentSnapshots) {
                                String source = snapshots.getMetadata().isFromCache() ?
                                        CACHE : SERVER;
                                Log.d(TAG, "Received Sections using :" + source);
                                for (String id : MainActivity.activityRef.sections.keySet()) {
                                    Section s = MainActivity.activityRef.sections.get(id);
                                    Log.d(TAG, "found section: " + id + " " + s);
                                }

                            }
                        }
                    }
                }
        );
        return sectionListener;
    }

    private void removeSchool(String schoolId) {

    }

    private void removeSection(String schoolId, QueryDocumentSnapshot snapshot) {
        Section section = snapshot.toObject(Section.class);
        section.setId(snapshot.getId());
        if (MainActivity.activityRef.sections.get(section.getId()) != null) {
            MainActivity.activityRef.sections.remove(section);

            MainActivity.activityRef.removeStudentListener(schoolId, section.getId());
            if (MainActivity.activityRef.sectionStudents != null) {
                Map students = MainActivity.activityRef.sectionStudents.get(section.getId());
                if (students != null) {
                    students.clear();
                    Log.d(TAG, "clear all students for section:" + section.getId());
                }

                MainActivity.activityRef.sectionStudents.remove(section.getId());
                Log.d(TAG, "clear section students for section:" + section.getId());
            }
        }
    }

    private Section createSection(String schoolId, QueryDocumentSnapshot snapshot) {
        Section section = snapshot.toObject(Section.class);
        section.setId(snapshot.getId());

        MainActivity.activityRef.sections.put(section.getId(), section);
        Log.d(TAG, "created/updated section:" + section.getId());

        MainActivity.activityRef.addStudentListener(schoolId, section.getId());
        return section;
    }

    public void addStudentListener(String schoolId, String sectionId) {
        final String sKey = schoolId + "/" + sectionId;
        ListenerRegistration listenerRegistration = MainActivity.activityRef.studentListeners.get(sKey);
        if (listenerRegistration == null) {
            Log.d(TAG, "Adding Student Listener for: " + schoolId + " " + sectionId);
            ListenerRegistration s = MainActivity.activityRef.initStudentSync(schoolId, sectionId);
            MainActivity.activityRef.studentListeners.put(sKey, s);
        }
    }

    public void removeStudentListener(String schoolId, String sectionId) {
        final String sKey = schoolId + "/" + sectionId;
        ListenerRegistration listenerRegistration = MainActivity.activityRef.studentListeners.get(sKey);
        if (listenerRegistration != null) {
            Log.d(TAG, "Remove Student Listener for: " + schoolId + " " + sectionId);
            MainActivity.activityRef.studentListeners.remove(sKey);
        }
    }

    public ListenerRegistration initStudentSync(String schoolId, String sectionId) {
        this.studentCollectionRef = db.collection(SCHOOL_COLLECTION + "/" + schoolId + "/" + SECTION_COLLECTION + "/" + sectionId + "/" + STUDENT_COLLECTION);

        ListenerRegistration studentListener = this.studentCollectionRef.addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Log.d(TAG, "New Student: " + dc.getDocument().getData());
                                    MainActivity.activityRef.createStudent(sectionId, dc.getDocument());
                                    break;
                                case MODIFIED:
                                    Log.d(TAG, "Modified Student: " + dc.getDocument().getData());
                                    MainActivity.activityRef.createStudent(sectionId, dc.getDocument());
                                    break;
                                case REMOVED:
                                    MainActivity.activityRef.students.remove(dc.getDocument().getId());
                                    MainActivity.activityRef.removeStudentListener(schoolId, sectionId);
                                    Log.d(TAG, "Removed Student: " + dc.getDocument().getData());
                                    break;
                            }
                        }

                        if (queryDocumentSnapshots != null) {
                            for (QueryDocumentSnapshot snapshots : queryDocumentSnapshots) {
                                String source = snapshots.getMetadata().isFromCache() ?
                                        CACHE : SERVER;
                                Log.d(TAG, "Received student using :" + source);
                                for (String id : MainActivity.activityRef.students.keySet()) {
                                    Student s = MainActivity.activityRef.students.get(id);
                                    Log.d(TAG, "found student: " + id + " " + s);
                                }
                            }
                        }
                    }
                }
        );
        return studentListener;
    }

    private Student createStudent(String sectionId, QueryDocumentSnapshot s) {
        Student student = s.toObject(Student.class);
        student.setId(s.getId());
        MainActivity.activityRef.students.put(student.getId(), student);
        MainActivity.activityRef.sectionStudents.put(sectionId, MainActivity.activityRef.students);
        Log.d(TAG, "created student: " + student.getId());
        return student;
    }


    public void doOfflineActivities() {
        db.disableNetwork()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "doOfflineActivities");
                    }
                });

    }

    public void doOnlineActivities() {
        db.enableNetwork()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "doOnlineActivities");
                    }
                });
    }
}