package com.bilgeadam.repository;

import com.bilgeadam.repository.entity.UserProfile;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserProfileRepository extends ElasticsearchRepository<UserProfile, String> {

    Optional<UserProfile> findOptionalByAuthid(Long authid);
    Optional<UserProfile> findOptionalById(Long id);
    Optional<UserProfile> findOptionalByUsernameIgnoreCase(String username);

}
