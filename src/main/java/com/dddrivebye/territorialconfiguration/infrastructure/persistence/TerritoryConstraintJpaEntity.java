package com.dddrivebye.territorialconfiguration.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(schema = "territory", name = "territory_constraints")
public class TerritoryConstraintJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "territory_id")
    private TerritoryJpaEntity territory;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "description", nullable = false)
    private String description;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public TerritoryJpaEntity getTerritory() { return territory; }
    public void setTerritory(TerritoryJpaEntity territory) { this.territory = territory; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
