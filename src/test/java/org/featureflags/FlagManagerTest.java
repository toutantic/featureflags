package org.featureflags;

import static org.junit.Assert.assertEquals;

import org.featureflags.FeatureFlags;
import org.featureflags.FlagManager;
import org.featureflags.FlagManager.Result;
import org.junit.Test;

public class FlagManagerTest {

    @Test
    public void testGetFlag() {
	FlagManager manager = new FlagManager("org.featureflags.Flags");
	FeatureFlags flag = manager.getFlag("ONE");
	assertEquals("get flag ONE", Flags.ONE, flag);
	flag = manager.getFlag("THREE");
	assertEquals("get flag THREE", Flags.THREE, flag);
	flag = manager.getFlag("YOU SHALL NOT EXIST");
	assertEquals("get flag YOU SHALL NOT EXIST", null, flag);
    }

    @Test
    public void testFlipFlag() {
	FlagManager manager = new FlagManager("org.featureflags.Flags");
	String flagName = "ONE";
	FeatureFlags flag = manager.getFlag(flagName);
	boolean status = flag.isUp();
	Result flipResult = manager.flipFlag(flagName);
	assertEquals("flip flag Result", !status, flag.isUp());
	assertEquals("flip flag Result", Result.FLIP_UP, flipResult);
	flipResult = manager.flipFlag(flagName);
	assertEquals("flip flag Result", status, flag.isUp());
	assertEquals("flip flag Result", Result.FLIP_DOWN, flipResult);
    }

}
