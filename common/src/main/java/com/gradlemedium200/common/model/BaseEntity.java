package com.gradlemedium200.common.model;

import java.util.Date;

/**
 * Base entity class that provides common fields and functionality for all entities in the system.
 * This class includes auditing fields for tracking creation and modification information,
 * as well as optimistic locking support through a version field.
 *
 * @author gradlemedium200
 * @version 1.0
 */
public abstract class BaseEntity {

    /**
     * Unique identifier for the entity
     */
    private String id;
    
    /**
     * Date when the entity was created
     */
    private Date createdDate;
    
    /**
     * Date when the entity was last updated
     */
    private Date updatedDate;
    
    /**
     * User who created the entity
     */
    private String createdBy;
    
    /**
     * User who last updated the entity
     */
    private String updatedBy;
    
    /**
     * Version number for optimistic locking
     */
    private Long version;

    /**
     * Default constructor
     */
    public BaseEntity() {
        // Initialize with current date for new entities
        this.createdDate = new Date();
        this.updatedDate = new Date();
        this.version = 1L;
    }
    
    /**
     * Constructor with ID
     * 
     * @param id the entity identifier
     */
    public BaseEntity(String id) {
        this();
        this.id = id;
    }

    /**
     * Get the entity ID
     *
     * @return the entity ID
     */
    public String getId() {
        return id;
    }

    /**
     * Set the entity ID
     *
     * @param id the entity ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the creation date
     *
     * @return the date when the entity was created
     */
    public Date getCreatedDate() {
        return createdDate != null ? new Date(createdDate.getTime()) : null;
    }

    /**
     * Set the creation date
     *
     * @param createdDate the date to set as creation date
     */
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate != null ? new Date(createdDate.getTime()) : null;
    }

    /**
     * Get the date when the entity was last updated
     * 
     * @return the last updated date
     */
    public Date getUpdatedDate() {
        return updatedDate != null ? new Date(updatedDate.getTime()) : null;
    }

    /**
     * Set the date when the entity was last updated
     * 
     * @param updatedDate the date to set as updated date
     */
    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate != null ? new Date(updatedDate.getTime()) : null;
    }

    /**
     * Get the user who created the entity
     * 
     * @return the creator's username or ID
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Set the user who created the entity
     * 
     * @param createdBy the username or ID of the creator
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Get the user who last updated the entity
     * 
     * @return the updater's username or ID
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Set the user who last updated the entity
     * 
     * @param updatedBy the username or ID of the updater
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Get the version number for optimistic locking
     * 
     * @return the version number
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Set the version number for optimistic locking
     * 
     * @param version the version number to set
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Update the entity's audit information before saving
     * 
     * @param username the user performing the update
     */
    public void prepareForUpdate(String username) {
        this.updatedDate = new Date();
        this.updatedBy = username;
        
        // Increment version for optimistic locking
        if (this.version != null) {
            this.version++;
        } else {
            this.version = 1L;
        }
    }

    /**
     * Initialize the entity with creation information
     * 
     * @param username the user creating the entity
     */
    public void prepareForCreate(String username) {
        Date now = new Date();
        this.createdDate = now;
        this.updatedDate = now;
        this.createdBy = username;
        this.updatedBy = username;
        this.version = 1L;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseEntity that = (BaseEntity) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id='" + id + '\'' +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                ", version=" + version +
                '}';
    }
}