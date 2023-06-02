package com.bilgeadam.controller;

import com.bilgeadam.dto.request.CreateFollowRequestDto;
import com.bilgeadam.repository.IFollowRepository;
import com.bilgeadam.repository.entity.Follow;
import com.bilgeadam.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.bilgeadam.constants.ApiUrls.*;

@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @GetMapping(FIND_ALL)
    public ResponseEntity<List<Follow>> findAll(){
        return ResponseEntity.ok(followService.findAll());
    }

    @PostMapping(CREATE)
    public ResponseEntity<Boolean> createFollow(@RequestBody CreateFollowRequestDto dto){
        return ResponseEntity.ok(followService.createFollow(dto));
    }
}
