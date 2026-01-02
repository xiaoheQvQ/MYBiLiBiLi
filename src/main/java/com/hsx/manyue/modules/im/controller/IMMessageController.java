package com.hsx.manyue.modules.im.controller;

import cn.hutool.extra.spring.SpringUtil;
import com.hsx.manyue.common.annotation.aspect.Login;
import com.hsx.manyue.common.controller.SuperController;
import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.im.model.dto.IMMessageDTO;
import com.hsx.manyue.modules.im.model.entity.IMMessageEntity;
import com.hsx.manyue.modules.im.service.IIMMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * IM消息控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/im/message")
@Tag(name = "IM消息接口")
public class IMMessageController extends SuperController {

    private final IIMMessageService messageService;

    @PostMapping("/send/single")
    @Operation(summary = "发送单聊消息")
    @Login
    public R sendSingleMessage(@RequestBody IMMessageDTO dto) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        dto.setFromUserId(userId);
        IMMessageEntity message = messageService.sendSingleMessage(dto);
        return success(message);
    }

    @PostMapping("/send/group")
    @Operation(summary = "发送群聊消息")
    @Login
    public R sendGroupMessage(@RequestBody IMMessageDTO dto) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        dto.setFromUserId(userId);
        IMMessageEntity message = messageService.sendGroupMessage(dto);
        return success(message);
    }

    @GetMapping("/history")
    @Operation(summary = "拉取历史消息")
    @Login
    public R pullHistory(@RequestParam Long targetId,
                         @RequestParam Integer sessionType,
                         @RequestParam(required = false) Long startSeq,
                         @RequestParam(defaultValue = "50") Integer limit) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        List<IMMessageEntity> messages = messageService.pullHistory(userId, targetId, sessionType, startSeq, limit);
        return success(messages);
    }

    @GetMapping("/unread/count")
    @Operation(summary = "获取未读消息数")
    @Login
    public R getUnreadCount() {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        Integer count = messageService.getUnreadCount(userId);
        return success(count);
    }

    @PostMapping("/sync")
    @Operation(summary = "同步消息")
    @Login
    public R syncMessages(@RequestParam Long lastSeq) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        List<IMMessageEntity> messages = messageService.syncMessages(userId, lastSeq);
        return success(messages);
    }

    @PostMapping("/uploadImage")
    @Operation(summary = "上传聊天图片")
    @Login
    public R uploadImage(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            // 使用OSS服务上传文件
            com.hsx.manyue.modules.oss.service.IOssService ossService = 
                SpringUtil.getBean(com.hsx.manyue.modules.oss.service.IOssService.class);
            String imageUrl = ossService.uploadFile(file);
            return success(imageUrl);
        } catch (Exception e) {
            return R.failure("图片上传失败: ");
        }
    }
}
