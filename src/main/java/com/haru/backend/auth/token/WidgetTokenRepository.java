package com.haru.backend.auth.token;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WidgetTokenRepository extends JpaRepository<WidgetToken,Long> {
    List<WidgetToken> findByFamilyId(Long familyId);
    Optional<WidgetToken> findByTokenHash(String tokenHash);
    void deleteByTokenHash(String tokenHash);
}
