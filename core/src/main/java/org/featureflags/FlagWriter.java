package org.featureflags;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.featureflags.FlagManager.FlagState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlagWriter {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private String userDir;
    private String featureFlagsClass;
    private FlagManager manager;
    private Map<FeatureFlags, FlagState> flagsStates;
    private Map<String, Map<FeatureFlags, FlagState>> flagUsers;
    private File file;
    
    public FlagWriter(String flagName, FlagManager manager, Map<FeatureFlags, FlagState> flagsStates, Map<String, Map<FeatureFlags, FlagState>> flagUsers) {
	this.manager = manager;
	this.featureFlagsClass = flagName;
	this.flagsStates = flagsStates;
	this.flagUsers = flagUsers;
	userDir = System.getProperty("user.dir");
	file = new File(userDir,flagName);
    }
    
    public void persist() {
	log.debug(flagsStates.toString());
	log.debug(flagUsers.toString());
	Writer writer = null;
	try {
	    writer = new BufferedWriter(new FileWriter(file));
	    writer.write(flagsStates.toString());
	    writer.write("\n");
	    writer.write(flagUsers.toString());
	    
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
	
	readFlagsStates(flagsStatesString, flagsStates);
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
	    Map<FeatureFlags, FlagState> userflagsStates = new HashMap<FeatureFlags, FlagManager.FlagState>();
	    readFlagsStates(flagsStatesString, userflagsStates);
	    if(userflagsStates.size() != 0) {
		flagUsers.put(userName, userflagsStates);
	    }
	}
	
    }

    private boolean doesNotEndWith(String string, String character) {
	return string.lastIndexOf(character) != string.length() -1;
    }

    private void readFlagsStates(String flagsStatesString, Map<FeatureFlags, FlagState> mapToUpdate) {
	//removing { }
	flagsStatesString = flagsStatesString.substring(1, flagsStatesString.length()-1);
	String[] flagsStatesArray = flagsStatesString.split(",");
	for (String flagString : flagsStatesArray) {
	    flagString=flagString.trim();
	    String[] flagStringArray = flagString.split("=");
	    FeatureFlags key = manager.getFlag(flagStringArray[0]);
	    if(key != null) {
		FlagState value = FlagState.valueOf(flagStringArray[1]);
		mapToUpdate.put(key, value);
	    }
	}
    }
    
}
