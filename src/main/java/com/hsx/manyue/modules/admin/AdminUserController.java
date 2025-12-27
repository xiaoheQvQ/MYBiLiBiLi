package com.hsx.manyue.modules.admin;

import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.modules.user.service.IUserService;
import com.hsx.manyue.modules.video.model.param.VideoQueryParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import static com.hsx.manyue.common.dto.R.success;

/**
 * 用户表 前端控制器
 */
@Tag(name = "视频历史记录管理")
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {
    @Resource
    private IUserService iUserService;

    @GetMapping("/page")
    @Operation(summary = "分页搜索用户列表")
    public R pageVideo(VideoQueryParam param) {

        System.out.println(iUserService.getPage(param)+"lll");
        return success(iUserService.getPage(param));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除用户数据")
    public R deleteVideo(@PathVariable String id) {
        return success(iUserService.removeById(id));
    }


}
