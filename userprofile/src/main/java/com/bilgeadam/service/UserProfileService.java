package com.bilgeadam.service;

import com.bilgeadam.dto.request.ChangePasswordUserProfileRequestDto;
import com.bilgeadam.dto.request.NewCreateUserRequestDto;
import com.bilgeadam.dto.request.UpdateEmailOrUsernameRequestDto;
import com.bilgeadam.dto.request.UserProfileUpdateRequestDto;
import com.bilgeadam.dto.response.UserProfileChangePasswordResponseDto;
import com.bilgeadam.exception.ErrorType;
import com.bilgeadam.exception.UserManagerException;
import com.bilgeadam.manager.IAuthManager;
import com.bilgeadam.mapper.IUserProfileMapper;
import com.bilgeadam.rabbitmq.model.RegisterModel;
import com.bilgeadam.rabbitmq.producer.RegisterElasticProducer;
import com.bilgeadam.repository.IUserProfileRepository;
import com.bilgeadam.repository.enums.EStatus;
import com.bilgeadam.repository.entity.UserProfile;
import com.bilgeadam.utility.JwtTokenProvider;
import com.bilgeadam.utility.ServiceManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserProfileService extends ServiceManager<UserProfile, String> {

    private final IUserProfileRepository userProfileRepository;
    private final IAuthManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final CacheManager cacheManager;
    private final RegisterElasticProducer registerElasticProducer;
    private final PasswordEncoder passwordEncoder;

    public UserProfileService(IUserProfileRepository userProfileRepository, IAuthManager authManager,
                              JwtTokenProvider tokenProvider, CacheManager cacheManager,
                              RegisterElasticProducer registerElasticProducer, PasswordEncoder passwordEncoder){
        super(userProfileRepository);
        this.userProfileRepository = userProfileRepository;
        this.authManager = authManager;
        this.tokenProvider = tokenProvider;
        this.cacheManager = cacheManager;
        this.registerElasticProducer = registerElasticProducer;

        this.passwordEncoder = passwordEncoder;
    }

    @CacheEvict(value = "find-by-role", allEntries = true)
    public Boolean createUser(NewCreateUserRequestDto dto){
        try {
            save(IUserProfileMapper.INSTANCE.fromDtotoUserProfile(dto));
            //cacheclear
            cacheManager.getCache("findAll").clear();
            return true;
        }catch (Exception e){
            throw new RuntimeException("Beklenmeyen hata oluştu!");
        }
    }

    @Cacheable(value = "findAll")
    public List<UserProfile> findAll(){
        try {
            Thread.sleep(1000);
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
        return userProfileRepository.findAll();
    }

    public Boolean activateStatus(Long authId){
        Optional<UserProfile> userProfile = userProfileRepository.findOptionalByAuthid(authId);
        if(userProfile.isEmpty()){
            throw new RuntimeException("Auth id bulunamadı.");
        }
        userProfile.get().setStatus(EStatus.ACTIVE);
        update(userProfile.get());
        return true;
    }

    //cache delete yerine cache'i update etmeye yaramaktadır
    @CachePut(value = "find-by-username", key = "#dto.username.toLowerCase()") // oluşan değişiklikler sonucunda cache'in update edilmesini sağlar
    public UserProfile update(UserProfileUpdateRequestDto dto){
        Optional<Long> authid = tokenProvider.getIdFromToken(dto.getToken());
        if(authid.isEmpty()){
            throw new UserManagerException(ErrorType.INVALID_TOKEN);
        }
        Optional<UserProfile> optionalUserProfile = userProfileRepository.findOptionalByAuthid(authid.get());
        if(optionalUserProfile.isEmpty()){
            throw new UserManagerException(ErrorType.BAD_REQUEST);
        }
        //cachedelete
        //cacheManager.getCache("find-by-username").evict(optionalUserProfile.get().getUsername().toLowerCase());

        UpdateEmailOrUsernameRequestDto updateDto = IUserProfileMapper.INSTANCE.fromUpdateDtotoEmailUsernameDto(dto);
        updateDto.setAuthid(authid.get());
        UserProfile userProfile = userProfileRepository.save(IUserProfileMapper.INSTANCE.updateUserfromDto(dto, optionalUserProfile.get()));
        authManager.updateAuth(updateDto);
        return  userProfile;
    }

    public Boolean delete(Long authId){
        Optional<UserProfile> userProfile = userProfileRepository.findOptionalByAuthid(authId);
        if (userProfile.isEmpty()) {
            throw new UserManagerException(ErrorType.USER_NOT_FOUND);
        }
        userProfile.get().setStatus(EStatus.DELETED);
        update(userProfile.get());
        return true;
    }

    @CacheEvict(value = "find-by-username", key = "#model.username.toLowerCase()")
    public Boolean createUserWithRabbitMq(RegisterModel model){
        try {
            UserProfile userProfile = save(IUserProfileMapper.INSTANCE.fromRegisterModelToUser(model));
            registerElasticProducer.sendNewUser(IUserProfileMapper.INSTANCE.fromUserToElasticModel(userProfile));
            cacheManager.getCache("findAll").clear();
            return true;
        }catch (Exception e){
            throw new RuntimeException("Beklenmeyen hata oluştu!");
        }
    }

    @CacheEvict(value = "find-by-username", allEntries = true)
    public Boolean changePassword(ChangePasswordUserProfileRequestDto dto){
        Optional<Long> authid = tokenProvider.getIdFromToken(dto.getToken());
        if(authid.isEmpty())
            throw new UserManagerException(ErrorType.INVALID_TOKEN);
        Optional<UserProfile> optionalUserProfile = userProfileRepository.findOptionalByAuthid(authid.get());
        if(optionalUserProfile.isPresent()){
            if(passwordEncoder.matches(dto.getOldPassword(), optionalUserProfile.get().getPassword())){
                optionalUserProfile.get().setPassword(passwordEncoder.encode(dto.getNewPassword()));
                cacheManager.getCache("findAll").clear();
                userProfileRepository.save(optionalUserProfile.get());
                authManager.changePassword(IUserProfileMapper.INSTANCE.fromUserProfileToAuthChangePasswordDto(optionalUserProfile.get()));
                return true;
            }else{
                throw new UserManagerException(ErrorType.OLD_PASSWORD_NOT_CORRECT);
            }
        }else{
            throw new UserManagerException(ErrorType.USER_NOT_FOUND);
        }
    }

    @Cacheable(value = "find-by-username", key = "#username.toLowerCase()")
    public UserProfile findByUsername(String username){
        try {
            Thread.sleep(1000);
        }catch (Exception e){
            throw new RuntimeException();
        }
        Optional<UserProfile> optionalUserProfile = userProfileRepository.findOptionalByUsernameIgnoreCase(username);
        if (optionalUserProfile.isEmpty())
            throw new UserManagerException(ErrorType.USER_NOT_FOUND);
        return optionalUserProfile.get();
    }

    @Cacheable(value = "find-by-role", key = "#role.toUpperCase()")
    public List<UserProfile> findByRole(String role){
        try{
            Thread.sleep(2000);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        List<Long> authIds = authManager.findByRole(role).getBody();
        return authIds.stream().map(authId -> userProfileRepository.findOptionalByAuthid(authId)
                        .orElseThrow(() -> {throw new UserManagerException(ErrorType.USER_NOT_FOUND);}))
                .collect(Collectors.toList());
    }

    public Optional<UserProfile> findOptionalByAuthid(Long authid) {
        Optional<UserProfile> optionalUserProfile = userProfileRepository.findOptionalByAuthid(authid);
        if(optionalUserProfile.isEmpty())
            throw new UserManagerException(ErrorType.USER_NOT_FOUND);
        return optionalUserProfile;
    }


    public Boolean forgotPassword(UserProfileChangePasswordResponseDto dto) {
        Optional<UserProfile> optionalUserProfile = userProfileRepository.findOptionalByAuthid(dto.getAuthid());
        if(optionalUserProfile.isEmpty()){
            throw new UserManagerException(ErrorType.USER_NOT_FOUND);
        }
        optionalUserProfile.get().setPassword(dto.getPassword());
        update(optionalUserProfile.get());
        return true;
    }
}
