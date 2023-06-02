package com.bilgeadam.service;

import com.bilgeadam.dto.request.*;
import com.bilgeadam.dto.response.ForgotPasswordMailResponseDto;
import com.bilgeadam.dto.response.RegisterResponseDto;
import com.bilgeadam.exception.AuthManagerException;
import com.bilgeadam.exception.ErrorType;
import com.bilgeadam.manager.IEmailManager;
import com.bilgeadam.manager.IUserProfileManager;
import com.bilgeadam.mapper.IAuthMapper;
import com.bilgeadam.rabbitmq.producer.RegisterMailProducer;
import com.bilgeadam.rabbitmq.producer.RegisterProducer;
import com.bilgeadam.repository.IAuthRepository;
import com.bilgeadam.repository.entity.Auth;
import com.bilgeadam.repository.entity.enums.ERole;
import com.bilgeadam.repository.entity.enums.EStatus;
import com.bilgeadam.utility.JwtTokenProvider;
import com.bilgeadam.utility.ServiceManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.bilgeadam.utility.CodeGenerator.generateCode;

@Service
public class AuthService extends ServiceManager<Auth, Long> {
    private final IAuthRepository authRepository;
    private final IUserProfileManager userManager;
    private final JwtTokenProvider tokenProvider;
    private final RegisterProducer registerProducer;
    private final RegisterMailProducer registerMailProducer;
    private final PasswordEncoder passwordEncoder;
    private final IEmailManager emailManager;


    public AuthService(JpaRepository<Auth, Long> repository, IAuthRepository authRepository, IUserProfileManager userManager,
                       JwtTokenProvider tokenProvider, RegisterProducer registerProducer, RegisterMailProducer registerMailProducer, PasswordEncoder passwordEncoder, IEmailManager emailManager) {
        super(repository);
        this.authRepository = authRepository;
        this.userManager = userManager;
        this.tokenProvider = tokenProvider;
        this.registerProducer = registerProducer;
        this.registerMailProducer = registerMailProducer;
        this.passwordEncoder = passwordEncoder;
        this.emailManager = emailManager;
    }

    public RegisterResponseDto register(RegisterRequestDto dto){
        Auth auth = IAuthMapper.INSTANCE.fromRequestDtoToAuth(dto);
        if (dto.getPassword().equals(dto.getRepassword())){
            auth.setActivationCode(generateCode());
            //auth.setPassword(MD5Encoding.md5(dto.getPassword()));
            auth.setPassword(passwordEncoder.encode(dto.getPassword()));
            save(auth);
            userManager.createUser(IAuthMapper.INSTANCE.fromAuthtoNewCreateUser(auth));
        }else {
            throw new AuthManagerException(ErrorType.PASSWORD_ERROR);
        }
        RegisterResponseDto responseDto = IAuthMapper.INSTANCE.fromAuthToResponseDto(auth);
        return responseDto;
    }

    public RegisterResponseDto registerWithRabbitMq(RegisterRequestDto dto){
        Auth auth = IAuthMapper.INSTANCE.fromRequestDtoToAuth(dto);
        if (dto.getPassword().equals(dto.getRepassword())){
            auth.setActivationCode(generateCode());
            auth.setPassword(passwordEncoder.encode(dto.getPassword()));
            save(auth);
            registerProducer.sendNewUser(IAuthMapper.INSTANCE.fromAuthToRegisterModel(auth));
            registerMailProducer.sendActivationCode(IAuthMapper.INSTANCE.fromAuthToRegisterMailModel(auth));
        }else {
            throw new AuthManagerException(ErrorType.PASSWORD_ERROR);
        }
        RegisterResponseDto responseDto = IAuthMapper.INSTANCE.fromAuthToResponseDto(auth);
        return responseDto;
    }

    public Boolean activateStatus(ActivateRequestDto dto){
        Optional<Auth> auth = findById(dto.getId());
        if (auth.isEmpty()){
            throw new AuthManagerException(ErrorType.USER_NOT_FOUND);
        }else if (auth.get().getActivationCode().equals(dto.getActivateCode())) {
            auth.get().setStatus(EStatus.ACTIVE);
            update(auth.get());
            return true;
        }
        throw new AuthManagerException(ErrorType.ACTIVATE_CODE_ERROR);
    }

    public String login(LoginRequestDto dto){
        Optional<Auth> auth = authRepository.findOptionalByUsername(dto.getUsername());
        if (auth.isEmpty() || !passwordEncoder.matches(dto.getPassword(), auth.get().getPassword())){
            throw new AuthManagerException(ErrorType.LOGIN_ERROR);
        } else if (!auth.get().getStatus().equals(EStatus.ACTIVE)) {
            throw new AuthManagerException(ErrorType.ACTIVATE_CODE_ERROR);
        }
        Optional<String> token = tokenProvider.createToken(auth.get().getId(), auth.get().getRole());
        return token.orElseThrow(() -> {throw new AuthManagerException(ErrorType.TOKEN_NOT_CREATED);});
    }

    public Boolean updateAuth(UpdateEmailOrUsernameRequestDto dto){
        Optional<Auth> auth = authRepository.findById(dto.getAuthid());
        if(auth.isEmpty()){
            throw new AuthManagerException(ErrorType.USER_NOT_FOUND);
        }
        IAuthMapper.INSTANCE.fromUpdateDtotoAuth(dto, auth.get());
        update(auth.get());
        return true;
    }

    public Boolean changePassword(FromUserProfilePasswordChangeDto dto){
        Optional<Auth> optionalAuth = authRepository.findById(dto.getAuthid());
        System.out.println(optionalAuth.get());
        if(optionalAuth.isEmpty())
            throw new AuthManagerException(ErrorType.USER_NOT_FOUND);
        optionalAuth.get().setPassword(dto.getPassword());
        authRepository.save(optionalAuth.get());
        return true;
    }

    public Boolean forgotPassword(String email, String username){
        Optional<Auth> optionalAuth = authRepository.findOptionalByEmail(email);
        if(optionalAuth.get().getStatus().equals(EStatus.ACTIVE)){
            if(optionalAuth.get().getUsername().equals(username)){
                String randomPassword = UUID.randomUUID().toString();
                optionalAuth.get().setPassword(passwordEncoder.encode(randomPassword));
                save(optionalAuth.get());
                ForgotPasswordMailResponseDto dto = ForgotPasswordMailResponseDto.builder()
                        .password(randomPassword)
                        .email(email)
                        .build();
                emailManager.forgotPasswordMail(dto);
                UserProfileChangePasswordRequestDto userProfileDto = UserProfileChangePasswordRequestDto.builder()
                        .authid(optionalAuth.get().getId())
                        .password(optionalAuth.get().getPassword())
                        .build();
                userManager.forgotPassword(userProfileDto);
                return true;
            }else{
                throw new AuthManagerException(ErrorType.USER_NOT_FOUND);
            }
        }else{
            if(optionalAuth.get().getStatus().equals(EStatus.DELETED))
                throw new AuthManagerException(ErrorType.USER_NOT_FOUND);
            throw new AuthManagerException(ErrorType.ACTIVATE_CODE_ERROR);
        }


    }

    public Boolean delete(String token){
        Optional<Long> authId = tokenProvider.getIdFromToken(token);
        if (authId.isEmpty()){
            throw new AuthManagerException(ErrorType.INVALID_TOKEN);
        }
        Optional<Auth> auth = authRepository.findById(authId.get());
        if (auth.isEmpty()){
            throw new AuthManagerException(ErrorType.USER_NOT_FOUND);
        }
        auth.get().setStatus(EStatus.DELETED);
        update(auth.get());
        userManager.delete(authId.get());
        return true;
    }

    public List<Long> findByRole(String role) {
        ERole roles = ERole.valueOf(role.toUpperCase(Locale.ENGLISH));
        List<Long> authIds = authRepository.findByRole(roles).stream().map(x -> x.getId()).collect(Collectors.toList());
        return authIds;
    }
}
