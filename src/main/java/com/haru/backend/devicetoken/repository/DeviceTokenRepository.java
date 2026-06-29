package com.haru.backend.devicetoken.repository;

import com.haru.backend.devicetoken.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    //토큰이 없을 수도 있기 때문에 Optional 로 하나 선언을 해놓겠다.
    Optional<DeviceToken> findByToken(String token);
}
