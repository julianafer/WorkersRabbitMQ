package org.example;

import com.rabbitmq.client.*;

public class Worker {
    private static final String TASK_QUEUE_NAME = "task_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();

        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
        System.out.println("[*] Aguardando mensagens. Para sair, pressione CTRL + C");

        channel.basicQos(1); // Recebe apenas uma mensagem por vez

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");

            System.out.println("[x] Recebido: '" + message + "'");
            try {
                doWork(message);
            } finally {
                System.out.println("[x] Feito");
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };

        channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> {});
    }

    private static void doWork(String task) {
        for (char ch : task.toCharArray()) {
            if (ch == '.') {
                try {
                    Thread.sleep(1000); // Simula processamento
                } catch (InterruptedException _ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}

