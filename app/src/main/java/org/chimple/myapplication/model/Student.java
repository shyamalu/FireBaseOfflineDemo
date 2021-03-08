package org.chimple.myapplication.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "STUDENT")
public class Student {
    @ColumnInfo(name = "age")
    private int age;

    @ColumnInfo(name = "countryCode")
    private String countryCode;

    @ColumnInfo(name = "gender")
    private String gender;

    @ColumnInfo(name = "image")
    private String image;

    @ColumnInfo(name = "link")
    private boolean link;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "phoneNumber")
    private String phoneNumber;

    @ColumnInfo(name = "progressId")
    private String progressId;

    @NonNull
    @PrimaryKey
    private String firebaseId;

    public Student() {
    }

    public Student(String firebaseId, int age, String countryCode, String gender, String image, boolean link, String name, String password, String phoneNumber, String progressId) {
        this.firebaseId = firebaseId;
        this.age = age;
        this.countryCode = countryCode;
        this.gender = gender;
        this.image = image;
        this.link = link;
        this.name = name;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.progressId = progressId;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isLink() {
        return link;
    }

    public void setLink(boolean link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProgressId() {
        return progressId;
    }

    public void setProgressId(String progressId) {
        this.progressId = progressId;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return age == student.age &&
                link == student.link &&
                Objects.equals(countryCode, student.countryCode) &&
                Objects.equals(gender, student.gender) &&
                Objects.equals(image, student.image) &&
                Objects.equals(name, student.name) &&
                Objects.equals(password, student.password) &&
                Objects.equals(phoneNumber, student.phoneNumber) &&
                Objects.equals(progressId, student.progressId) &&
                firebaseId.equals(student.firebaseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(age, countryCode, gender, image, link, name, password, phoneNumber, progressId, firebaseId);
    }

    @Override
    public String toString() {
        return "Student{" +
                "age=" + age +
                ", countryCode='" + countryCode + '\'' +
                ", gender='" + gender + '\'' +
                ", image='" + image + '\'' +
                ", link=" + link +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", progressId='" + progressId + '\'' +
                ", firebaseId='" + firebaseId + '\'' +
                '}';
    }
}
