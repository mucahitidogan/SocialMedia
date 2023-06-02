package com.bilgeadam.controller;

import com.bilgeadam.dto.response.ForgotPasswordMailResponseDto;
import com.bilgeadam.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/mail")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;
    @PostMapping("forgot-password")
    public ResponseEntity<Boolean> forgotPasswordMail(@RequestBody ForgotPasswordMailResponseDto dto){
        return ResponseEntity.ok(mailService.sendMailForgetPassword(dto));
    }
}
