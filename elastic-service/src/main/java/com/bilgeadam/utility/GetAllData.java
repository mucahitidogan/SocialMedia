package com.bilgeadam.utility;

import com.bilgeadam.manager.IUserManager;
import com.bilgeadam.repository.entity.UserProfile;
import com.bilgeadam.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetAllData {

    private final IUserManager userManager;
    private final UserProfileService userProfileService;

    //@PostConstruct
    public void initData(){
        List<UserProfile> userProfiles = userManager.findAll().getBody();
        userProfileService.saveAll(userProfiles);
    }
}
