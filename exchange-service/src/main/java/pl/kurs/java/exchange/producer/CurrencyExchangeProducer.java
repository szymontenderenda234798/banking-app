//package pl.kurs.java.exchange.producer;
//
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.Connection;
//import pl.kurs.java.exchange.rabbitmq.RabbitMQConnectionFactory;
//
//public class CurrencyExchangeProducer {
//    private final static String QUEUE_NAME = "exchange_requests";
//
//    public static void main(String[] argv) throws Exception {
//        try (Connection connection = RabbitMQConnectionFactory.getConnection()) {
//            Channel channel = connection.createChannel();
//            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//            String message = "Exchange Request: USD to EUR, Amount: 100";
//            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
//            System.out.println(" [x] Sent '" + message + "'");
//        }
//    }
//}
