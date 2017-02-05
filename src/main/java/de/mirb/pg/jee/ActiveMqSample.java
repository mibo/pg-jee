package de.mirb.pg.jee;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * Created by mibo on 05.12.16.
 */
public class ActiveMqSample {

  // Run sample with activemq broker via docker container:
  // docker run -it --rm -p 38161:8161 -p 31616:61616 -P webcenter/activemq:latest
  // or to test with JMS credentials run:
  // docker run -it --rm -p 38161:8161 -p 31616:61616 -P mibo/activemq-sample:user
  // Admin console: http://localhost:38161 with user/pwd: admin/admin
  private static final String BROKER_URL = "tcp://localhost:31616";
  // Run sample with activemq as embedded broker
//  private static final String BROKER_URL = "vm://localhost";
  private static final int LOOPS = 1;
  public static final String USER_NAME = "user";
  public static final String PASSWORD = "password";

  public static void main(String[] args) throws Exception {
    if(LOOPS < 0) {
      while(true) {
        runSample();
      }
    }
    for (int i = 0; i < LOOPS; i++) {
      runSample();
    }
  }

  private static void runSample() throws InterruptedException {
    thread(new HelloWorldProducer(), false);
    thread(new HelloWorldProducer(), false);
    thread(new HelloWorldConsumer(), false);
    Thread.sleep(1000);
    thread(new HelloWorldConsumer(), false);
    thread(new HelloWorldProducer(), false);
    thread(new HelloWorldConsumer(), false);
    thread(new HelloWorldProducer(), false);
    Thread.sleep(1000);
    thread(new HelloWorldConsumer(), false);
    thread(new HelloWorldProducer(), false);
    thread(new HelloWorldConsumer(), false);
    thread(new HelloWorldConsumer(), false);
    thread(new HelloWorldProducer(), false);
    thread(new HelloWorldProducer(), false);
    Thread.sleep(1000);
    thread(new HelloWorldProducer(), false);
    thread(new HelloWorldConsumer(), false);
    thread(new HelloWorldConsumer(), false);
    thread(new HelloWorldProducer(), false);
    thread(new HelloWorldConsumer(), false);
    thread(new HelloWorldProducer(), false);
    thread(new HelloWorldConsumer(), false);
    thread(new HelloWorldProducer(), false);
    thread(new HelloWorldConsumer(), false);
    thread(new HelloWorldConsumer(), false);
    thread(new HelloWorldProducer(), false);
  }

  public static void thread(Runnable runnable, boolean daemon) {
    Thread brokerThread = new Thread(runnable);
    brokerThread.setDaemon(daemon);
    brokerThread.start();
  }

  public static class HelloWorldProducer implements Runnable {
    public void run() {
      try {
        // Create a ConnectionFactory
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);

        // Create a Connection
        Connection connection = connectionFactory.createConnection(USER_NAME, PASSWORD);
        connection.start();

        // Create a Session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create the destination (Topic or Queue)
        Destination destination = session.createQueue("TEST.FOO");

        // Create a MessageProducer from the Session to the Topic or Queue
        MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        // Create a messages
        String text = "Hello world! From: " + Thread.currentThread().getName() + " : " + this.hashCode();
        TextMessage message = session.createTextMessage(text);

        // Tell the producer to send the message
        System.out.println("Sent message: "+ message.hashCode() + " : " + Thread.currentThread().getName());
        producer.send(message);

        // Clean up
        session.close();
        connection.close();
      }
      catch (Exception e) {
        System.out.println("Caught: " + e);
        e.printStackTrace();
      }
    }
  }

  public static class HelloWorldConsumer implements Runnable, ExceptionListener {
    public void run() {
      try {

        // Create a ConnectionFactory
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);

        // Create a Connection
        Connection connection = connectionFactory.createConnection(USER_NAME, PASSWORD);
        connection.start();

        connection.setExceptionListener(this);

        // Create a Session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create the destination (Topic or Queue)
        Destination destination = session.createQueue("TEST.FOO");

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

    public synchronized void onException(JMSException ex) {
      System.out.println("JMS Exception occured.  Shutting down client.");
    }
  }
}
