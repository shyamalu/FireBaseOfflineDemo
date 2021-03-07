package org.chimple.myapplication.model;

import java.util.List;
import java.util.Objects;

public class School {
    //We Must have an empty constructor for Firestore
    public School() {
    }

    public School(String image, String name, boolean open, List<String> subjects) {
        this.image = image;
        this.name = name;
        this.open = open;
        this.subjects = subjects;
    }

    private String image;
    private String name;
    private boolean open;
    List<String> subjects;


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        School school = (School) o;
        return open == school.open &&
                Objects.equals(image, school.image) &&
                Objects.equals(name, school.name) &&
                Objects.equals(subjects, school.subjects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(image, name, open, subjects);
    }
}
