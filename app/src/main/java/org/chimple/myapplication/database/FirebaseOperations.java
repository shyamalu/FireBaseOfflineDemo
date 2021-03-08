package org.chimple.myapplication.database;

import android.util.Log;

import androidx.annotation.Nullable;

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
import java.util.Map;

public class FirebaseOperations {
    private static final String TAG = FirebaseOperations.class.getSimpleName();
    private FirebaseFirestore db = null;
    private DocumentReference schoolRef = null;
    private CollectionReference sectionCollectionRef = null;
    private CollectionReference studentCollectionRef = null;
    private static FirebaseOperations sInstance;
    public static String SCHOOL_COLLECTION = "School";
    public static String SECTION_COLLECTION = "Section";
    public static String STUDENT_COLLECTION = "Student";
    private static final long DEFAULT_CACHE_SIZE_BYTES = 100 * 1024 * 1024; // 100 MB
    private static final Object LOCK = new Object();
    private static final String CACHE = "CACHE";
    private static final String SERVER = "SERVER";
    private static FirebaseOperations ref = null;
    private DbOperations operations;
    private ListenerRegistration schoolListener;
    private ListenerRegistration sectionListener;
    private Map<String, ListenerRegistration> studentListeners = new HashMap<String, ListenerRegistration>();

    public static String SCHOOL_ID = "000Test";

    private FirebaseOperations(DbOperations operations) {
        this.ref = this;
        this.operations = operations;
        this.setup();
    }

    private void setup() {
        this.db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(DEFAULT_CACHE_SIZE_BYTES)
                .build();
        db.setFirestoreSettings(settings);

        this.schoolListener = this.initSchoolSync(SCHOOL_ID);
    }

    public static FirebaseOperations getInitializedInstance() {
        return sInstance;
    }

    public static FirebaseOperations getInstance(DbOperations operations) {
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(TAG, "Creating new database instance");
                sInstance = new FirebaseOperations(operations);
            }
        }
        return sInstance;
    }

    private ListenerRegistration initSchoolSync(String schoolId) {

        if (FirebaseOperations.ref.sectionListener == null) {
            FirebaseOperations.ref.sectionListener = FirebaseOperations.ref.initSectionSync(schoolId);
        }

        operations.initFirebaseSyncForAllCachedStudents(schoolId);

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

                        if (source.equalsIgnoreCase(SERVER)) {
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                Log.d(TAG, source + " Current data: " + documentSnapshot.getData());
                                School school = documentSnapshot.toObject(School.class);
                                school.setFirebaseId(documentSnapshot.getId());
                                Log.d(TAG, "school" + school.getName());

                                if (FirebaseOperations.ref.sectionListener == null) {
                                    FirebaseOperations.ref.sectionListener = FirebaseOperations.ref.initSectionSync(schoolId);
                                }

                                if (source.equalsIgnoreCase(SERVER)) {
                                    operations.upsertSchool(school);
                                }
                            } else {
                                Log.d(TAG, source + " Current data: null");
                                if (FirebaseOperations.ref.sectionListener != null) {
                                    FirebaseOperations.ref.sectionListener.remove();
                                    FirebaseOperations.ref.sectionListener = null;
                                }
                                if (source.equalsIgnoreCase(SERVER)) {
                                    operations.deleteSchoolById(SCHOOL_ID);
                                }
                            }
                        }
                    }
                }
        );

        operations.loadAllSchools(schoolId);
        operations.loadAllSectionsWithStudents(schoolId);

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
                                    FirebaseOperations.ref.createSection(schoolId, dc.getDocument());
                                    break;
                                case MODIFIED:
                                    Log.d(TAG, "Modified Section: " + dc.getDocument().getData());
                                    FirebaseOperations.ref.createSection(schoolId, dc.getDocument());
                                    break;
                                case REMOVED:
                                    Log.d(TAG, "Removed Section: " + dc.getDocument().getData());
                                    FirebaseOperations.ref.removeSection(schoolId, dc.getDocument());
                                    break;
                            }
                        }

                        if (queryDocumentSnapshots != null) {
                            for (QueryDocumentSnapshot snapshots : queryDocumentSnapshots) {
                                String source = snapshots.getMetadata().isFromCache() ?
                                        CACHE : SERVER;
                                Log.d(TAG, "Received Sections using :" + source);
                            }
                        }
                    }
                }
        );
        return sectionListener;
    }

    private void removeSection(String schoolId, QueryDocumentSnapshot snapshot) {
        Section section = snapshot.toObject(Section.class);
        section.setFirebaseId(snapshot.getId());
        FirebaseOperations.ref.removeStudentListener(schoolId, section.getFirebaseId());
        FirebaseOperations.ref.operations.deleteSectionById(section.getFirebaseId());
    }

    private Section createSection(String schoolId, QueryDocumentSnapshot snapshot) {
        Section section = snapshot.toObject(Section.class);
        section.setFirebaseId(snapshot.getId());
        section.setSchoolId(schoolId);
        FirebaseOperations.ref.addStudentListener(schoolId, section.getFirebaseId());
        FirebaseOperations.ref.operations.upsertSection(section);
        Log.d(TAG, "created/updated section:" + section.getFirebaseId());
        return section;
    }

    public void addStudentListener(String schoolId, String sectionId) {
        final String sKey = schoolId + "/" + sectionId;
        ListenerRegistration listenerRegistration = FirebaseOperations.ref.studentListeners.get(sKey);
        if (listenerRegistration == null) {
            Log.d(TAG, "Adding Student Listener for: " + schoolId + " " + sectionId);
            ListenerRegistration s = FirebaseOperations.ref.initStudentSync(schoolId, sectionId);
            FirebaseOperations.ref.studentListeners.put(sKey, s);
        }
    }

    public void removeStudentListener(String schoolId, String sectionId) {
        final String sKey = schoolId + "/" + sectionId;
        ListenerRegistration listenerRegistration = FirebaseOperations.ref.studentListeners.get(sKey);
        if (listenerRegistration != null) {
            Log.d(TAG, "Remove Student Listener for: " + schoolId + " " + sectionId);
            FirebaseOperations.ref.studentListeners.remove(sKey);
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
                                    FirebaseOperations.ref.createStudent(schoolId, sectionId, dc.getDocument());
                                    break;
                                case MODIFIED:
                                    Log.d(TAG, "Modified Student: " + dc.getDocument().getData());
                                    FirebaseOperations.ref.createStudent(schoolId, sectionId, dc.getDocument());
                                    break;
                                case REMOVED:
                                    FirebaseOperations.ref.operations.deleteStudentById(dc.getDocument().getId());
                                    Log.d(TAG, "Removed Student: " + dc.getDocument().getData());
                                    break;
                            }
                        }

                        if (queryDocumentSnapshots != null) {
                            for (QueryDocumentSnapshot snapshots : queryDocumentSnapshots) {
                                String source = snapshots.getMetadata().isFromCache() ?
                                        CACHE : SERVER;
                                Log.d(TAG, "Received student using :" + source);
                            }
                        }
                    }
                }
        );
        return studentListener;
    }

    private Student createStudent(String schoolId, String sectionId, QueryDocumentSnapshot s) {
        Student student = s.toObject(Student.class);
        student.setFirebaseId(s.getId());
        student.setSchoolId(schoolId);
        student.setSectionId(sectionId);
        FirebaseOperations.ref.operations.upsertStudent(student);
        Log.d(TAG, "created student: " + student.getFirebaseId());
        return student;
    }

}
