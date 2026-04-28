package com.dddrivebye.territorialconfiguration.domain.entity;

import com.dddrivebye.shared.domain.valueobject.GeoCoordinates;
import com.dddrivebye.territorialconfiguration.domain.exception.InvalidTerritoryOperationException;
import com.dddrivebye.territorialconfiguration.domain.valueobject.GeographicZone;
import com.dddrivebye.territorialconfiguration.domain.valueobject.RegulatoryConstraint;
import com.dddrivebye.territorialconfiguration.domain.valueobject.TerritorialRule;
import com.dddrivebye.territorialconfiguration.domain.valueobject.TerritoryId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Territory {

    private final TerritoryId id;
    private String name;
    private GeographicZone zone;
    private TerritorialRule rule;
    private final Set<RegulatoryConstraint> constraints;
    private boolean active;

    private Territory(TerritoryId id,
                      String name,
                      GeographicZone zone,
                      TerritorialRule rule,
                      Set<RegulatoryConstraint> constraints,
                      boolean active) {
        this.id = id;
        this.name = name;
        this.zone = zone;
        this.rule = rule;
        this.constraints = constraints;
        this.active = active;
    }

    public static Territory create(String name,
                                   GeographicZone zone,
                                   TerritorialRule rule) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
        Objects.requireNonNull(rule, "rule must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return new Territory(TerritoryId.generate(), name.trim(), zone, rule, new HashSet<>(), true);
    }

    public static Territory reconstitute(TerritoryId id,
                                         String name,
                                         GeographicZone zone,
                                         TerritorialRule rule,
                                         Set<RegulatoryConstraint> constraints,
                                         boolean active) {
        return new Territory(id, name, zone, rule,
                constraints == null ? new HashSet<>() : new HashSet<>(constraints),
                active);
    }

    public TerritoryId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public GeographicZone zone() {
        return zone;
    }

    public TerritorialRule rule() {
        return rule;
    }

    public Set<RegulatoryConstraint> constraints() {
        return Collections.unmodifiableSet(constraints);
    }

    public boolean isActive() {
        return active;
    }

    public void updateRule(TerritorialRule newRule) {
        Objects.requireNonNull(newRule, "rule must not be null");
        this.rule = newRule;
    }

    public void addConstraint(RegulatoryConstraint constraint) {
        Objects.requireNonNull(constraint);
        constraints.add(constraint);
    }

    public void removeConstraint(String code) {
        constraints.removeIf(c -> c.code().equalsIgnoreCase(code));
    }

    public void deactivate() {
        if (!active) {
            throw new InvalidTerritoryOperationException("Territory already inactive: " + name);
        }
        active = false;
    }

    public boolean covers(GeoCoordinates point) {
        return active && zone.covers(point);
    }

    public TerritorialRule mergedRulesWithFallback(Territory fallback) {
        if (fallback == null) {
            return rule;
        }
        return rule;
    }

    public List<RegulatoryConstraint> constraintsList() {
        return new ArrayList<>(constraints);
    }
}
