package de.mirb.pg.jee;

import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mibo on 06.12.16.
 */
public class ActiveMqJndi {

  private ConnectionHolder connectionHolder;
  private List<String> messages = new ArrayList<>();

  public ActiveMqJndi() {
    lookup();
  }

  private static final int LOOPS = 5;

  // Run sample with correct 'jndi.properties' which should use an activemq broker via docker container
  // `docker run -it --rm -p 38161:8161 -p 31616:61616 -P webcenter/activemq:latest`
  // Admin console: http://localhost:38161 with user/pwd: admin/admin
  public static void main(String[] args) throws Exception {
    ActiveMqJndi activeMqJndi = new ActiveMqJndi();
    activeMqJndi.runSample(LOOPS);
    activeMqJndi.cleanup();
  }

  public void runSample(int loops) {
    if(loops < 1) {
      loops = 10;
    }
    for (int i = 0; i < loops; i++) {
      publishMessage("Message..." + i);
      System.out.println("Count: " + getMessageCount());
      System.out.println("Message: " + getLatestMessageText());
    }
  }

  public boolean publishMessage(String message) {
    try {
      lookup();
      ActiveMQTextMessage msg = new ActiveMQTextMessage();
      msg.setText(message);
      connectionHolder.publisher.publish(msg);
      return true;
    } catch (JMSException e) {
      e.printStackTrace();
      return false;
    }
  }

  public void cleanup() {
    try {
      connectionHolder.close();
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }

  private int getMessageCount() {
    return messages.size();
  }

  private String getLatestMessageText() {
    if(messages.isEmpty()) {
      return null;
    }
    return messages.get(messages.size()-1);
  }

  private void lookup() {
    if(connectionHolder == null) {
      // Create a new initial context, which loads from jndi.properties file:
      try {
        Context ctx = new InitialContext();
        // Lookup the connection factory (either as 'topicConnectionFactory' or 'ConnectionFactory'):
        TopicConnectionFactory factory = (TopicConnectionFactory) ctx.lookup("ConnectionFactory");

        // Create a new TopicConnection for pub/sub messaging:
        TopicConnection conn = factory.createTopicConnection();
        conn.start();

        // Lookup an existing topic (configured in jndi.properties):
        Topic mytopic = (Topic) ctx.lookup("MyTopic");

        // Create a new TopicSession for the client:
        TopicSession session = conn.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        TopicPublisher publisher = session.createPublisher(mytopic);

        // Create a new subscriber to receive messages:

        TopicSubscriber subscriber = session.createSubscriber(mytopic);
        subscriber.setMessageListener(message -> {
          if(message instanceof TextMessage) {
            try {
              messages.add(((TextMessage) message).getText());
              System.out.println("MSG Listener: Received message...");
            } catch (JMSException e) {
              e.printStackTrace();
            }
          } else {
            System.out.println("Unknown message class: " + message.getClass());
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
}
