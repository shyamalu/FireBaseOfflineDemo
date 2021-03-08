package org.chimple.myapplication.database;

import android.util.Log;

import org.chimple.myapplication.model.School;
import org.chimple.myapplication.model.Section;
import org.chimple.myapplication.model.Student;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DbOperations {
    private static final Object LOCK = new Object();

    private DbOperations(AppDatabase db) {
        this.db = db;
    }

    private static final String TAG = DbOperations.class.getSimpleName();
    private static DbOperations sInstance;
    private static AppDatabase db;

    public static DbOperations getInstance(AppDatabase db) {
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(TAG, "Creating new database instance");
                sInstance = new DbOperations(db);
            }
        }
        return sInstance;
    }

    public void upsertSchool(School school) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                db.schoolDao().insertSchool(school);
                School school1 = db.schoolDao().loadSchoolById(school.getFirebaseId());
                Log.d(TAG, "school1: " + school1);
            }
        });
    }

    public void upsertSection(Section section) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                db.sectionDao().insertSection(section);
                Section Section1 = db.sectionDao().loadSectionById(section.getFirebaseId());
                Log.d(TAG, "Section1: " + Section1);
            }
        });
    }

    public void upsertStudent(Student student) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                db.studentDao().insertStudent(student);
                Student s1 = db.studentDao().loadStudentById(student.getFirebaseId());
                Log.d(TAG, "updated Student: " + s1);
            }
        });
    }

    public void deleteSchool(School school) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                db.schoolDao().delete(school);
                Log.d(TAG, "Delete School: " + school);
            }
        });
    }

    public void deleteSectionById(String firebaseId) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                db.sectionDao().deleteById(firebaseId);
                Log.d(TAG, "Delete Section: " + firebaseId);
            }
        });
    }

    public void deleteStudentById(String firebaseId) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                db.studentDao().deleteById(firebaseId);
                Log.d(TAG, "Delete Student: " + firebaseId);
            }
        });
    }

    public void deleteStudent(Student student) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                db.studentDao().delete(student);
                Log.d(TAG, "Delete Student: " + student);
            }
        });
    }

    public void deleteSection(Section section) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                db.sectionDao().delete(section);
                Log.d(TAG, "Delete Section: " + section);
            }
        });
    }

    public void deleteSchoolById(String firebaseId) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                School school = db.schoolDao().loadSchoolById(firebaseId);
                if (school != null) {
                    db.schoolDao().delete(school);
                    Log.d(TAG, "Delete School: " + school);
                }
            }
        });
    }

    public void loadSchoolById(String firebaseId) {
        final School[] school = new School[1];
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                school[0] = db.schoolDao().loadSchoolById(firebaseId);
                Log.d(TAG, "School loaded" + school[0]);
            }
        });
    }

    public void loadStudentById(String firebaseId) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Student s = db.studentDao().loadStudentById(firebaseId);
                Log.d(TAG, "Student loaded" + s);
            }
        });
    }

    public void loadSectionById(String firebaseId) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Section s = db.sectionDao().loadSectionById(firebaseId);
                Log.d(TAG, "Section loaded" + s);
            }
        });
    }

    public void loadAllSchools() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<School> schools = db.schoolDao().loadAllSchools();
                for (School s : schools) {
                    Log.d(TAG, "School found:" + s);
                }
            }
        });
    }

    public void loadAllSections() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Section> s = db.sectionDao().loadAllSections();
                for (Section s1 : s) {
                    Log.d(TAG, "Section found:" + s1);
                }
            }
        });
    }

    public void loadAllStudents() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Student> s = db.studentDao().loadAllStudents();
                for (Student s1 : s) {
                    Log.d(TAG, "Student found:" + s1);
                }
            }
        });
    }

    public void initFirebaseSyncForAllCachedStudents() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Section> s = db.sectionDao().loadAllSections();
                for (Section s1 : s) {
                    FirebaseOperations.getInitializedInstance().addStudentListener(
                            "000Test", s1.getFirebaseId()
                    );
                }
            }
        });
    }

}
