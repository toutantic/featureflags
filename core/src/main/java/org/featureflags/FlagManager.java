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
    private boolean initialized = false;
    
    private FeatureFlags[] flags;
    private Class<?> featureFlagsClass;
    
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

    private FlagManager(Class<?> featureFlagsClass) {
	this.featureFlagsClass = featureFlagsClass;
	init();
    }

    public void init() {
	flagWriter = new FlagWriter(this);
	flagsStates = new HashMap<FeatureFlags, FlagManager.FlagState>();
	flagUsers = new HashMap<String, Map<FeatureFlags, FlagState>>();
	currentUser = new ThreadLocal<String>();
	initialized = false;
    }
    
    public static FlagManager get(Class<?> featureFlagsClass) {
	if(instance == null) {
	    instance = new FlagManager(featureFlagsClass);
	}

	return instance;
    }
    
    public static FlagManager get(String className) {
	return 	get(loadFeatureFlagsClass(className));
    }
    
    private static Class<?> loadFeatureFlagsClass(String className) {
	//log.info("Loading feature flags: " + className);
	Class<FeatureFlags> featureFlagsClass = null;
	try {
	    featureFlagsClass = (Class<FeatureFlags>) Class.forName(className);
	} catch (ClassNotFoundException e) {
	    throw new RuntimeException("Can't find Feature Flags ", e);
	}
	return featureFlagsClass;
    }
    

    public static FlagManager get() {
	return instance;
    }

    public void initFlags() {
	flagWriter.read();
	initialized = true;
    }

    /**
     * We can't call method on the FeatureFlag (like Flags.values() in a method call by the FeatureFlag class constructor
     * like in Flags.ONE => this calls the Flag constructor
     * We do this kind of call here in a lazy way
     * like in Flags.ONE.isUp() after the constructor call has ended
     */
    private void checkInitialized() {
	if(!initialized) {
	    initFlags();
	}
    }
    
    public FeatureFlags[] getFlags() {
	if(flags == null) {
	    flags = (FeatureFlags[]) invokeStaticMethod("values", null);
	}
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
	return setFlagStateToAndPersist(flag, newFlagState);
    }

    public Result setFlagStateTo(String flagName, FlagState newFlagState) {
	FeatureFlags flag = getFlag(flagName);
	return setFlagStateTo(flag, newFlagState);
    }
    
    public Result setFlagStateToAndPersist(String flagName, FlagState newFlagState) {
	FeatureFlags flag = getFlag(flagName);
	return setFlagStateToAndPersist(flag, newFlagState);
    }

    public Result setFlagStateToAndPersist(FeatureFlags flag, FlagState newFlagState) {
	return setFlagStateToAndPersist(this.flagsStates, "every user", flag, newFlagState);
    }
    
    public Result setFlagStateTo(FeatureFlags flag, FlagState newFlagState) {
	return setFlagStateTo(this.flagsStates, "every user", flag, newFlagState);
    }

    
    private Result setFlagStateTo(Map<FeatureFlags, FlagState> flagsStatesToChange,String userName, FeatureFlags flag, FlagState newFlagState) {
	if (flag == null) {
	    return Result.NOT_FOUND;
	}
	flagsStatesToChange.put(flag, newFlagState);
	log.info("Flag {} {} for {}", new Object[] { flag, newFlagState, userName });

	return newFlagState == FlagState.UP ? Result.FLIP_UP : Result.FLIP_DOWN;
    }
    
    private Result setFlagStateToAndPersist(Map<FeatureFlags, FlagState> flagsStatesToChange,String userName, FeatureFlags flag, FlagState newFlagState) {
	Result result = setFlagStateTo(flagsStatesToChange, userName, flag, newFlagState);
	flagWriter.persist();
	return result;
    }
    
    

    public boolean isUp(FeatureFlags flag) {
	checkInitialized();
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
	return setFlagStateToAndPersist(userFlagsState, userName, flag, newFlagState);
    }

    public Result setFlagStateForUserToAndPersist(String userName, String flagName, FlagState newFlagState) {
	FeatureFlags flag = getFlag(flagName);
	Map<FeatureFlags, FlagState> userFlagsState = getOrCreateUser(userName);
	return setFlagStateToAndPersist(userFlagsState, userName, flag, newFlagState);
    }
    
    public Result setFlagStateForUser(String userName, String flagName, FlagState newFlagState) {
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
	checkInitialized();
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

    public String getFeatureFlagClassName() {
	return featureFlagsClass.getCanonicalName();
    }
    
    public String getFlagsStatesAsString() {
	return flagsStates.toString();
    }
    
    public String getFlagsUsersAsString() {
	return flagUsers.toString();
    }
    
}
