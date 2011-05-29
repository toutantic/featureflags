package org.featureflags;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.featureflags.FlagManager.FlagState;
import org.junit.Test;

public class TestFlagWriterTest {

    @Test
    public void testPersist() {
	FlagManager manager = FlagManager.get("org.featureflags.Flags");
	manager.initFlags();
	Map<FeatureFlags, FlagState> flagsStates = new HashMap<FeatureFlags, FlagManager.FlagState>();
	Map<FeatureFlags, FlagState> bobflagsStates = new HashMap<FeatureFlags, FlagManager.FlagState>();
	Map<FeatureFlags, FlagState> fooflagsStates = new HashMap<FeatureFlags, FlagManager.FlagState>();
	Map<String, Map<FeatureFlags, FlagState>> flagUsers = new HashMap<String, Map<FeatureFlags,FlagState>>();
	
	flagsStates.put(Flags.ONE, FlagState.DOWN);
	flagsStates.put(Flags.TWO, FlagState.UP);
	bobflagsStates.put(Flags.ONE, FlagState.UP);
	fooflagsStates.put(Flags.THREE, FlagState.DOWN);
	fooflagsStates.put(Flags.TWO, FlagState.DOWN);
	String userName= "bob";
	String userName2= "foo";
	flagUsers.put(userName, bobflagsStates);
	flagUsers.put(userName2, fooflagsStates);
	FlagWriter writer = new FlagWriter("junit.test",manager, flagsStates, flagUsers);
	writer.persist();
	
	
	flagsStates = new HashMap<FeatureFlags, FlagManager.FlagState>();
	bobflagsStates = new HashMap<FeatureFlags, FlagManager.FlagState>();
	flagUsers = new HashMap<String, Map<FeatureFlags,FlagState>>();
	writer = new FlagWriter("junit.test",manager, flagsStates, flagUsers);
	writer.read();
	assertEquals(FlagState.DOWN, flagsStates.get(Flags.ONE));
	assertEquals(FlagState.UP, flagsStates.get(Flags.TWO));
	assertEquals(FlagState.UP, flagUsers.get(userName).get(Flags.ONE));
	assertEquals(FlagState.DOWN, flagUsers.get(userName2).get(Flags.THREE));
	
    
    }

}
