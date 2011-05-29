package org.featureflags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author apelletier
 *
 */
public class FlagManager {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static FlagManager instance;
    private FeatureFlags[] flags;
    private Class<?> featureFlagsClass;
    private String featureFlagClassName;
    private Map<FeatureFlags, FlagState> flagsStates;
    private Map<String, Map<FeatureFlags, FlagState>> flagUsers;
    private FlagWriter flagWriter;

    private static ThreadLocal<String> currentUser;

    public enum Result {
	FLIP_UP, FLIP_DOWN, NOT_FOUND, OK
    }

    public enum FlagState {
	UP, DOWN
    }

    private FlagManager(String className) {
	this.featureFlagClassName = className;
	flagsStates = new HashMap<FeatureFlags, FlagManager.FlagState>();
	flagUsers = new HashMap<String, Map<FeatureFlags, FlagState>>();
	currentUser = new ThreadLocal<String>();
	flagWriter = new FlagWriter(featureFlagClassName,this, flagsStates, flagUsers);
    }

    public static FlagManager get(FeatureFlags flag, FlagState flagState) {
	if (instance == null) {
	    instance = new FlagManager(flag.getClass().getCanonicalName());
	}
	// We do not persist state when we initializa the flags
	instance.setFlagStateTo(flag, flagState, false);
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
	flagWriter.read();
    }

    public FeatureFlags[] getFlags() {
	return flags;
    }

    /**
     * @param flagName
     *            name of the flag you want to flip
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
	return setFlagStateTo(flag, newFlagState, true);
    }

    public Result setFlagStateTo(String flagName, FlagState newFlagState) {
	FeatureFlags flag = getFlag(flagName);
	return setFlagStateTo(flag, newFlagState, true);
    }

    public Result setFlagStateTo(FeatureFlags flag, FlagState newFlagState, boolean persist) {
	return setFlagStateTo(this.flagsStates, "every user", flag, newFlagState, persist);
    }
    
    private Result setFlagStateTo(Map<FeatureFlags, FlagState> flagsStatesToChange,String userName, FeatureFlags flag, FlagState newFlagState) {
	return setFlagStateTo(flagsStatesToChange, userName, flag, newFlagState, true);
    }
    
    private Result setFlagStateTo(Map<FeatureFlags, FlagState> flagsStatesToChange,String userName, FeatureFlags flag, FlagState newFlagState, boolean persist) {
	if (flag == null) {
	    return Result.NOT_FOUND;
	}
	flagsStatesToChange.put(flag, newFlagState);
	if(persist) {
	    flagWriter.persist();
	}
	log.info("Flag {} {} for {}", new Object[] { flag, newFlagState, userName });

	return newFlagState == FlagState.UP ? Result.FLIP_UP : Result.FLIP_DOWN;
    }
    

    public boolean isUp(FeatureFlags flag) {
	String userName = getThreadUserName();
	Map<FeatureFlags, FlagState> userFlagsState = flagUsers.get(userName);
	FlagState currentFlagState = null;
	if (userFlagsState != null && userFlagsState.get(flag) != null) {
	    currentFlagState = userFlagsState.get(flag);
	} else {
	    currentFlagState = flagsStates.get(flag);
	}
	return currentFlagState == FlagState.UP ? true : false;
    }

    public FeatureFlags getFlag(String flagName) {
	return (FeatureFlags) invokeStaticMethod("valueOf", new Object[] { flagName }, String.class);
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

    private Map<FeatureFlags, FlagState> getOrCreateUser(String userName) {
	Map<FeatureFlags, FlagState> userFlagsState = flagUsers.get(userName);
	if (userFlagsState == null) {
	    userFlagsState = new HashMap<FeatureFlags, FlagManager.FlagState>();
	    flagUsers.put(userName, userFlagsState);
	}
	return userFlagsState;
    }

    public Result flipFlagForUser(String userName, String flagName) {
	setThreadUserName(userName);
	Map<FeatureFlags, FlagState> userFlagsState = getOrCreateUser(userName);
	FeatureFlags flag = getFlag(flagName);
	FlagState newFlagState = flag.isUp() ? FlagState.DOWN : FlagState.UP;
	resetThreadUserName();
	return setFlagStateTo(userFlagsState, userName, flag, newFlagState);
    }

    public Result setFlagStateForUserTo(String userName, String flagName, FlagState newFlagState) {
	FeatureFlags flag = getFlag(flagName);
	Map<FeatureFlags, FlagState> userFlagsState = getOrCreateUser(userName);
	return setFlagStateTo(userFlagsState, userName, flag, newFlagState);
    }

    public void resetThreadUserName() {
	currentUser.remove();
    }

    public void setThreadUserName(String userName) {
	currentUser.set(userName);
    }

    public String getThreadUserName() {
	return currentUser.get();
    }
    
    
    /**
     * @param flag
     * @return the list username who have a specific state for this flag.
     */
    public String[] getUsersForFlag(FeatureFlags flag) {
	List<String> userNames = new ArrayList<String>();
	for (String userName : flagUsers.keySet()) {
	    Map<FeatureFlags, FlagState> flagsStates = flagUsers.get(userName);
	    for (FeatureFlags userFlag : flagsStates.keySet()) {
		if(flag == userFlag) {
		    userNames.add(userName);
		}
	    }
	}
	return userNames.toArray(new String[userNames.size()]);
    }

}
