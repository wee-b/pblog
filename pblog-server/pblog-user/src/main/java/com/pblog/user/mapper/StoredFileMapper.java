package com.pblog.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pblog.common.domain.entity.StoredFile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StoredFileMapper extends BaseMapper<StoredFile> {
}
