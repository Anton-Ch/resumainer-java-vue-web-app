package com.resumainer.model;

import java.util.Objects;

/**
 * User generation permission lookup — mapped to the 'user_permission' table (BIGSERIAL PK).
 * Immutable read-only model for lookup data.
 */
public class UserPermission {

    private Long id;
    private String code;
    private String name;

    public UserPermission() {
    }

    public UserPermission(Long id, String code, String name) {
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
        UserPermission that = (UserPermission) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserPermission{id=" + id + ", code='" + code + "'}";
    }
}
