package com.haru.backend.devicetoken.service;

import com.haru.backend.devicetoken.DeviceToken;
import com.haru.backend.devicetoken.dto.DeviceTokenRequest;
import com.haru.backend.devicetoken.repository.DeviceTokenRepository;
import com.haru.backend.global.exception.BusinessException;
import com.haru.backend.global.exception.ErrorCode;
import com.haru.backend.user.entity.User;
import com.haru.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
//저장 작업이니까, 
@Transactional
public class DeviceTokenService {
    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;
    //@LoginUser는 컨트롤러 입구에서만 사용하는 것이다. 왜?

    public void saveToken(UUID userId, DeviceTokenRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 앱은 같은 FCM 토큰을 여러 번 보낼 수 있어서, 이미 있으면 새로 만들지 않고 다시 활성화한다.
        deviceTokenRepository.findByToken(request.token())
                .ifPresentOrElse(
                        existingToken -> existingToken.reactivate(user, request.platform()),
                        () -> deviceTokenRepository.save(
                                DeviceToken.create(request.token(), user, request.platform())
                        )
                );
    }
}
