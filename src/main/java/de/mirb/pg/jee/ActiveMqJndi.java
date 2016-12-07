package de.mirb.pg.jee;

import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mibo on 06.12.16.
 */
@Path("jms")
public class ActiveMqJndi {

  private TopicPublisher publisher;
  private List<String> messages = new ArrayList<>();

//  public ActiveMqJndi() {
//    try {
//      lookup();
//    } catch (NamingException | JMSException e) {
//      e.printStackTrace();
//    }
//  }

  @GET
  @Path("ping")
  public String ping() {
    return "ping";
  }

  @GET
  @Path("list")
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> listMessages() {
    lookup();
    return messages;
  }

  @GET
  @Path("send")
  @Produces(MediaType.APPLICATION_JSON)
  public String sendMessage(@QueryParam("msg") String message) {
    lookup();
    ActiveMQTextMessage msg = new ActiveMQTextMessage();
    try {
      msg.setText(message);
      publisher.publish(msg);
    } catch (JMSException e) {
      return "FAIL: " + e.getMessage();
    }
    return "OKAY";
  }


  private void lookup() {
    if(publisher == null) {
      // Create a new initial context, which loads from jndi.properties file:
      try {
        Context ctx = null;
        ctx = new InitialContext();
        // Lookup the connection factory:
        TopicConnectionFactory factory = (TopicConnectionFactory) ctx.lookup("ConnectionFactory");

        // Create a new TopicConnection for pub/sub messaging:
        TopicConnection conn = factory.createTopicConnection();

        // Lookup an existing topic:
        Topic mytopic = (Topic) ctx.lookup("MyTopic");

        // Create a new TopicSession for the client:
        TopicSession session = conn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
    //    session = conn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);

        // Create a new subscriber to receive messages:
        TopicSubscriber subscriber = session.createSubscriber(mytopic);
        publisher = session.createPublisher(mytopic);

        subscriber.setMessageListener(message -> {
          if(message instanceof TextMessage) {
            try {
              messages.add(((TextMessage) message).getText());
            } catch (JMSException e) {
              e.printStackTrace();
            }
          }
        });
      } catch (NamingException | JMSException e) {
        e.printStackTrace();
      }

    }
  }
}
