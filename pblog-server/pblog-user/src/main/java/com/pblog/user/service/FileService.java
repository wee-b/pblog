package com.pblog.user.service;

import com.pblog.common.domain.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileUploadVO uploadAvatar(MultipartFile file);

    FileUploadVO uploadImage(MultipartFile file);

    String getAccessUrl(Long fileId);

}
