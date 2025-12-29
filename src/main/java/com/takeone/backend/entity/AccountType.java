package com.takeone.backend.entity;

/**
 * User account types for the talent management platform
 */
public enum AccountType {
    /**
     * Artists - Create portfolios, post content, apply for jobs
     */
    CREATOR,

    /**
     * Scouts - Discover talent, view portfolios
     */
    SCOUT,

    /**
     * Employers - Post job opportunities, hire talent
     */
    FANATICS,

    /**
     * Admin - Platform administration
     */
    ADMIN,

    /**
     * Default Account Type until user selects
     */
    NEW_USER
}