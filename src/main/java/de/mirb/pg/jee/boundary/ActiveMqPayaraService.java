package de.mirb.pg.jee.boundary;

import de.mirb.pg.jee.ActiveMqJndi;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.stream.Collectors;

/**
 * Created by mibo on 06.12.16.
 */
@Path("amq")
public class ActiveMqPayaraService {
  private static final Logger LOG = LoggerFactory.getLogger(ActiveMqPayaraService.class);
  private static final String JMS_MY_CONNECTION_FACTORY = "jms/myConnectionFactory";
  public static final String JMS_TEST_TOPIC = "jms/test-topic";
  private List<String> messages = new ArrayList<>();
  private ConnectionHolder connectionHolder;

  public ActiveMqPayaraService() {
    lookup();
  }

  @GET
  @Path("ping")
  public String ping() {
    return "ping";
  }

  @GET
  @Path("list")
  @Produces(MediaType.APPLICATION_JSON)
  public String listMessages() {
    lookup();
    return "{" + messages.stream().collect(Collectors.joining(",")) + "}";
  }

  @GET
  @Path("send")
  @Produces(MediaType.APPLICATION_JSON)
  public String sendMessage(@QueryParam("msg") String message) {
    lookup();
    ActiveMQTextMessage msg = new ActiveMQTextMessage();
    try {
      msg.setText(message);
      connectionHolder.publisher.publish(msg);
      connectionHolder.close();
    } catch (JMSException e) {
      return "FAIL: " + e.getMessage();
    }
    return "OKAY";
  }

  @GET
  @Path("relookup")
  public boolean reLookup() {
    connectionHolder = null;
    lookup();
    return connectionHolder != null;
  }

  private void lookup() {
    if(connectionHolder == null) {
      // Create a new initial context, which loads from jndi.properties file:
      try {
        Context ctx = new InitialContext();
        // Lookup the connection factory
        TopicConnectionFactory factory = (TopicConnectionFactory) ctx.lookup(JMS_MY_CONNECTION_FACTORY);

        // Create a new TopicConnection for pub/sub messaging:
        TopicConnection conn = factory.createTopicConnection();
        conn.start();

        // Lookup an existing topic
        Topic mytopic = (Topic) ctx.lookup(JMS_TEST_TOPIC);

        // Create a new TopicSession for the client:
        TopicSession session = conn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        TopicPublisher publisher = session.createPublisher(mytopic);

        // Create a new subscriber to receive messages:
        TopicSubscriber subscriber = session.createSubscriber(mytopic);
        subscriber.setMessageListener(message -> {
          if(message instanceof TextMessage) {
            try {
              messages.add(((TextMessage) message).getText());
              LOG.info("MSG Listener: Received message...");
            } catch (JMSException e) {
              LOG.error("JMSException occurred.", e);
            }
          } else {
            LOG.error("Unknown message class: {}", message.getClass());
          }
        });
        connectionHolder = new ConnectionHolder(conn, session, subscriber, publisher);
      } catch (NamingException | JMSException e) {
        e.printStackTrace();
      }
    }
  }

  private class ConnectionHolder {
    private Connection connection;
    private TopicSession session;
    private TopicSubscriber consumer;
    private TopicPublisher publisher;

    public ConnectionHolder(Connection connection, TopicSession session, TopicSubscriber consumer, TopicPublisher publisher) {
      this.connection = connection;
      this.session = session;
      this.consumer = consumer;
      this.publisher = publisher;
    }

    public void close() throws JMSException {
      this.publisher.close();
      this.consumer.close();
      this.session.close();
      this.connection.close();
    }
  }


//  private void lookup() {
//    if(publisher == null) {
//      // Create a new initial context, which loads from jndi.properties file:
//      try {
//        Context ctx = new InitialContext();
//        // Lookup the connection factory:
//        TopicConnectionFactory factory = (TopicConnectionFactory) ctx.lookup(JMS_MY_CONNECTION_FACTORY);
//
//        // Create a new TopicConnection for pub/sub messaging:
//        TopicConnection conn = factory.createTopicConnection();
//
//        // Lookup an existing topic:
//        Topic mytopic = (Topic) ctx.lookup("jms/test-topic");
//
//        // Create a new TopicSession for the client:
//        TopicSession session = conn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
//    //    session = conn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
//
//        // Create a new subscriber to receive messages:
//        TopicSubscriber subscriber = session.createSubscriber(mytopic);
//        publisher = session.createPublisher(mytopic);
//
//        subscriber.setMessageListener(message -> {
//          if(message instanceof TextMessage) {
//            try {
//              messages.add(((TextMessage) message).getText());
//            } catch (JMSException e) {
//              e.printStackTrace();
//            }
//          }
//        });
//      } catch (NamingException | JMSException e) {
//        e.printStackTrace();
//      }
//
//    }
//  }
}
