package com.bilgeadam.manager;

import com.bilgeadam.dto.request.ToAuthPasswordChangeDto;
import com.bilgeadam.dto.request.UpdateEmailOrUsernameRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "auth-service",
        url = "http://localhost:8090/api/v1/auth"
)
public interface IAuthManager {
    @PutMapping("/update")
    public ResponseEntity<Boolean> updateAuth(@RequestBody UpdateEmailOrUsernameRequestDto dto);

    @GetMapping("/find-by-role/{role}")
    public ResponseEntity<List<Long>> findByRole(@PathVariable String role);

    @PutMapping("/change-password")
    public ResponseEntity<Boolean> changePassword(@RequestBody ToAuthPasswordChangeDto dto);
}
