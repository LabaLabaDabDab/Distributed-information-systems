package com.manager.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.manager.request.queue.name}")
    private String managerRequestQueueName;

    @Value("${rabbitmq.worker.response.queue.name}")
    private String workerResponseQueueName;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.manager.request.routing.key}")
    private String managerRequestRoutingKey;

    @Value("${rabbitmq.worker.response.routing.key}")
    private String workerResponseRoutingKey;

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue managerRequestQueue() {
        return new Queue(managerRequestQueueName, true, false, false);
    }

    @Bean
    public Queue workerResponseQueue() {
        return new Queue(workerResponseQueueName, true, false, false);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Binding managerBinding(DirectExchange exchange, Queue managerRequestQueue) {
        return BindingBuilder.bind(managerRequestQueue).to(exchange).with(managerRequestRoutingKey);
    }

    @Bean
    public Binding workerBinding(DirectExchange exchange, Queue workerResponseQueue) {
        return BindingBuilder.bind(workerResponseQueue).to(exchange).with(workerResponseRoutingKey);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setPort(port);
        return connectionFactory;
    }
}