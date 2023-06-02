package com.bilgeadam.service;

import com.bilgeadam.dto.response.UserProfileResponseDto;
import com.bilgeadam.exception.ErrorType;
import com.bilgeadam.exception.PostManagerException;
import com.bilgeadam.manager.IUserProfileManager;
import com.bilgeadam.repository.ILikeRepository;
import com.bilgeadam.repository.entity.Like;
import com.bilgeadam.repository.entity.Post;
import com.bilgeadam.utility.JwtTokenProvider;
import com.bilgeadam.utility.ServiceManager;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class LikeService  extends ServiceManager<Like, String> {

    private final ILikeRepository likeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final IUserProfileManager userProfileManager;

    public LikeService(ILikeRepository likeRepository, JwtTokenProvider jwtTokenProvider,
                       IUserProfileManager userProfileManager){
        super(likeRepository);
        this.likeRepository = likeRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userProfileManager = userProfileManager;
    }

    public Optional<Like> findByUseridAndPostid(String userid, String postid){
        return likeRepository.findByUseridAndPostid(userid, postid);
    }

    public void deleteByUseridAndPostid(String userid, String postid){
        likeRepository.deleteByUseridAndPostid(userid, postid);
    }


}
