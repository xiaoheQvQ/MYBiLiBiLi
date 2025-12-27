package com.hsx.manyue.modules.notification;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/rtcs")
public class RtcController {

    private final UserManager userManager;

    @PostMapping("/sendMessage")
    public Boolean sendMessage(@RequestBody MessageReceive messageReceive) {
        log.info("转发消息给用户 {}, 类型: {}", messageReceive.getUserId(), messageReceive.getType());
        userManager.sendMessage(messageReceive);
        return true;
    }
}