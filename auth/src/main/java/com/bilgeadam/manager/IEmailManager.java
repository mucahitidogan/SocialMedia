package com.bilgeadam.manager;

import com.bilgeadam.dto.response.ForgotPasswordMailResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        url = "http://localhost:8085/api/v1/mail",
        name = "auth-mail"
)
public interface IEmailManager {

    @PostMapping("forgot-password")
    public Boolean forgotPasswordMail(@RequestBody ForgotPasswordMailResponseDto dto);
}
