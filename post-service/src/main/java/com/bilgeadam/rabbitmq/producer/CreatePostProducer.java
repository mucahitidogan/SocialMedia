package com.bilgeadam.rabbitmq.producer;

import com.bilgeadam.rabbitmq.model.CreatePostModel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreatePostProducer {

    @Value("${rabbitmq.exchange}")
    String exchange;
    @Value("${rabbitmq.bindingKeyCreatePost}")
    String createPostBindingKey;

    private final RabbitTemplate rabbitTemplate;

    public Object  createPost(CreatePostModel model){
        return rabbitTemplate.convertSendAndReceive(exchange, createPostBindingKey, model);
    }
}
