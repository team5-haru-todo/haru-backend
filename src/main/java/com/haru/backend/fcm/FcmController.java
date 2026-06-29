package com.haru.backend.fcm;


import com.haru.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/v1/fcm")
@RequiredArgsConstructor
public class FcmController {
    private final FcmService fcmService;

    @PostMapping("/test")
    //응답만 줘도 돼서 반환 타입 void 임
    public ApiResponse<Void> test(@RequestBody FcmTestRequest request){
        fcmService.sendMessage(request.token(),request.title(),request.body());
        return ApiResponse.ok();
    }
}
