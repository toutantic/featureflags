package org.featureflags;

import org.featureflags.FeatureFlags;
import org.featureflags.FlagManager.FlagState;

public enum Flags implements FeatureFlags {

    ONE("First Feature Flag"),
    TWO("Second Feature Flag", FlagState.UP),
    THREE("Third Feature Flag");
    
    //Don't change anything below
    private String description;
    private static FlagManager flagManager;
    
    private Flags(String description) {
	this(description, FlagState.DOWN);
    }

    private Flags(String description, FlagState flagState) {
	initFlag(description, flagState);
    }

    public void initFlag(String description, FlagState flagState) {
	this.description = description;
	flagManager = FlagManager.get(this.getClass());
	flagManager.setFlagStateTo(this, flagState);
    }
    
    public boolean isUp() {
	    return flagManager.isUp(this);
    }

    public String getDescription() {
        return description;
    }

}
