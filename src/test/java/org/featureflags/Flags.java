package org.featureflags;

import org.featureflags.FeatureFlags;

public enum Flags implements FeatureFlags {

    ONE("First Feature Flag"),
    TWO("Second Feature Flag", true),
    THREE("Third Feature Flag");
    
    private boolean flagState = false;
    private String description;
    
    private Flags(String description) {
	this.description = description;
    }

    private Flags(String description, boolean state) {
	this.description = description;
	this.flagState = state;
    }
    
    public boolean isUp() {
	    return flagState;
    }
    
    public void up() {
	this.flagState = true;
    }
    
    public void down() {
	this.flagState = false;
    }
    
    public String getDescription() {
        return description;
    }
    
}
