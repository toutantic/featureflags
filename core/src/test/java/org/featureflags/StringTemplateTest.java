package org.featureflags;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StringTemplateTest {

    private StringTemplate template;
    private String test1;
    private FlagManager manager;

    @Before
    public void setUp() {
	manager = FlagManager.get("org.featureflags.Flags");
	manager.initFlags();
	template = new StringTemplate(manager);
	template.setServletUri("/ff/flags");
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
	manager.flipFlagForUser("bob", Flags.ONE.toString());
	actual = template.getHtmlForFlag(Flags.ONE, "bob");
	expected = Utils.readRessource(this, "/expectedUser.html");

	assertEquals("User", expected, actual);
    }


}
