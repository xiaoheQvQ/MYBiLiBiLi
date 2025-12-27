package com.hsx.manyue.modules.Live.controller;

import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.Live.model.dto.aLiveDTO;
import com.hsx.manyue.modules.Live.model.entity.aLiveEntity;
import com.hsx.manyue.modules.Live.service.aLiveSerive;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestController
@RequiredArgsConstructor
@RequestMapping("/live")
public class aLiveController {

    private final aLiveSerive aLiveSerive;

    // 1. 获取推流码，使用推流码开播，推送给粉丝
    @GetMapping("/getStream")
    public R getStream() {

        // 获取推流码
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        String streamKey = aLiveSerive.generateStreamKey(userId);

        // 推流码：  "rtmp://localhost/live/" + userId + "_" +  System.currentTimeMillis()

        // 使用推流码  （使用obs输入推流码，触发srs的开播回执，调用后端服务器的/publish，传入的参数是推流码）
        // 验证推流码的正确性，调用notifyFans，获取粉丝列表，给在线的粉丝推送直播通知


        return R.success(streamKey);
    }


    @PostMapping("/notify")
    public R notifyFans(@Param("nick") String nick) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();

        aLiveSerive.notifyFans(userId,nick);

        return R.success();
    }


    @PostMapping("/unpublish")
    public R unPublish( ) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();

        aLiveSerive.unPublish(userId);
        return R.success();
    }



    //3. 关播
    @PostMapping("/publish")
    public void onPublish( @RequestParam String stream) {

        System.out.println("开播");

        // 验证stream是否合法
        System.out.println("stream"+stream);

        // 更新直播间状态为"直播中"

    }

    @GetMapping("/livingList")
    public R getLivingStreams() {
        List<aLiveDTO> livingStreams = aLiveSerive.getLivingStreams();
        System.out.println("livingStreams"+livingStreams);
        return R.success(livingStreams);
    }
}
