package com.pblog.common.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("pb_file")
public class StoredFile implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String provider;
    private String objectKey;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private String fileType;
    private String status;
    private Integer createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
