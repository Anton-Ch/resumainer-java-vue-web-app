package com.resumainer.model;

import java.util.Objects;

/**
 * Work format lookup — mapped to the 'work_format' table (BIGSERIAL PK).
 * Immutable read-only model for lookup data.
 * Values seeded from BA data dictionary (V15 migration).
 */
public class WorkFormat {

    private Long id;
    private String code;
    private String name;

    public WorkFormat() {
    }

    public WorkFormat(Long id, String code, String name) {
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
        WorkFormat that = (WorkFormat) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "WorkFormat{id=" + id + ", code='" + code + "'}";
    }
}
