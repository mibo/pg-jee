package de.mirb.pg.plain.jms;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HelloWorldProducer implements Runnable {

  private final String brokerUrl;
  private final boolean useTopic;
  private final String destinationName;
  private ActiveMQConnectionFactory connectionFactory;
  private int daemonDelay = 1000;
  private int deamonCount = 10;

  public HelloWorldProducer(String brokerUrl, boolean useTopic, String destinationName) {
    this.brokerUrl = brokerUrl;
    this.useTopic = useTopic;
    this.destinationName = destinationName;
    // Create a ConnectionFactory
    connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
  }

  public int getSendDelay() {
    return daemonDelay;
  }

  public void setSendDelay(int sendDelay) {
    this.daemonDelay = sendDelay;
  }

  public int getSendCount() {
    return deamonCount;
  }

  public void setSendCount(int sendCount) {
    this.deamonCount = sendCount;
  }

  public void run(int count, int delayInMs) {
    try {
      // Create a Connection
      Connection connection = connectionFactory.createConnection("artemis", "simetraehcapa");
      connection.start();

      // Create a Session
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Destination destination = createDestination(session);

      // Create a MessageProducer from the Session to the Topic or Queue
      MessageProducer producer = session.createProducer(destination);
      producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

      SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss.SSS");

      for (int i = 0; i < count; i++) {
        // Create a messages
        final String time = sdf.format(new Date());
        String text = "Message (id='" + i + "') at " + time
            + "! From: " + Thread.currentThread().getName() + " : " + this.hashCode();
        TextMessage message = session.createTextMessage(text);

        // Tell the producer to send the message
        System.out.println("Sent message (id='" + i + "'): " + message.hashCode() + " : " + Thread.currentThread().getName());
        producer.send(message);

        Thread.sleep(delayInMs);
//        TimeUnit.MILLISECONDS.wait(delayInMs);
      }

      // Clean up
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

  private String grantDestinationName() {
    if(destinationName == null) {
      if(useTopic) {
        return "TEST.TOPIC";
      }
      return "TEST.FOO";
    }
    return destinationName;
  }

  @Override
  public void run() {
    run(deamonCount, daemonDelay);
  }
}
