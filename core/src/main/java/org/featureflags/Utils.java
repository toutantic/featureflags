package org.featureflags;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static Logger log = LoggerFactory.getLogger("org.featureflags.Utils");

    public static String readRessource(Object obj, String ressourceName) {
	InputStream input = obj.getClass().getResourceAsStream(ressourceName);
	Reader reader = new InputStreamReader(input);
	return readerToString(reader);
    }

    public static String readerToString(Reader reader) {
	StringBuilder sb = new StringBuilder();
	char[] buffer = new char[4096];
	int charsRead;
	try {
	    while ((charsRead = reader.read(buffer)) >= 0) {
		sb.append(buffer, 0, charsRead);
	    }
	} catch (IOException e) {
	    log.error("Can't read content", e);
	} finally {
	    try {
		if (reader != null) {
		    reader.close();
		}
	    } catch (IOException e) {
		log.error("Can't close reader", e);
	    }
	}
	return sb.toString();
    }

    public static Object invokeStaticClass(Class<?> staticClass, String methodName, Object[] args, Class... parameterTypes) {
	Method method = null;
	try {
	    method = staticClass.getMethod(methodName, parameterTypes);
	} catch (SecurityException e) {
	    log.error("Can't get method " + methodName, e);
	    return null;
	} catch (NoSuchMethodException e) {
	    log.error("Can't get method " + methodName, e);
	    return null;
	}

	try {
	    return method.invoke(null, args);
	} catch (IllegalArgumentException e) {
	    log.error("Can't call method " + methodName, e);
	} catch (IllegalAccessException e) {
	    log.error("Can't call method " + methodName, e);
	} catch (InvocationTargetException e) {
	    if(e.getTargetException() instanceof IllegalArgumentException) {
		log.info("Flag does not exist " + args[0]);
	    } else {
		log.error("Can't call method " + methodName, e);
	    }
	}

	return null;
    }

}
