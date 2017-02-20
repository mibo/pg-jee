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
    // send to queue with two consumer -> message split over consumer
//    sampleQueues();

    // send to topic with two consumer -> message delivered to all consumer
    sampleNoneDurableTopics();

    // send to durable topic with two consumer -> message delivered to all consumer
//    sampleDurableTopic();

    // first produce (true) and later consume with two consumer -> message delivered to all consumer
    // first call is necessary to create the durable topic after first ActiveMQ start
//    sampleDurableTopicSplitProduceAndConsume(false);
//    Thread.sleep(5000);
//    sampleDurableTopicSplitProduceAndConsume(true);
//    Thread.sleep(2000);
//    sampleDurableTopicSplitProduceAndConsume(false);
  }

  public static void sampleQueues() throws Exception {
    boolean useTopic = false;
    final String destinationName = "QUEUE.FOO.SAMPLE";

    HelloWorldProducer producer = new HelloWorldProducer(BROKER_URL, useTopic, destinationName);
    producer.setSendCount(10);
    producer.setSendDelay(500);

    final boolean durable = false;
    HelloWorldConsumer consumer = new HelloWorldConsumer(BROKER_URL, "C1", destinationName, useTopic, durable);
    HelloWorldConsumer consumer2 = new HelloWorldConsumer(BROKER_URL, "C2", destinationName, useTopic, durable);

    thread(producer, false);
    thread(consumer, false);
    thread(consumer2, false);
  }

  public static void sampleNoneDurableTopics() throws Exception {
    boolean useTopic = true;
    final String destinationName = "TOPIC.FOO.SAMPLE";

    HelloWorldProducer producer = new HelloWorldProducer(BROKER_URL, useTopic, destinationName);
    producer.setSendCount(10);
    producer.setSendDelay(500);

    final boolean durable = false;
    HelloWorldConsumer consumer = new HelloWorldConsumer(BROKER_URL, "C1", destinationName, useTopic, durable);
    HelloWorldConsumer consumer2 = new HelloWorldConsumer(BROKER_URL, "C2", destinationName, useTopic, durable);

    thread(producer, false);
    thread(consumer, false);
    thread(consumer2, false);
  }

  public static void sampleDurableTopicSplitProduceAndConsume(boolean produceMode) throws Exception {
    boolean useTopic = true;
    final String destinationName = "TOPIC.C";
    if(produceMode) {
      HelloWorldProducer producer = new HelloWorldProducer(BROKER_URL, useTopic, destinationName);
      producer.setSendCount(50);
      producer.setSendDelay(200);
      thread(producer, false);
    } else {
      HelloWorldConsumer consumer = new HelloWorldConsumer(BROKER_URL, "C1", destinationName, useTopic, true);
      HelloWorldConsumer consumer2 = new HelloWorldConsumer(BROKER_URL, "C2", destinationName, useTopic, true);
      thread(consumer, false);
      thread(consumer2, false);
    }
  }

  public static void sampleDurableTopic() throws Exception {
    boolean useTopic = true;
    final String destinationName = "TOPIC.C";
    HelloWorldProducer producer = new HelloWorldProducer(BROKER_URL, useTopic, destinationName);
    producer.setSendCount(50);
    producer.setSendDelay(2);

    HelloWorldConsumer consumer = new HelloWorldConsumer(BROKER_URL, "C1", destinationName, useTopic, true);
    HelloWorldConsumer consumer2 = new HelloWorldConsumer(BROKER_URL, "C2", destinationName, useTopic, true);

    thread(producer, false);
    thread(consumer, false);
    thread(consumer2, false);
  }


  public static void thread(Runnable runnable, boolean daemon) {
    Thread brokerThread = new Thread(runnable);
    brokerThread.setDaemon(daemon);
    brokerThread.start();
  }
}
