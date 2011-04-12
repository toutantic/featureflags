package org.featureflags;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlagManager {

    private static FlagManager instance;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private FeatureFlags[] flags;
    private Class<?> featureFlagsClass;
    private String featureFlagClassName;
    private Map<FeatureFlags, FlagState> flagsStates;

    public enum Result {
	FLIP_UP, FLIP_DOWN, NOT_FOUND, OK
    }

    public enum FlagState {
	UP, DOWN
    }

    private FlagManager(String className) {
	this.featureFlagClassName = className;
	flagsStates = new HashMap<FeatureFlags, FlagState>();
    }

    
    public static FlagManager get(FeatureFlags flag, FlagState flagState) {
	if (instance == null) {
	    instance = new FlagManager(flag.getClass().getCanonicalName());
	}
	instance.setFlagStateTo(flag, flagState);
	return instance;
    }
    
    public static FlagManager get(String className) {
	if (instance == null) {
	    instance = new FlagManager(className);
	}
	return instance;
    }

    public static FlagManager get() {
	return instance;
    }

    public void initFlags() {
	loadFeatureFlagsClass(featureFlagClassName);
	flags = (FeatureFlags[]) invokeStaticMethod("values", null);
    }

    public FeatureFlags[] getFlags() {
	return flags;
    }

    /**
     * @param flagName name of the flag you want to flip
     * @return Result the Result of the action
     */
    public Result flipFlag(String flagName) {
	FeatureFlags flag = getFlag(flagName);
	if (flag == null) {
	    return Result.NOT_FOUND;
	}
	return flipFlag(flag);
    }

    public Result flipFlag(FeatureFlags flag) {
	FlagState newFlagState = flag.isUp() ? FlagState.DOWN : FlagState.UP;
	return setFlagStateTo(flag, newFlagState);
    }

    public Result setFlagStateTo(String flagName, FlagState newFlagState) {
	FeatureFlags flag = getFlag(flagName);
	return setFlagStateTo(flag, newFlagState);
    }

    public Result setFlagStateTo(FeatureFlags flag, FlagState newFlagState) {
	if (flag == null) {
	    return Result.NOT_FOUND;
	}
	flagsStates.put(flag, newFlagState);
	log.info("Flag {} {}", flag,newFlagState);
	
	return newFlagState == FlagState.UP ? Result.FLIP_UP : Result.FLIP_DOWN;
	
    }
    
    public boolean isUp(FeatureFlags flag) {
	FlagState currentFlagState = flagsStates.get(flag);
	return currentFlagState == FlagState.UP ? true : false;
    }

    public FeatureFlags getFlag(String flagName) {
	return (FeatureFlags) Utils.invokeStaticClass(featureFlagsClass, "valueOf", new Object[] { flagName }, String.class);
    }

    private void loadFeatureFlagsClass(String className) {
	log.info("Loading feature flags: " + className);
	try {
	    featureFlagsClass = Class.forName(className);
	} catch (ClassNotFoundException e) {
	    throw new RuntimeException("Can't find Feature Flags ", e);
	}
    }

    private Object invokeStaticMethod(String methodName, Object[] args, Class<?>... parameterTypes) {
	return Utils.invokeStaticClass(featureFlagsClass, methodName, args, parameterTypes);
    }


}
