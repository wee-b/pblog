package com.pblog.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pblog.common.Expection.BusinessException;
import com.pblog.common.domain.entity.StoredFile;
import com.pblog.common.domain.entity.User;
import com.pblog.common.domain.vo.FileUploadVO;
import com.pblog.common.storage.FileAccessUrlBuilder;
import com.pblog.common.storage.ObjectStorageService;
import com.pblog.common.storage.StorageServiceRegistry;
import com.pblog.common.utils.SecurityContextUtil;
import com.pblog.user.mapper.StoredFileMapper;
import com.pblog.user.mapper.UserMapper;
import com.pblog.user.service.FileService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    // 头像存储的子目录
    private static final String AVATAR_DIR = "avatars/";
    // 文章图片存储的子目录
    private static final String ARTICLE_IMAGE_DIR = "article-images/";

    // 头像最大尺寸（5MB）
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;
    // 文章图片最大尺寸（10MB）
    private static final long MAX_ARTICLE_IMAGE_SIZE = 10 * 1024 * 1024;

    // 支持的图片格式
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/jpg", "image/gif"};

    // 文章图片最大宽度（超过则等比压缩）
    private static final int MAX_ARTICLE_IMAGE_WIDTH = 1920;
    // 文章图片压缩质量
    private static final float ARTICLE_IMAGE_QUALITY = 0.85f;

    @Autowired
    private StorageServiceRegistry storageRegistry;
    @Autowired
    private FileAccessUrlBuilder fileAccessUrlBuilder;
    @Autowired
    private StoredFileMapper storedFileMapper;
    @Autowired
    private UserMapper userMapper;


    @Override
    public FileUploadVO uploadImage(MultipartFile file) {
        try {
            // 1. 校验文章图片合法性
            validateArticleImageFile(file);

            // 2. 处理文章图片（仅压缩不裁剪，保持宽高比）
            InputStream processedStream = processArticleImage(file);

            // 3. 生成唯一文件名：article-images/用户ID/日期/UUID.后缀
            Integer userId = SecurityContextUtil.getUser().getId();
            String fileName = generateArticleImageFileName(file, userId);

            // 4. 上传到配置文件选中的对象存储
            ObjectStorageService storage = storageRegistry.getDefault();
            uploadToStorage(storage, fileName, processedStream, file.getContentType());

            // 5. 保存统一文件记录并返回稳定访问地址
            return saveFileRecord(storage, fileName, file, "article");

        } catch (Exception e) {
            log.error("文章图片上传失败", e);
            throw new BusinessException("文章图片上传失败：" + e.getMessage());
        }
    }


    @Override
    public FileUploadVO uploadAvatar(MultipartFile file) {
        try {
            // 1. 校验文件合法性
            validateImageFile(file);

            // 2. 处理图片（压缩、裁剪为200x200）
            InputStream processedStream = processAvatarImage(file);

            // 3. 生成唯一文件名（避免重复）：用户ID/日期/UUID.后缀
            Integer userId = SecurityContextUtil.getUser().getId();
            String fileName = generateAvatarFileName(userId);

            // 4. 上传到配置文件选中的对象存储
            ObjectStorageService storage = storageRegistry.getDefault();
            uploadToStorage(storage, fileName, processedStream, "image/jpeg");

            // TODO 删除原有的头像，或者最多保存9个历史头像

            // 5. 保存文件记录并更新用户头像引用
            FileUploadVO uploadedFile = saveFileRecord(storage, fileName, file, "avatar");
            try {
                LambdaUpdateWrapper<User> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                lambdaUpdateWrapper.eq(User::getId, userId)
                        .set(User::getAvatarFileId, uploadedFile.getId());
                userMapper.update(null, lambdaUpdateWrapper);
            } catch (RuntimeException e) {
                storedFileMapper.deleteById(uploadedFile.getId());
                try {
                    storage.delete(fileName);
                } catch (RuntimeException cleanupException) {
                    e.addSuppressed(cleanupException);
                }
                throw e;
            }

            return uploadedFile;

        } catch (Exception e) {
            log.error("头像上传失败", e);
            throw new BusinessException("头像上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传文件到启动时选中的对象存储
     */
    private void uploadToStorage(
            ObjectStorageService storage, String objectKey,
            InputStream inputStream, String contentType) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        storage.upload(objectKey, bytes, contentType);
    }

    /**
     * 校验头像文件（大小、格式、合法性）
     */
    private void validateImageFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new RuntimeException("图片大小不能超过5MB");
        }
        validateImageFileType(file);
        validateRealImage(file);
    }

    /**
     * 校验文章图片文件（更大的尺寸限制）
     */
    private void validateArticleImageFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }
        if (file.getSize() > MAX_ARTICLE_IMAGE_SIZE) {
            throw new RuntimeException("图片大小不能超过10MB");
        }
        validateImageFileType(file);
        validateRealImage(file);
    }

    /**
     * 通用校验图片文件类型
     */
    private void validateImageFileType(MultipartFile file) {
        String contentType = file.getContentType();
        boolean isAllowed = false;
        for (String type : ALLOWED_IMAGE_TYPES) {
            if (type.equals(contentType)) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) {
            throw new RuntimeException("仅支持JPG、PNG、GIF格式的图片");
        }
    }

    /**
     * 通用校验是否为真实图片
     */
    private void validateRealImage(MultipartFile file) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new RuntimeException("无效的图片文件");
        }
    }

    /**
     * 处理头像图片（压缩+裁剪为200x200）
     */
    private InputStream processAvatarImage(MultipartFile file) throws IOException {
        BufferedImage processedImage = Thumbnails.of(file.getInputStream())
                .size(200, 200)
                .keepAspectRatio(false)
                .outputQuality(0.8f)
                .asBufferedImage();
        return bufferedImageToInputStream(processedImage, "jpg");
    }

    /**
     * 处理文章图片（仅压缩不裁剪，保持宽高比）
     */
    private InputStream processArticleImage(MultipartFile file) throws IOException {
        if (file.isEmpty() || file.getInputStream() == null) {
            throw new IllegalArgumentException("上传的图片文件为空或输入流无效");
        }

        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("无法解析图片文件，请确认是有效图片格式（jpg/png/webp等）");
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        BufferedImage processedImage;
        if (originalWidth > MAX_ARTICLE_IMAGE_WIDTH) {
            processedImage = Thumbnails.of(originalImage)
                    .width(MAX_ARTICLE_IMAGE_WIDTH)
                    .keepAspectRatio(true)
                    .outputQuality(ARTICLE_IMAGE_QUALITY)
                    .asBufferedImage();
        } else {
            processedImage = Thumbnails.of(originalImage)
                    .size(originalWidth, originalHeight)
                    .keepAspectRatio(true)
                    .outputQuality(ARTICLE_IMAGE_QUALITY)
                    .asBufferedImage();
        }

        String suffix = getFileSuffix(file.getOriginalFilename());
        String format = suffix.startsWith(".") ? suffix.substring(1) : "jpg";
        if ("webp".equalsIgnoreCase(format)) {
            format = "png";
        }

        return bufferedImageToInputStream(processedImage, format);
    }

    /**
     * BufferedImage转InputStream
     */
    private InputStream bufferedImageToInputStream(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * 生成头像文件名（格式：avatars/用户ID/20241010/UUID.jpg）
     */
    private String generateAvatarFileName(Integer userId) {
        String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        // 头像处理流程固定编码为 JPEG，扩展名也必须与真实内容一致。
        return AVATAR_DIR + userId + "/" + dateDir + "/" + uuid + ".jpg";
    }

    /**
     * 生成文章图片文件名（格式：article-images/用户ID/20241010/UUID.后缀）
     */
    private String generateArticleImageFileName(MultipartFile file, Integer userId) {
        String suffix = getFileSuffix(file.getOriginalFilename());
        String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return ARTICLE_IMAGE_DIR + userId + "/" + dateDir + "/" + uuid + suffix;
    }

    /**
     * 获取文件后缀（默认jpg）
     */
    private String getFileSuffix(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".jpg";
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        return suffix.toLowerCase();
    }

    /**
     * 根据 objectKey 生成带时效的签名URL（供外部调用，如刷新头像链接）
     */
    @Override
    public String getAccessUrl(Long fileId) {
        StoredFile storedFile = storedFileMapper.selectById(fileId);
        if (storedFile == null || !"1".equals(storedFile.getStatus())) {
            throw new BusinessException("文件不存在");
        }
        return storageRegistry.get(storedFile.getProvider()).getUrl(storedFile.getObjectKey());
    }

    private FileUploadVO saveFileRecord(
            ObjectStorageService storage, String objectKey,
            MultipartFile source, String fileType) {
        StoredFile storedFile = new StoredFile();
        storedFile.setProvider(storage.getType());
        storedFile.setObjectKey(objectKey);
        storedFile.setOriginalName(source.getOriginalFilename());
        storedFile.setContentType(source.getContentType());
        storedFile.setFileSize(source.getSize());
        storedFile.setFileType(fileType);
        storedFile.setStatus("1");
        storedFile.setCreateBy(SecurityContextUtil.getUser().getId());
        storedFile.setCreateTime(LocalDateTime.now());
        try {
            storedFileMapper.insert(storedFile);
            return new FileUploadVO(
                    storedFile.getId(), fileAccessUrlBuilder.build(storedFile.getId()));
        } catch (RuntimeException e) {
            try {
                storage.delete(objectKey);
            } catch (RuntimeException cleanupException) {
                e.addSuppressed(cleanupException);
            }
            throw e;
        }
    }
}
