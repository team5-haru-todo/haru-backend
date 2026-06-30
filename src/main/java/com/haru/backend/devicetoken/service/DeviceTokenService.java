package com.haru.backend.devicetoken.service;

import com.haru.backend.devicetoken.DeviceToken;
import com.haru.backend.devicetoken.dto.DeviceTokenRequest;
import com.haru.backend.devicetoken.repository.DeviceTokenRepository;
import com.haru.backend.user.entity.User;
import com.haru.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
//저장 작업이니까, 
@Transactional
public class DeviceTokenService {
    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;
    //@LoginUser는 컨트롤러 입구에서만 사용하는 것이다. 왜?

    public void saveToken(UUID userId, DeviceTokenRequest request){
         User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
         DeviceToken deviceToken = DeviceToken.create(request.token(),user,request.platform());
         deviceTokenRepository.save(deviceToken);
    }
}
