package org.featureflags;

import static org.junit.Assert.assertEquals;

import org.featureflags.FeatureFlags;
import org.featureflags.FlagManager;
import org.featureflags.FlagManager.FlagState;
import org.featureflags.FlagManager.Result;
import org.junit.Before;
import org.junit.Test;

public class FlagManagerTest {
    
    private FlagManager manager;
    
    @Before
    public void setUp() {
	FlagManager.reset();
	manager = FlagManager.get("org.featureflags.Flags");
	manager.setThreadUserName(null);
    }
    
    @Test
    public void testGetFlag() {

	FeatureFlags flag = manager.getFlag("ONE");
	assertEquals("get flag ONE", Flags.ONE, flag);
	flag = manager.getFlag("THREE");
	assertEquals("get flag THREE", Flags.THREE, flag);
	flag = manager.getFlag("YOU SHALL NOT EXIST");
	assertEquals("get flag YOU SHALL NOT EXIST", null, flag);
    }

    @Test
    public void testFlipFlag() {
	flipFlag("ONE");
	flipFlag("TWO");
	flipFlag("THREE");
    }

    private void flipFlag(String flagName) {
	FeatureFlags flag = manager.getFlag(flagName);
	boolean status = flag.isUp();
	Result expectedResult = status ? Result.FLIP_DOWN : Result.FLIP_UP;
	
	Result flipResult = manager.flipFlag(flagName);
	
	assertEquals("flip flag Result", !status, flag.isUp());
	assertEquals("flip flag Result", expectedResult, flipResult);
	
	flipResult = manager.flipFlag(flagName);
	expectedResult =  status ? Result.FLIP_UP : Result.FLIP_DOWN;
	assertEquals("flip flag Result", status, flag.isUp());
	assertEquals("flip flag Result", expectedResult, flipResult);
    }

    @Test
    public void testFlipFlagForUser() {
	String[] testUsers = new String[]{"bob","john","foo","bar","bob2","john2","foo2","bar2"};
	String[] flagState = new String[]{"UP","UP","DOWN","UP","UP","UP","DOWN","UP"};
	FlagThread[] flagThreads = new FlagThread[8];
	for (int i = 0; i < testUsers.length; i++) {
	    flagThreads[i] = new FlagThread(testUsers[i]);
	    manager.setFlagStateForUserTo(testUsers[i], Flags.ONE.toString(), FlagState.valueOf(flagState[i]));
	}

	for (int i = 0; i < testUsers.length; i++) {
	    Thread thread = new Thread(flagThreads[i]);
	    thread.start();
	}

	try {
	    Thread.sleep(100);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	
	assertEquals(true, flagThreads[0].isUp());
	assertEquals(true, flagThreads[1].isUp());
	assertEquals(false, flagThreads[2].isUp());
	assertEquals(true, flagThreads[3].isUp());
	assertEquals(true, flagThreads[4].isUp());
	assertEquals(true, flagThreads[5].isUp());
	assertEquals(false, flagThreads[6].isUp());
	assertEquals(true, flagThreads[7].isUp());
	assertEquals(false, Flags.ONE.isUp());
	
	assertEquals(true, Flags.TWO.isUp());
	manager.setFlagStateForUserTo("other", Flags.TWO.toString(), FlagState.DOWN);
	assertEquals(true, Flags.TWO.isUp());
	manager.setThreadUserName("other");
	assertEquals(false, Flags.ONE.isUp());
	assertEquals(false, Flags.TWO.isUp());
	
	
    }
    
}
