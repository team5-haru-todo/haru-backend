package com.haru.backend.auth.token;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class WidgetTokenReuseHandler {

    private final WidgetTokenRepository tokenRepository;

    // 별도 트랜잭션(REQUIRES_NEW): 바깥(rotate)이 롤백돼도 이 폐기는 이미 커밋되어 살아남는다.
    // public + 다른 클래스라서 프록시가 이 호출을 가로챌 수 있음 → REQUIRES_NEW가 실제로 작동.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeFamily(Long familyId) {
        List<WidgetToken> family = tokenRepository.findByFamilyId(familyId);
        family.forEach(WidgetToken::revoke);
    }
}
