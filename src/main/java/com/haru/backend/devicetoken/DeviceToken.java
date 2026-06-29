package com.haru.backend.devicetoken;

import com.haru.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Table(name="device_tokens")
//JPA 는 엔티티를 만들 때 인자 없는 기본 생상자 반드시 필요
@NoArgsConstructor(access= AccessLevel.PROTECTED)
public class DeviceToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    // enum타입을 문자열로 저장해라 라는 의미이다 .
    //enum은 String으로 저장했다고해서 타입이 스트링이 아니다. enum 타입이다.
    @Enumerated(EnumType.STRING)
    private Platform platform;
    //여러 사용자 중 비어있을 수 없다.
    @ManyToOne(fetch = FetchType.LAZY)
    //DB 테이블에 user_id라는 왜래키를 만들어서 거기 유저 id를 저장
    @JoinColumn(name="user_id",nullable = false)
    private User user;

    //create() 파라미터로 받는가?
    public static DeviceToken create(String token, User user, Platform platform) {
        DeviceToken deviceToken = new DeviceToken(); //protected 생성자. 같은 클래스 안이라 호출 가능
        deviceToken.token = token;
        deviceToken.user = user;
        deviceToken.platform = platform;
        return deviceToken;
    }


}
