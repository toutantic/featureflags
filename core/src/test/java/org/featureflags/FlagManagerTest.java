package org.featureflags;

import static org.junit.Assert.assertEquals;

import org.featureflags.FeatureFlags;
import org.featureflags.FlagManager;
import org.featureflags.FlagManager.Result;
import org.junit.Test;

public class FlagManagerTest {

    @Test
    public void testGetFlag() {
	FlagManager manager = FlagManager.get("org.featureflags.Flags");
	manager.initFlags();
	FeatureFlags flag = manager.getFlag("ONE");
	assertEquals("get flag ONE", Flags.ONE, flag);
	flag = manager.getFlag("THREE");
	assertEquals("get flag THREE", Flags.THREE, flag);
	flag = manager.getFlag("YOU SHALL NOT EXIST");
	assertEquals("get flag YOU SHALL NOT EXIST", null, flag);
    }

    @Test
    public void testFlipFlag() {
	FlagManager manager = FlagManager.get("org.featureflags.Flags");

	flipFlag(manager, "ONE");
	flipFlag(manager, "TWO");
	flipFlag(manager, "THREE");
    }

    private void flipFlag(FlagManager manager, String flagName) {
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

}
