package org.featureflags;

import java.io.File;

import org.featureflags.FlagManager.FlagState;
import org.junit.After;
import org.junit.Before;

public abstract class FeatureFlagTest {
    protected FlagManager manager;

    
    @Before
    public void setUp() {
	manager = FlagManager.get(Flags.class);
	manager.init();
	
	// enum are static 
	// Our test are dependent on each other
	// We reset the state of the flags manually
	Flags.ONE.initFlag("First Feature Flag", FlagState.DOWN);
	Flags.TWO.initFlag("Second Feature Flag", FlagState.UP);
	Flags.THREE.initFlag("Third Feature Flag", FlagState.DOWN);
	
	deleteFile();
	
    }
    
    @After
    public void deleteFile() {
	String userDir = System.getProperty("user.dir");
	File file = new File(userDir,"org.featureflags.Flags");
	file.delete();
    }
    
}
