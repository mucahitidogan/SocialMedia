package com.bilgeadam.controller;

import com.bilgeadam.dto.request.*;
import com.bilgeadam.dto.response.RegisterResponseDto;
import com.bilgeadam.repository.entity.Auth;
import com.bilgeadam.repository.entity.enums.ERole;
import com.bilgeadam.service.AuthService;
import com.bilgeadam.utility.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static  com.bilgeadam.constants.ApiUrls.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping(AUTH)
public class AuthController {
    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;

    @PostMapping(REGISTER)
    public ResponseEntity<RegisterResponseDto> register(@RequestBody @Valid RegisterRequestDto dto){
        return ResponseEntity.ok(authService.register(dto));
    }
    @PostMapping(ACTIVATE_STATUS)
    public ResponseEntity<Boolean> activateStatus(@RequestBody ActivateRequestDto dto){
        return ResponseEntity.ok(authService.activateStatus(dto));
    }
    @PostMapping(LOGIN)
    public ResponseEntity<String> login(LoginRequestDto dto){
        return ResponseEntity.ok(authService.login(dto));
    }

    @GetMapping(FIND_ALL)
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<List<Auth>> findAll(){
        return ResponseEntity.ok(authService.findAll());
    }

    @PutMapping(UPDATE)
    public ResponseEntity<Boolean> updateAuth(@RequestBody UpdateEmailOrUsernameRequestDto dto){
        return ResponseEntity.ok(authService.updateAuth(dto));
    }

    @DeleteMapping(DELETE_BY_ID)
    public ResponseEntity<Boolean> delete(String token){
        return ResponseEntity.ok(authService.delete(token));
    }
    @GetMapping("/create-token-with-id")
    public ResponseEntity<String> createToken(Long id){
        return ResponseEntity.ok(tokenProvider.createToken(id).get());
    }

    @GetMapping("/create-token-with-role")
    public ResponseEntity<String> createToken(Long id, ERole role){
        return ResponseEntity.ok(tokenProvider.createToken(id, role).get());
    }

    @GetMapping("/get-id-from-token")
    public ResponseEntity<Long> getIdFromToken(String token){
        return ResponseEntity.ok(tokenProvider.getIdFromToken(token).get());
    }

    @GetMapping("/get-role-from-token")
    public ResponseEntity<String> getRoleFromToken(String token){
        return ResponseEntity.ok(tokenProvider.getRoleFromToken(token).get());
    }

    @PostMapping(REGISTER+"rabbitmq")
    public ResponseEntity<RegisterResponseDto> registerWithRabbitMq(@RequestBody @Valid RegisterRequestDto dto){
        return ResponseEntity.ok(authService.registerWithRabbitMq(dto));
    }

    @GetMapping("/redis")
    @Cacheable(value = "redis-example")
    public String redisExample(String value){
        try {
            Thread.sleep(2000);
            return value;
        }catch (Exception e){
            throw new RuntimeException();
        }
    }

    @GetMapping("/cache-delete")
    @CacheEvict(cacheNames = "redis-example", allEntries = true)
    public void cacheDelete(){

    }

    @GetMapping("find-by-role/{role}")
    public ResponseEntity<List<Long>> findByrole(@PathVariable String role){
        return ResponseEntity.ok(authService.findByRole(role));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Boolean> changePassword(@RequestBody FromUserProfilePasswordChangeDto dto){
        return ResponseEntity.ok(authService.changePassword(dto));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Boolean> forgotPassword(@RequestBody String email, String username){
        return ResponseEntity.ok(authService.forgotPassword(email, username));
    }

}
