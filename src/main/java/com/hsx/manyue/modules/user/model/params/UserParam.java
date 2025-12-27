package com.hsx.manyue.modules.user.model.params;

import com.hsx.manyue.common.enums.GenderEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 用户信息
 */
@Data
@Accessors(chain = true)
public class UserParam {

    /**
     * 管理端登录: 0
     * 用户端登录: 1
     */
    private String type;

    @NotNull(message = "不能为空", groups = Update.class)
    private Long id;

    @NotBlank(message = "不能为空", groups = {Signup.class, Login.class})
    private String email;

    @NotBlank(message = "不能为空", groups = {Signup.class, Login.class})
    private String password;

    @NotBlank(message = "不能为空", groups = Signup.class)
    @Pattern(regexp = "^\\d{6}$", message = "长度无效，必须纯数字", groups = Signup.class)
    private String captcha;

    @NotBlank(message = "不能为空", groups = {Signup.class, Update.class})
    private String nick;

    @NotNull(message = "不能为空", groups = Update.class)
    private GenderEnum gender;

    private String sign;

    private String birth;

    private MultipartFile avatarFile;


    /**
     * 注册分组
     */
    public interface Signup {
    }

    /**
     * 登录分组
     */
    public interface Login {
    }

    /**
     * 更新分组
     */
    public interface Update {
    }
}
