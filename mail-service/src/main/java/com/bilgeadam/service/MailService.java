package com.bilgeadam.service;

import com.bilgeadam.dto.response.ForgotPasswordMailResponseDto;
import com.bilgeadam.rabbitmq.model.RegisterMailModel;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;

    public void sendMail(RegisterMailModel registerMailModel){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("${spring.mail.username}");
        mailMessage.setTo(registerMailModel.getEmail());
        mailMessage.setSubject("ACTIVATION CODE");
        mailMessage.setText(registerMailModel.getUsername() + " kaydınız başarıyla oluşturuldu.\n\n\n"+
                            "Activation Code: " + registerMailModel.getActivationCode());
        javaMailSender.send(mailMessage);
    }

    public Boolean sendMailForgetPassword(ForgotPasswordMailResponseDto dto) {
        try{
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("${spring.mail.username}");
            mailMessage.setTo(dto.getEmail());
            mailMessage.setSubject("ŞİFRE SIFIRLAMA E-POSTASI");
            mailMessage.setText("Yeni şifreniz: " + dto.getPassword() +
                    "\n\nGiriş yaptıktan sonra güvenlik nedeniyle şifrenizi değiştiriniz.");
            javaMailSender.send(mailMessage);
        }catch (Exception e){
            e.getMessage();
        }
        return true;
    }
}
