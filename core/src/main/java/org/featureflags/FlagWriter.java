package org.featureflags;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.featureflags.FlagManager.FlagState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlagWriter {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private FlagManager manager;
    private File file;
    
    public FlagWriter(FlagManager manager) {
	this.manager = manager;
	String userDir = System.getProperty("user.dir");
	file = new File(userDir,manager.getFeatureFlagClassName());
    }
    
    public void persist() {
	log.info(manager.getFlagsStatesAsString());
	log.info(manager.getFlagsUsersAsString());
	Writer writer = null;
	try {
	    writer = new BufferedWriter(new FileWriter(file));
	    writer.write(manager.getFlagsStatesAsString());
	    writer.write("\n");
	    writer.write(manager.getFlagsUsersAsString());
	    
	} catch (FileNotFoundException e) {
	    log.error("Writing flagfile", e);
	} catch (IOException e) {
	    log.error("Writing flagfile", e);
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
    
    public void read() {
	String flagsStatesString = null;
	String flagUsersString = null;
	BufferedReader reader = null;
	try {
	    reader = new BufferedReader(new FileReader(file));
	} catch (FileNotFoundException e) {
	    log.info("No flag persisted " + file.getAbsolutePath());
	    return;
	}

	try {
	    flagsStatesString = reader.readLine();
	    flagUsersString = reader.readLine();
	} catch (IOException ioe) {
	    log.error("Reading flagfile", ioe);
	}

	readFlagsStates(flagsStatesString, null);
	readFlagsUsersStates(flagUsersString);
	
    }

    private void readFlagsUsersStates(String flagUsersString) {
	flagUsersString = flagUsersString.substring(1, flagUsersString.length()-1);
	if(flagUsersString.length() == 0) {
	    return;
	}
	String[] flagUsersArray = flagUsersString.split("},");
	for (String flagString : flagUsersArray) {
	    flagString=flagString.trim();
	    if(doesNotEndWith(flagString,"}")) {
		flagString+= "}";
	    }
	    String[] userArray = flagString.split("=",2);
	    String userName = userArray[0];
	    String flagsStatesString = userArray[1].trim();
	    readFlagsStates(flagsStatesString, userName);
	}
	
    }

    private boolean doesNotEndWith(String string, String character) {
	return string.lastIndexOf(character) != string.length() -1;
    }

    private void readFlagsStates(String flagsStatesString, String userName) {
	//removing { }
	flagsStatesString = flagsStatesString.substring(1, flagsStatesString.length()-1);
	String[] flagsStatesArray = flagsStatesString.split(",");
	for (String flagString : flagsStatesArray) {
	    flagString=flagString.trim();
	    String[] flagStringArray = flagString.split("=");
	    FeatureFlags key = manager.getFlag(flagStringArray[0]);
	    if(key != null) {
		FlagState value = FlagState.valueOf(flagStringArray[1]);
		if(userName != null) {
		    manager.setFlagStateForUserTo(userName, flagStringArray[0], value);
		} else {
		    manager.setFlagStateTo(flagStringArray[0], value);
		}
	    }
	}
    }
    
}
