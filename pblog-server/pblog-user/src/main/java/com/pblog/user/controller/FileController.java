package com.pblog.user.controller;


import com.pblog.common.domain.result.ResponseResult;
import com.pblog.common.domain.vo.FileUploadVO;
import com.pblog.user.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

   @Autowired
   private FileService fileService;


   @PostMapping("/uploadAvatar")
   public ResponseResult<FileUploadVO> uploadAvatar(@RequestParam("file") MultipartFile file){
      FileUploadVO uploaded = fileService.uploadAvatar(file);
      log.info("/file/uploadAvatar fileId={}", uploaded.getId());
      return ResponseResult.successData(uploaded);
   }

   @PostMapping("/uploadImage")
   public ResponseResult<FileUploadVO> uploadImage(@RequestParam("file") MultipartFile file){
      FileUploadVO uploaded = fileService.uploadImage(file);
      log.info("/file/uploadImage fileId={}", uploaded.getId());
      return ResponseResult.successData(uploaded);
   }

   @GetMapping("/{fileId}")
   public ResponseEntity<Void> access(@PathVariable Long fileId){
      String url = fileService.getAccessUrl(fileId);
      return ResponseEntity.status(HttpStatus.FOUND)
              .header(HttpHeaders.LOCATION, URI.create(url).toASCIIString())
              .build();
   }
}
