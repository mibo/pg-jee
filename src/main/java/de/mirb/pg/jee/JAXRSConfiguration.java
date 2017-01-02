package de.mirb.pg.jee;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.Set;

/**
 * Configures a JAX-RS endpoint. Delete this class, if you are not exposing
 * JAX-RS resources in your application.
 */
@ApplicationPath("/")
public class JAXRSConfiguration extends Application {

//  @Override
//  public Set<Class<?>> getClasses() {
//    return Collections.singleton(ActiveMqJndi.class);
//  }
}
