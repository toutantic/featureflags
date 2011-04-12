package org.featureflags;

import org.featureflags.FeatureFlags;
import org.featureflags.FlagManager.FlagState;

public enum Flags implements FeatureFlags {

    ONE("First Feature Flag"),
    TWO("Second Feature Flag", FlagState.UP),
    THREE("Third Feature Flag");
    
    //Don't change anything below
    private FlagState flagState = FlagState.DOWN;
    private String description;
    private FlagManager flagManager;
    
    private Flags(String description) {
	initFlag(description, flagState);
    }

    private Flags(String description, FlagState flagState) {
	this.flagState = flagState;
	initFlag(description, flagState);
    }

    public void initFlag(String description, FlagState flagState) {
	this.description = description;
	this.flagManager = FlagManager.get(this, flagState);    
    }
    
    public boolean isUp() {
	    return flagManager.isUp(this);
    }

    public String getDescription() {
        return description;
    }
    
}
