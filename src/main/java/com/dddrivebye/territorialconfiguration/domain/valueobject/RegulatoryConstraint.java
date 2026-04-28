package com.dddrivebye.territorialconfiguration.domain.valueobject;

import java.util.Objects;

public final class RegulatoryConstraint {

    private final String code;
    private final String description;

    private RegulatoryConstraint(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static RegulatoryConstraint of(String code, String description) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(description, "description must not be null");
        if (code.isBlank() || description.isBlank()) {
            throw new IllegalArgumentException("code and description must not be blank");
        }
        return new RegulatoryConstraint(code.trim(), description.trim());
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegulatoryConstraint that)) return false;
        return code.equals(that.code) && description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, description);
    }
}
