package de.mirb.pg.plain.jms;

/**
 * Created by mibo on 05.12.16.
 */
public class ActiveMqTopicSample {

  // Run sample with activemq broker via docker container
  // docker run -it --rm -p 38161:8161 -p 31616:61616 -P webcenter/activemq:latest
  // Admin console: http://localhost:38161 with user/pwd: admin/admin
  private static final String BROKER_URL = "tcp://localhost:31616";
  // Run sample with activemq as embedded broker
//  private static final String BROKER_URL = "vm://localhost";
  private static final int LOOPS = 5;

  public static void main(String[] args) throws Exception {
    boolean useTopic = false;
    HelloWorldProducer producer = new HelloWorldProducer(BROKER_URL, useTopic);
//    producer.run(10, 500);

    HelloWorldConsumer consumer = new HelloWorldConsumer(BROKER_URL, useTopic);
//    consumer.consumeAllAvailable(1000);

    thread(producer, false);
    thread(consumer, false);
  }

//  private static void runSample() throws InterruptedException {
//    thread(new HelloWorldProducer(brokerUrl), false);
//    thread(new HelloWorldProducer(brokerUrl), false);
//    thread(new HelloWorldConsumer(brokerUrl), false);
//    Thread.sleep(1000);
//    thread(new HelloWorldConsumer(brokerUrl), false);
//    thread(new HelloWorldProducer(brokerUrl), false);
//    thread(new HelloWorldConsumer(brokerUrl), false);
//    thread(new HelloWorldProducer(brokerUrl), false);
//    Thread.sleep(1000);
//    thread(new HelloWorldConsumer(brokerUrl), false);
//    thread(new HelloWorldProducer(brokerUrl), false);
//    thread(new HelloWorldConsumer(brokerUrl), false);
//    thread(new HelloWorldConsumer(brokerUrl), false);
//    thread(new HelloWorldProducer(brokerUrl), false);
//    thread(new HelloWorldProducer(brokerUrl), false);
//    Thread.sleep(1000);
//    thread(new HelloWorldProducer(brokerUrl), false);
//    thread(new HelloWorldConsumer(brokerUrl), false);
//    thread(new HelloWorldConsumer(brokerUrl), false);
//    thread(new HelloWorldProducer(brokerUrl), false);
//    thread(new HelloWorldConsumer(brokerUrl), false);
//    thread(new HelloWorldProducer(brokerUrl), false);
//    thread(new HelloWorldConsumer(brokerUrl), false);
//    thread(new HelloWorldProducer(brokerUrl), false);
//    thread(new HelloWorldConsumer(brokerUrl), false);
//    thread(new HelloWorldConsumer(brokerUrl), false);
//    thread(new HelloWorldProducer(brokerUrl), false);
//  }

  public static void thread(Runnable runnable, boolean daemon) {
    Thread brokerThread = new Thread(runnable);
    brokerThread.setDaemon(daemon);
    brokerThread.start();
  }
}
