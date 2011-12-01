package org.featureflags;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.featureflags.FlagManager.FlagState;
import org.junit.Test;

public class TestFlagWriterTest extends FeatureFlagTest {

    @Test
    public void test00Init() {
	writeFlagFile();
	assertTrue("Flag ONE is up", Flags.ONE.isUp());
    }
    
    public void testPersist() {
	String userName= "bob";
	String userName2= "foo";
	
	manager.setFlagStateToAndPersist(Flags.ONE, FlagState.DOWN);
	manager.setFlagStateToAndPersist(Flags.TWO, FlagState.UP);
	manager.setFlagStateForUserToAndPersist(userName, Flags.ONE.name(), FlagState.UP);
	manager.setFlagStateForUserToAndPersist(userName2, Flags.THREE.name(), FlagState.DOWN);
	manager.setFlagStateForUserToAndPersist(userName2, Flags.TWO.name(), FlagState.DOWN);
	FlagWriter writer = new FlagWriter(manager);
	writer.persist();
	
	
	writer = new FlagWriter(manager);
	writer.read();
	
	assertEquals(false, Flags.ONE.isUp());
	assertEquals(true, Flags.TWO.isUp());
	manager.setThreadUserName(userName);
	assertEquals(true, Flags.ONE.isUp());
	manager.setThreadUserName(userName2);
	assertEquals(false, Flags.THREE.isUp());
	
    }
    
    private void writeFlagFile() {
	String userDir = System.getProperty("user.dir");
	File file = new File(userDir,"org.featureflags.Flags");
	Writer writer = null;
	try {
	    writer = new BufferedWriter(new FileWriter(file));
	    writer.write("{TWO=UP, THREE=DOWN, ONE=UP}");
	    writer.write("\n");
	    writer.write("{}");
	    
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	finally {
	    if(writer != null) {
		try {
		    writer.close();
		} catch (IOException e) {
		}
	    }
	}
    }
    
}
