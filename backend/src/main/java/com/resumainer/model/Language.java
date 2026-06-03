package com.resumainer.model;

import java.util.Objects;

/**
 * Language lookup — mapped to the 'language' table (BIGSERIAL PK).
 * Immutable read-only model for lookup data.
 */
public class Language {

    private Long id;
    private String code;
    private String name;

    public Language() {
    }

    public Language(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Language language = (Language) o;
        return Objects.equals(id, language.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Language{id=" + id + ", code='" + code + "'}";
    }
}
