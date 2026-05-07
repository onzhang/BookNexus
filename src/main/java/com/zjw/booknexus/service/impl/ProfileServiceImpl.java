package com.zjw.booknexus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjw.booknexus.common.ErrorCode;
import com.zjw.booknexus.config.MinIOConfig;
import com.zjw.booknexus.dto.ProfileUpdateReq;
import com.zjw.booknexus.entity.User;
import com.zjw.booknexus.exception.BusinessException;
import com.zjw.booknexus.mapper.UserMapper;
import com.zjw.booknexus.service.ProfileService;
import com.zjw.booknexus.vo.UserVO;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * 用户个人资料服务实现类，处理头像上传与资料更新业务逻辑。
 *
 * @author 张俊文
 * @since 2026-05-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserMapper userMapper;
    private final MinioClient minioClient;
    private final MinIOConfig minIOConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "上传文件不能为空");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, ErrorCode.USER_NOT_FOUND);
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.lastIndexOf('.') > 0) {
            ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        String objectName = "avatars/" + userId + "/" + UUID.randomUUID() + ext;

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minIOConfig.getBucket())
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            log.error("Avatar upload failed for user {}", userId, e);
            throw new BusinessException(500, "头像上传失败");
        }

        String endpoint = minIOConfig.getEndpoint();
        String avatarUrl = endpoint.endsWith("/") ? endpoint : endpoint + "/";
        avatarUrl += minIOConfig.getBucket() + "/" + objectName;

        user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);
        return avatarUrl;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateProfile(Long userId, ProfileUpdateReq req) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, ErrorCode.USER_NOT_FOUND);
        }

        if (!user.getUsername().equals(req.getUsername())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, req.getUsername());
            if (userMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(409, ErrorCode.USERNAME_EXISTS);
            }
        }

        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        userMapper.updateById(user);

        return toVO(user);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        return vo;
    }
}
