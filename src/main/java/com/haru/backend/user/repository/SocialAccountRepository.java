package com.haru.backend.user.repository;

import com.haru.backend.user.entity.SocialAccount;
import com.haru.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(String provider, String providerUserId);

    List<SocialAccount> findAllByUser(User user);
}