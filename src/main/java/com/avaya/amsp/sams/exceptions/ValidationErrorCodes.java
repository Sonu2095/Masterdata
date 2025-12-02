package com.avaya.amsp.sams.exceptions;

public enum ValidationErrorCodes {

    AREA_CODE_NOT_FOUND(-100, "Area code was not found"),
    EXT_ALREADY_IN_USE(-103, "Extension is already in use for the given area code."),
    EXT_NT_USABLE(-105, "Extension is not usable for a new connection"),
    NAME_MISSING(-106, "Name is missing"),
    COST_CENTER_MISSING(-107, "Cost Centre is missing"),
    COST_CENTER_NOT_VALID(-108, "Cost Centre is not valid"),
    AMFK_LOCATIN_MISSING(-109, "AMFK location is missing"),
    AMFK_LOCATION_NOT_UNIQUE(-110, "AMFK location is not unique"),
    PBX_SYSTEM_MISSING(-113, "PBX system for SfB is missing for the cluster of the passed area code"),
    ORDER_NOT_FOUND(-200, "Order was not found"),
    EXT_NOT_FOUND(-101, "Extension was not found for the given area code"),
    DIFF_EXT(-102, "The found connection for the given area code and extension has a different connection type"),
    CONNECTION_TYPE_NOT_CONFIG(-111, "Connection Type is not configured for the cluster of the given area code"),
    WRONG_API_KEY(-2,"Wrong API-KEY"),
    MISSING_API_KEY(-1,"Missing API-KEY"),
    MGR_NOT_SUPPORTED(-114, "Migration to SfB/Teams is not supported for this original connection type"),
    WRONG_CONNECTION_TYPE(-122, "Wrong connection type"),
    MGR_TO_SAME_BCS_TYPE_NOT_ALLOWED(-146, "No migration to the same BCS type");

    private final int code;
    private final String message;

    ValidationErrorCodes(int code, String message) {
        this.code = code;
        this.message = message;
    }

    // Getter methods
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ValidationErrorCodes fromCode(int code) {
        for (ValidationErrorCodes error : ValidationErrorCodes.values()) {
            if (error.code == code) {
                return error;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
