package org.chimple.myapplication.model;

import com.google.firebase.firestore.DocumentReference;

import java.util.Objects;

public class Section {
    private String id;
    private String image;
    private String name;
    private DocumentReference school;

    public Section() {
    }

    public Section(String image, String name, DocumentReference school) {
        this.image = image;
        this.name = name;
        this.school = school;
    }

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

    public DocumentReference getSchool() {
        return school;
    }

    public void setSchool(DocumentReference school) {
        this.school = school;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Section section = (Section) o;
        return Objects.equals(id, section.id) &&
                Objects.equals(image, section.image) &&
                Objects.equals(name, section.name) &&
                Objects.equals(school, section.school);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, image, name, school);
    }
}
