package com.hsx.manyue.modules.admin;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.model.entity.BaseEntity;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.common.utils.PageUtils;
import com.hsx.manyue.modules.user.model.entity.UserEntity;
import com.hsx.manyue.modules.user.service.IUserService;

import com.hsx.manyue.modules.video.model.dto.CommentDto;
import com.hsx.manyue.modules.video.model.dto.CommentPublishDto;
import com.hsx.manyue.modules.video.model.dto.CommentReplyDto;
import com.hsx.manyue.modules.video.model.dto.VideoCommentDTO;
import com.hsx.manyue.modules.video.model.entity.CommentEntity;

import com.hsx.manyue.modules.video.model.param.AdminVideoCommentQueryParam;
import com.hsx.manyue.modules.video.service.IVideoCommentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Operation;
import static com.hsx.manyue.common.dto.R.success;

/**
 * 评论表 前端控制器
 */
@RestController
@RequestMapping("/admin/comment")
public class AdminCommentController {
    @Resource
    private IVideoCommentService iVideoCommentService;

    @Resource
    private IUserService iUserService;

    @GetMapping("/page")
    @Operation(summary = "分页搜索评论列表")
    public R pageVideo(AdminVideoCommentQueryParam param) {
        List<UserEntity> userEntityList = Collections.emptyList();
        if (StrUtil.isNotBlank(param.getUserName())) {
            userEntityList = iUserService.list(Wrappers.lambdaQuery(UserEntity.class).like(UserEntity::getNick, param.getUserName()));
            if (CollUtil.isEmpty(userEntityList)) {
                return success(new PageDTO<>(param.getCurrent(), param.getSize()));
            }
        }
        Map<Long, String> userMap = userEntityList.stream().collect(Collectors.toMap(BaseEntity::getId, UserEntity::getNick));

        Page<CommentEntity> page = iVideoCommentService.page(new Page<>(param.getCurrent(), param.getSize()), Wrappers.lambdaQuery(CommentEntity.class)
                .like(StrUtil.isNotBlank(param.getContent()), CommentEntity::getContent, param.getContent())
                .in(CollUtil.isNotEmpty(userMap), CommentEntity::getUserId, new ArrayList<>(userMap.keySet())));
        if (Objects.isNull(page) || page.getSize() == 0) {
            return success(new PageDTO<>(param.getCurrent(), param.getSize()));
        }

        IPage<VideoCommentDTO> convert = PageUtils.convert(page, VideoCommentDTO.class);
        if (CollUtil.isNotEmpty(userMap)) {
            Map<Long, String> finalUserMap = userMap;
            convert.getRecords().forEach(CommentEntity -> {
                CommentEntity.setNick(finalUserMap.get(CommentEntity.getUserId()));
            });
        } else {
            userEntityList = iUserService.list(Wrappers.lambdaQuery(UserEntity.class));
            userMap = userEntityList.stream().collect(Collectors.toMap(BaseEntity::getId, UserEntity::getNick));
            Map<Long, String> finalUserMap1 = userMap;
            convert.getRecords().forEach(CommentEntity -> {
                CommentEntity.setNick(finalUserMap1.get(CommentEntity.getUserId()));
            });
        }
        return success(convert);
    }


}
