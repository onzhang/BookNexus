package com.zjw.booknexus.controller;

import com.zjw.booknexus.common.Result;
import com.zjw.booknexus.dto.ProfileUpdateReq;
import com.zjw.booknexus.service.ProfileService;
import com.zjw.booknexus.utils.UserContext;
import com.zjw.booknexus.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户个人资料控制器，提供当前登录用户头像上传与资料更新的 RESTful API。
 *
 * @author 张俊文
 * @since 2026-05-07
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 上传用户头像接口。
     * <p>
     * POST /api/v1/user/profile/avatar
     * 接收 multipart 图片文件，上传至 MinIO 并保存访问 URL 到用户资料。
     * </p>
     *
     * @param file 头像图片文件
     * @return 头像访问 URL
     */
    @PostMapping("/user/profile/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = profileService.uploadAvatar(UserContext.getUserId(), file);
        return Result.success(avatarUrl);
    }

    /**
     * 更新当前用户个人资料接口。
     * <p>
     * PUT /api/v1/user/profile
     * 更新当前登录用户的用户名、邮箱和手机号。
     * 修改用户名时会校验唯一性（排除自身）。
     * </p>
     *
     * @param req 资料更新请求体
     * @return 更新后的用户信息
     */
    @PutMapping("/user/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody ProfileUpdateReq req) {
        UserVO userVO = profileService.updateProfile(UserContext.getUserId(), req);
        return Result.success(userVO);
    }
}
