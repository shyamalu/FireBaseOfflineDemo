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

import org.chimple.myapplication.MainActivity;
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
    private Map<String, Section> sections = new HashMap<String, Section>();
    private Map<String, Student> students = new HashMap<String, Student>();
    private Map<String, Map<String, Student>> sectionStudents = new HashMap<String, Map<String, Student>>();

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

        this.schoolListener = this.initSchoolSync("000Test");
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

        operations.initFirebaseSyncForAllCachedStudents();

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
                                    operations.deleteSchoolById("000Test");
                                }
                            }
                        } else {
                            operations.loadAllSchools();
                            operations.loadAllSections();
                            operations.loadAllStudents();
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
                                for (String id : FirebaseOperations.ref.sections.keySet()) {
                                    Section s = FirebaseOperations.ref.sections.get(id);
                                    Log.d(TAG, "found section: " + id + " " + s);
                                }

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
        if (FirebaseOperations.ref.sections.get(section.getFirebaseId()) != null) {
            FirebaseOperations.ref.sections.remove(section);
            FirebaseOperations.ref.operations.deleteSectionById(section.getFirebaseId());

            FirebaseOperations.ref.removeStudentListener(schoolId, section.getFirebaseId());
            if (FirebaseOperations.ref.sectionStudents != null) {
                Map students = FirebaseOperations.ref.sectionStudents.get(section.getFirebaseId());
                if (students != null) {
                    students.clear();
                    Log.d(TAG, "clear all students for section:" + section.getFirebaseId());
                }

                FirebaseOperations.ref.sectionStudents.remove(section.getFirebaseId());
                Log.d(TAG, "clear section students for section:" + section.getFirebaseId());
            }
        }
    }

    private Section createSection(String schoolId, QueryDocumentSnapshot snapshot) {
        Section section = snapshot.toObject(Section.class);
        section.setFirebaseId(snapshot.getId());

        FirebaseOperations.ref.sections.put(section.getFirebaseId(), section);
        Log.d(TAG, "created/updated section:" + section.getFirebaseId());

        FirebaseOperations.ref.addStudentListener(schoolId, section.getFirebaseId());
        FirebaseOperations.ref.operations.upsertSection(section);
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
                                    FirebaseOperations.ref.createStudent(sectionId, dc.getDocument());
                                    break;
                                case MODIFIED:
                                    Log.d(TAG, "Modified Student: " + dc.getDocument().getData());
                                    FirebaseOperations.ref.createStudent(sectionId, dc.getDocument());
                                    break;
                                case REMOVED:
                                    FirebaseOperations.ref.students.remove(dc.getDocument().getId());
                                    FirebaseOperations.ref.operations.deleteStudentById(dc.getDocument().getId());
                                    FirebaseOperations.ref.removeStudentListener(schoolId, sectionId);
                                    Log.d(TAG, "Removed Student: " + dc.getDocument().getData());
                                    break;
                            }
                        }

                        if (queryDocumentSnapshots != null) {
                            for (QueryDocumentSnapshot snapshots : queryDocumentSnapshots) {
                                String source = snapshots.getMetadata().isFromCache() ?
                                        CACHE : SERVER;
                                Log.d(TAG, "Received student using :" + source);
                                for (String id : FirebaseOperations.ref.students.keySet()) {
                                    Student s = FirebaseOperations.ref.students.get(id);
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
        student.setFirebaseId(s.getId());
        FirebaseOperations.ref.students.put(student.getFirebaseId(), student);
        FirebaseOperations.ref.operations.upsertStudent(student);
        FirebaseOperations.ref.sectionStudents.put(sectionId, FirebaseOperations.ref.students);
        Log.d(TAG, "created student: " + student.getFirebaseId());
        return student;
    }

}
