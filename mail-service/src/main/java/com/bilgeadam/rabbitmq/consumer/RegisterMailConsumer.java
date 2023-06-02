package com.bilgeadam.rabbitmq.consumer;

import com.bilgeadam.rabbitmq.model.RegisterMailModel;
import com.bilgeadam.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterMailConsumer {

    private final MailService mailService;

    @RabbitListener(queues = ("${rabbitmq.registerMailQueue}"))
    public void sendActivationCode(RegisterMailModel registerMailModel) {
        mailService.sendMail(registerMailModel);
    }
}
