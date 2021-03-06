package org.chimple.myapplication.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import org.chimple.myapplication.model.Student;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface StudentDao {
    @Query("SELECT * FROM STUDENT where sectionId = :sectionId and schoolId = :schoolId")
    List<Student> loadAllStudentsBySchoolIdAndSectionId(String schoolId, String sectionId);

    @Query("SELECT * FROM STUDENT where schoolId = :schoolId")
    List<Student> loadAllStudentsBySchoolId(String schoolId);

    @Query("SELECT * FROM STUDENT where schoolId = :schoolId and is_synced = 0")
    List<Student> findAllNonSyncedProfiles(String schoolId);


    @Insert(onConflict = REPLACE)
    void insertStudent(Student Student);

    @Delete
    void delete(Student Student);

    @Query("delete from STUDENT where firebaseId = :firebaseId")
    void deleteById(String firebaseId);

    @Query("delete from STUDENT where sectionId = :sectionId")
    void deleteBySectionId(String sectionId);


    @Query("SELECT COUNT(firebaseId) FROM Student WHERE firebaseId = :firebaseId LIMIT 1")
    Integer countStudentById(String firebaseId);

    @Query("SELECT * FROM Student WHERE firebaseId = :firebaseId LIMIT 1")
    Student loadStudentById(String firebaseId);

    @Query("update STUDENT set profile_info = :profile, is_synced = :synced WHERE firebaseId = :firebaseId")
    void updateStudentProfile(String profile, String firebaseId, boolean synced);

    @Query("update STUDENT set is_synced = :sync WHERE firebaseId = :firebaseId")
    void updateSync(String firebaseId, boolean sync);

}
