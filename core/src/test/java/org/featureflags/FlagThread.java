package org.featureflags;


public class FlagThread implements Runnable {

    String userName;
    boolean result;
    
    
    public boolean isUp() {
        return result;
    }

    public FlagThread(String userName) {
	this.userName = userName;
    }
    
    public void run() {
	FlagManager manager = FlagManager.get(); 
	manager.setThreadUserName(userName);
	result = Flags.ONE.isUp();
	manager.resetThreadUserName();
    }

}
