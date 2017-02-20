package de.mirb.pg.plain.jms;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

public class HelloWorldConsumer implements Runnable, ExceptionListener {

  private final String brokerUrl;
  private final boolean useTopic;
  private final String name;
  private final String destinationName;
  private ActiveMQConnectionFactory connectionFactory;
  private int receivedWait = 2000;
  private boolean durable;

  public HelloWorldConsumer(String brokerUrl, String consumerName, boolean useTopic) {
    this(brokerUrl, consumerName, null, useTopic, false);
  }

  public HelloWorldConsumer(String brokerUrl, String consumerName, String destinationName, boolean useTopic, boolean durable) {
    this.brokerUrl = brokerUrl;
    this.name = consumerName;
    // Create a ConnectionFactory
    connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
    this.useTopic = useTopic;
    this.durable = durable;
    this.destinationName = destinationName;
  }

  public int getReceivedWait() {
    return receivedWait;
  }

  public void setReceivedWait(int receivedWait) {
    this.receivedWait = receivedWait;
  }

  @Override
  public void run() {
    consumeAllAvailable(receivedWait);
  }

  public void consumeLatestMessage() {
    try {
      // Create a Connection
      Connection connection = connectionFactory.createConnection();
      connection.start();

      connection.setExceptionListener(this);

      // Create a Session
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Destination destination = createDestination(session);

      // Create a MessageConsumer from the Session to the Topic or Queue
      MessageConsumer consumer = createConsumer(session, destination);

      // Wait for a message
      Message message = consumer.receive(1000);

      if (message instanceof TextMessage) {
        TextMessage textMessage = (TextMessage) message;
        String text = textMessage.getText();
        System.out.println("Received: " + text);
      } else {
        System.out.println("Received: " + message);
      }

      consumer.close();
      session.close();
      connection.close();
    } catch (Exception e) {
      System.out.println("Caught: " + e);
      e.printStackTrace();
    }
  }

  private Destination createDestination(Session session) throws JMSException {
    // Create the destination (Topic or Queue)
    if(useTopic) {
      return session.createTopic(grantDestinationName());
    }
    return session.createQueue(grantDestinationName());
  }


  public void consumeAllAvailable(int waitInMs) {
    try {
      // Create a Connection
      Connection connection = connectionFactory.createConnection();
      connection.setClientID(name);
      connection.setExceptionListener(this);

      connection.start();

      // Create a Session
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

      // Create the destination (Topic or Queue)
      Destination destination = createDestination(session);
      MessageConsumer consumer = createConsumer(session, destination);

      Thread.sleep(waitInMs);
      Message message = consumer.receive(waitInMs);
      while (message != null) {
        if (message instanceof TextMessage) {
          TextMessage textMessage = (TextMessage) message;
          String text = textMessage.getText();
          print("Received message: " + text);
        } else {
          print("Received (none text msg): " + message);
        }
        // Wait for next message
        message = consumer.receive(waitInMs);
      }
      print("No more messages received after '" + receivedWait + "'ms");

      consumer.close();
      session.close();
      connection.close();
    } catch (Exception e) {
      print("Caught: " + e);
      e.printStackTrace();
    }
  }

  private MessageConsumer createConsumer(Session session, Destination destination) throws JMSException {
    if(durable) {
      if(destination instanceof Topic) {
        // JMS 2.0
//        return session.createDurableConsumer((Topic) destination, grantDestinationName());
        // JMS 1.1
        return session.createDurableSubscriber((Topic) destination, grantDestinationName());
      }
      throw new IllegalStateException("Destination is no Topic => not possible to create a durable consumer.");
    }
    // Create a MessageConsumer from the Session to the Topic or Queue
    return session.createConsumer(destination);
  }

  private void print(String message) {
    System.out.println(name + ": " + message);
  }

  private String grantDestinationName() {
    if(destinationName == null) {
      if(useTopic) {
        return "TEST.TOPIC";
      }
      return "TEST.FOO";
    }
    return destinationName;
  }


  public synchronized void onException(JMSException ex) {
    print("JMS Exception occured.  Shutting down client.");
  }
}
