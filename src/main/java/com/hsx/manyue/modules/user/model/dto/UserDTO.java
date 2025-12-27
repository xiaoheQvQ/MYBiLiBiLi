package com.hsx.manyue.modules.user.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hsx.manyue.common.enums.GenderEnum;
import com.hsx.manyue.modules.apply.model.vo.MemberInfoVO;
import lombok.Data;


import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户信息
 */
@Data
public class UserDTO {

    private Long id;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 昵称
     */
    private String nick;

    private Long followerCount;

    private Long followingCount;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 个性签名
     */
    private String sign;

    /**
     * 性别，0女，1男，2-未知
     */
    private GenderEnum gender;

    /**
     * 生日
     */
    private LocalDate birth;

    private MemberInfoVO memberInfoVO;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
