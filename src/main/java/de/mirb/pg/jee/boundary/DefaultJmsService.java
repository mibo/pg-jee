package de.mirb.pg.jee.boundary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("default")
public class DefaultJmsService {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultJmsService.class);
  private static final String MY_QUEUE = "myqueue";

  private ConnectionFactory connectionFactory;


  @GET
  @Path("send")
  public String sendMessage(@QueryParam("content") String content) {
    if(sendToQueuer(content)) {
      return "success";
    }
    return "fail";
  }

  @GET
  @Path("receive")
  public String receiveMessage(@QueryParam("content") String content) {
    return receiveFromQueue();
  }

  private String receiveFromQueue() {
    try {
      ConnectionFactory cf = grantConnectionFactory();
      Connection connection = cf.createConnection();
      connection.start();
      Session session = connection.createSession();

      Destination destination = session.createQueue(MY_QUEUE);
      MessageConsumer consumer = session.createConsumer(destination);
//      consumer.setMessageListener((message) -> {
//
//      });

      Message message = consumer.receive(1000);
      String text = null;
      if(message != null) {
        if (message instanceof TextMessage) {
          TextMessage textMessage = (TextMessage) message;
          text = textMessage.getText();
        } else {
          text = "unexpected message class: " + message.getClass();
        }
      } else {
        text = "No Message in queue " + MY_QUEUE;
      }

      session.close();
      connection.close();

      return text;
    } catch (JMSException e) {
      LOG.error("Error for Message send: " + e.getMessage());
      return "error";
    }
  }

  private boolean sendToQueuer(String message) {
    try {
      ConnectionFactory cf = grantConnectionFactory();
      Connection connection = cf.createConnection();
      connection.start();
      Session session = connection.createSession();

      Destination destination = session.createQueue(MY_QUEUE);
      MessageProducer producer = session.createProducer(destination);
      producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

      TextMessage msg = session.createTextMessage();
      msg.setText(message);
      producer.send(msg);

      session.close();
      connection.close();

      return true;
    } catch (JMSException e) {
      LOG.error("Error for Message send: " + e.getMessage());
      return false;
    }
  }

  private ConnectionFactory grantConnectionFactory() {
    if(connectionFactory == null) {
      try {
        Context ctx = new InitialContext();
        connectionFactory = (ConnectionFactory) ctx.lookup("jms/__defaultConnectionFactory");
      } catch (NamingException e) {
        LOG.error("Error for ConnectionFactory creation: " + e.getMessage());
      }
    }
    return connectionFactory;
  }
}
