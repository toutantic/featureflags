package org.featureflags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlagManager {

    private static FlagManager instance;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private FeatureFlags[] flags;
    private Class<?> featureFlagsClass;

    public FlagManager(String className) {
	initFlags(className);

    }

    private void initFlags(String className) {
	loadFeatureFlagsClass(className);
	flags = (FeatureFlags[]) invokeStaticMethod("values", null);

	for (FeatureFlags flag : flags) {
	    FlagState flagState = flag.isUp() ? FlagState.DOWN : FlagState.UP;
	    log.info("{}({}) is " + flagState, flag, flag.getDescription());
	}

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
	if(flag == null) { 
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
	if(flag == null) { 
	    return Result.NOT_FOUND;
	}
	switch (newFlagState) {
	case UP:
	    flag.up();
	    log.info("Flag {} UP", flag);
	    return Result.FLIP_UP;
	case DOWN:
	    flag.down();
	    log.info("Flag {} UP", flag);
	    return Result.FLIP_DOWN;
	}
	
	// can't happend
	return Result.UNEXPECTED;
    }
    
    public FeatureFlags getFlag(String flagName) {
	return (FeatureFlags) Utils.invokeStaticClass(featureFlagsClass, "valueOf" , new Object[]{flagName},  String.class);
    }
    
    private void loadFeatureFlagsClass(String className) {
	log.info("Loading feature flags: " + className);
	try {
	    featureFlagsClass = Class.forName(className);
	} catch (ClassNotFoundException e) {
	    throw new RuntimeException("Can't find Feature Flags ",e);
	}
    }

    private Object invokeStaticMethod(String methodName, Object[] args, Class<?>... parameterTypes) {
	return Utils.invokeStaticClass(featureFlagsClass, methodName, args, parameterTypes);
    }
    
    public enum Result {
	FLIP_UP,
	FLIP_DOWN,
	NOT_FOUND,
	OK,
	UNEXPECTED
    }

    public enum FlagState {
	UP,
	DOWN
    }


    
}
