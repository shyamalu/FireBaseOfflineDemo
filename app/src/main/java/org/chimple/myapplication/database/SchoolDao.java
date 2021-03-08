package org.chimple.myapplication.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import org.chimple.myapplication.model.School;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface SchoolDao {
    @Query("SELECT * FROM SCHOOL")
    List<School> loadAllSchools();

    @Insert(onConflict = REPLACE)
    void insertSchool(School school);

    @Delete
    void delete(School school);

    @Query("SELECT COUNT(firebaseId) FROM SCHOOL WHERE firebaseId = :firebaseId LIMIT 1")
    Integer countSchoolById(String firebaseId);

    @Query("SELECT * FROM SCHOOL WHERE firebaseId = :firebaseId LIMIT 1")
    School loadSchoolById(String firebaseId);

}
