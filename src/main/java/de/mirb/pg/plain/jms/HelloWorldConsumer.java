package de.mirb.pg.plain.jms;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

public class HelloWorldConsumer implements Runnable, ExceptionListener {

  private final String brokerUrl;
  private final boolean useTopic;
  private final String name;
  private ActiveMQConnectionFactory connectionFactory;
  private int daemonWait = 2000;

  public HelloWorldConsumer(String brokerUrl, String name, boolean useTopic) {
    this.brokerUrl = brokerUrl;
    this.name = name;
    // Create a ConnectionFactory
    connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
    this.useTopic = useTopic;
  }

  public int getDaemonWait() {
    return daemonWait;
  }

  public void setDaemonWait(int daemonWait) {
    this.daemonWait = daemonWait;
  }

  @Override
  public void run() {
    consumeAllAvailable(daemonWait);
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
      MessageConsumer consumer = session.createConsumer(destination);

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
      return session.createTopic("TEST.TOPIC");
    }
    return session.createQueue("TEST.FOO");
  }


  public void consumeAllAvailable(int waitInMs) {
    try {
      // Create a Connection
      Connection connection = connectionFactory.createConnection();
      connection.start();

      connection.setExceptionListener(this);

      // Create a Session
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

      // Create the destination (Topic or Queue)
      Destination destination = createDestination(session);

      // Create a MessageConsumer from the Session to the Topic or Queue
      MessageConsumer consumer = session.createConsumer(destination);
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
      print("No more messages received after '" + daemonWait + "'ms");

      consumer.close();
      session.close();
      connection.close();
    } catch (Exception e) {
      print("Caught: " + e);
      e.printStackTrace();
    }
  }

  private void print(String message) {
    System.out.println(name + ": " + message);
  }

  public synchronized void onException(JMSException ex) {
    print("JMS Exception occured.  Shutting down client.");
  }
}
