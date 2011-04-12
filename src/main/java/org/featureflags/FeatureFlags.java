package org.featureflags;

public interface FeatureFlags {

    boolean isUp();
    
    void up();
    
    void down();
    
    String getDescription();
    
}
