package com.bilgeadam.config.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${rabbitmq.exchange}")
    String exchange;
    @Value("${rabbitmq.queueCreatePost}")
    String queueCreatePost;
    @Value("${rabbitmq.bindingKeyCreatePost}")
    String createPostBindingKey;

    @Bean
    Queue queueCreatePost(){
        return new Queue(queueCreatePost);
    }

    @Bean
    DirectExchange exchange(){
        return new DirectExchange(queueCreatePost);
    }

    @Bean
    public Binding createPostBindingKey(final Queue queueCreatePost, final DirectExchange exchange){
        return BindingBuilder.bind(queueCreatePost).to(exchange).with(createPostBindingKey);
    }

}
