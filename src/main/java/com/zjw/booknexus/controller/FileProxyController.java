package com.zjw.booknexus.controller;

import com.zjw.booknexus.config.MinIOConfig;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaTypeFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

/**
 * 文件代理控制器，将 MinIO 中的文件通过后端接口代理返回给前端，
 * 解决浏览器无法直接访问 MinIO 私有桶或内网地址的问题。
 *
 * @author 张俊文
 * @since 2026-05-08
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/public/files")
@RequiredArgsConstructor
public class FileProxyController {

    private final MinioClient minioClient;
    private final MinIOConfig minIOConfig;

    @GetMapping("/**")
    public void getFile(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        int idx = uri.indexOf("/files/");
        String objectName = idx >= 0 ? uri.substring(idx + 7) : uri;
        if (objectName.isEmpty()) {
            response.setStatus(400);
            return;
        }

        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minIOConfig.getBucket())
                        .object(objectName)
                        .build())) {
            MediaTypeFactory.getMediaType(objectName)
                    .ifPresent(mt -> response.setContentType(mt.toString()));
            response.setHeader("Cache-Control", "public, max-age=86400");
            StreamUtils.copy(stream, response.getOutputStream());
        } catch (Exception e) {
            log.error("文件代理读取失败: {}", objectName, e);
            response.setStatus(404);
        }
    }

}
