package com.pblog.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pblog.admin.query.commentPageQueryDTO;
import com.pblog.admin.vo.CommentDetailVO;
import com.pblog.common.domain.entity.Comment;
import com.pblog.common.domain.vo.CommentForMeVO;
import com.pblog.common.domain.vo.CommentFromMeVO;
import com.pblog.common.domain.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    IPage<CommentDetailVO> selectCommentPage(
            @Param("page") Page<CommentDetailVO> page,
            @Param("query") commentPageQueryDTO queryDTO
    );

    List<CommentVO> selectAllByArticleId(Integer articleId);

    List<CommentFromMeVO> selectCommentsFromMe(String username);

    List<CommentForMeVO> selectCommentsForMe(String username);
}
