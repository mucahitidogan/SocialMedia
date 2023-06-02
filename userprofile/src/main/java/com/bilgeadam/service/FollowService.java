package com.bilgeadam.service;

import com.bilgeadam.dto.request.CreateFollowRequestDto;
import com.bilgeadam.exception.ErrorType;
import com.bilgeadam.exception.UserManagerException;
import com.bilgeadam.mapper.IFollowMapper;
import com.bilgeadam.repository.IFollowRepository;
import com.bilgeadam.repository.entity.Follow;
import com.bilgeadam.repository.entity.UserProfile;
import com.bilgeadam.utility.JwtTokenProvider;
import com.bilgeadam.utility.ServiceManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FollowService extends ServiceManager<Follow, String> {

    private final IFollowRepository followRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserProfileService userProfileService;


    public FollowService(IFollowRepository followRepository, JwtTokenProvider jwtTokenProvider, UserProfileService userProfileService) {
        super(followRepository);
        this.followRepository = followRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userProfileService = userProfileService;
    }

    public Boolean createFollow(CreateFollowRequestDto dto){
        Optional<Long> authid = jwtTokenProvider.getIdFromToken(dto.getToken());
        if (authid.isEmpty())
            throw new UserManagerException(ErrorType.INVALID_TOKEN);
        Optional<UserProfile> optionalUserProfile = userProfileService.findOptionalByAuthid(authid.get());
        Optional<UserProfile> followUser = userProfileService.findById(dto.getFollowid());
        Optional<Follow> followDB = followRepository.findByUseridAndFollowid(optionalUserProfile.get().getId(), followUser.get().getId());
        if(followDB.isPresent())
            throw new UserManagerException(ErrorType.FOLLOW_ALREADY_EXIST);
        if(optionalUserProfile.isPresent() && followUser.isPresent()){
            if (optionalUserProfile.get().getId().equals(followUser.get().getId()))
                throw new UserManagerException(ErrorType.LOGIN_ERROR);
            Follow follow = IFollowMapper.INSTANCE.toFollow(dto.getFollowid(), optionalUserProfile.get().getId());
            save(follow);
            optionalUserProfile.get().getFollows().add(followUser.get().getId());
            followUser.get().getFollowers().add(optionalUserProfile.get().getId());
            userProfileService.update(optionalUserProfile.get());
            userProfileService.update(followUser.get());
            return true;
        }else {
            throw new UserManagerException(ErrorType.USER_NOT_FOUND);
        }
    }
}
