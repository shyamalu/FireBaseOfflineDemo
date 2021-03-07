package org.chimple.myapplication.model;

import java.util.Objects;

public class Student {
    private int age;
    private String countryCode;
    private String gender;
    private String image;
    private boolean link;
    private String name;
    private String password;
    private String phoneNumber;
    private String progressId;
    private String id;

    public Student() {
    }

    public Student(int age, String countryCode, String gender, String image, boolean link, String name, String password, String phoneNumber, String progressId) {
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
                id.equals(student.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(age, countryCode, gender, image, link, name, password, phoneNumber, progressId, id);
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
                ", id='" + id + '\'' +
                '}';
    }
}
