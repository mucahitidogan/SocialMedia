package com.bilgeadam.manager;

import com.bilgeadam.dto.request.NewCreateUserRequestDto;
import com.bilgeadam.dto.request.UserProfileChangePasswordRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static com.bilgeadam.constants.ApiUrls.*;


@FeignClient(
        url = "http://localhost:8080/api/v1/user-profile",
        name = "auth-userprofile"
)
public interface IUserProfileManager {
    @PostMapping("/create")
    public ResponseEntity<Boolean> createUser(@RequestBody NewCreateUserRequestDto dto);

    @GetMapping("/activate-status/{authId}")
    public ResponseEntity<Boolean> activateStatus(@PathVariable Long authId);

    @DeleteMapping(DELETE_BY_ID + "/{authId}")
    public ResponseEntity<Boolean> delete(@PathVariable Long authId);

    @PutMapping("/forgot-password")
    public ResponseEntity<Boolean> forgotPassword(@RequestBody UserProfileChangePasswordRequestDto dto);

}
