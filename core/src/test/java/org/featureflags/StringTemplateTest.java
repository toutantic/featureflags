package org.featureflags;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class StringTemplateTest {

    private StringTemplate template;
    private FlagManager manager;

    @Before
    public void setUp() {
	FlagManager.reset();
	manager = FlagManager.get("org.featureflags.Flags");
	manager.resetUsersFlags();

	template = new StringTemplate(manager);
	template.setServletUri("/ff/flags");
	
	String userDir = System.getProperty("user.dir");
	File file = new File(userDir,"org.featureflags.Flags");
	file.delete();
	
    }
    
    @Test
    public void testNoUser() {
	String actual = template.getHtmlForFlag(Flags.ONE, null);
	String expected = Utils.readRessource(this, "/expectedNoUser.html");

	assertEquals("NoUser", expected, actual);
    }
    
    @Test
    public void testUserBob() {
	
	String actual = template.getHtmlForFlag(Flags.ONE, "bob");
	String expected = Utils.readRessource(this, "/expectedUserDoesNotExist.html");
	assertEquals("UserDoesNotExist", expected, actual);
	manager.flipFlagForUser("bob", Flags.ONE.name());
	actual = template.getHtmlForFlag(Flags.ONE, "bob");
	expected = Utils.readRessource(this, "/expectedUser.html");

	assertEquals("User", expected, actual);
    }


}
