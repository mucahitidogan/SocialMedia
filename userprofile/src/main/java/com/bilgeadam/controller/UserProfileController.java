package com.bilgeadam.controller;

import com.bilgeadam.dto.request.ChangePasswordUserProfileRequestDto;
import com.bilgeadam.dto.request.NewCreateUserRequestDto;
import com.bilgeadam.dto.request.UserProfileUpdateRequestDto;
import com.bilgeadam.dto.response.UserProfileChangePasswordResponseDto;
import com.bilgeadam.repository.entity.UserProfile;
import com.bilgeadam.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static com.bilgeadam.constants.ApiUrls.*;

@RestController
@RequestMapping(USERSERVICE)
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;
    @PostMapping(CREATE)
    public ResponseEntity<Boolean> createUser(@RequestBody NewCreateUserRequestDto dto){
        return ResponseEntity.ok(userProfileService.createUser(dto));
    }

    @GetMapping(FIND_ALL)
    public ResponseEntity<List<UserProfile>> findAll(){
        return ResponseEntity.ok(userProfileService.findAll());
    }

    @GetMapping(ACTIVATE_STATUS + "/{authId}")
    public ResponseEntity<Boolean> activateStatus(@PathVariable Long authId){
        return ResponseEntity.ok(userProfileService.activateStatus(authId));
    }

    @PutMapping(UPDATE)
    public ResponseEntity<UserProfile> update(@RequestBody UserProfileUpdateRequestDto dto){
        return ResponseEntity.ok(userProfileService.update(dto));
    }

    @DeleteMapping(DELETE_BY_ID + "/{authId}")
    public ResponseEntity<Boolean> delete(@PathVariable Long authId){
        return ResponseEntity.ok(userProfileService.delete(authId));
    }
    @GetMapping("/find-by-username/{username}")
    public UserProfile findByUsername(String username){
        return userProfileService.findByUsername(username);
    }

    @GetMapping("/find-by-role/{role}")
    public ResponseEntity<List<UserProfile>> findByRole(@PathVariable String role){
        return ResponseEntity.ok(userProfileService.findByRole(role));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Boolean> changePassword(@RequestBody ChangePasswordUserProfileRequestDto dto){
        return ResponseEntity.ok(userProfileService.changePassword(dto));
    }

    @PutMapping("/forgot-password")
    public ResponseEntity<Boolean> forgotPassword(@RequestBody UserProfileChangePasswordResponseDto dto){
        return ResponseEntity.ok(userProfileService.forgotPassword(dto));
    }

    @GetMapping("/find-by-auth-id/{authid}")
    public ResponseEntity<Optional<UserProfile>> findByAuthid(@PathVariable Long authid){
        return ResponseEntity.ok(userProfileService.findOptionalByAuthid(authid));
    }
}
