package com.zjw.booknexus.service;

import com.zjw.booknexus.dto.ProfileUpdateReq;
import com.zjw.booknexus.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户个人资料服务接口，定义当前用户头像上传与资料更新相关的业务逻辑。
 *
 * @author 张俊文
 * @since 2026-05-07
 */
public interface ProfileService {

    /**
     * 上传用户头像。
     * <p>
     * 将 multipart 文件上传至 MinIO 对象存储，返回可访问的 URL，
     * 并同步更新当前用户的 avatarUrl 字段。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param file   头像图片文件
     * @return 头像在 MinIO 上的访问 URL
     */
    String uploadAvatar(Long userId, MultipartFile file);

    /**
     * 更新当前用户个人资料。
     * <p>
     * 支持更新用户名、邮箱和手机号。
     * 若修改用户名，需校验新用户名是否已被其他用户占用。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param req    资料更新请求
     * @return 更新后的用户视图对象
     */
    UserVO updateProfile(Long userId, ProfileUpdateReq req);
}
