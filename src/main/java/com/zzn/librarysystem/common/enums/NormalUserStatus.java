package com.zzn.librarysystem.common.enums;

public enum NormalUserStatus {

    /**
     * Newly registered but never logged in
     */
    NEVER_LOGIN,

    /**
     * Normal status
     */
    ACTIVE,

    /**
     * Locked by admin
     */
    LOCKED,

    /**
     * User had deleted
     */
    DISABLED,
}
