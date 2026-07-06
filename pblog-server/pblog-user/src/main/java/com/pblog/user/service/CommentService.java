package com.pblog.user.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.pblog.common.domain.dto.PageQueryDTO;
import com.pblog.common.domain.entity.Comment;
import com.pblog.common.domain.dto.CommentDTO;
import com.pblog.common.domain.result.PageResult;
import com.pblog.common.domain.vo.CommentForMeVO;
import com.pblog.common.domain.vo.CommentFromMeVO;
import com.pblog.common.domain.vo.CommentVO;

import java.util.List;


public interface CommentService extends IService<Comment> {

	PageResult pageQuery(PageQueryDTO pageQueryDTO);
	
	List<CommentVO> all(Integer articleId);

    List<CommentFromMeVO> fromMe();

    List<CommentForMeVO> forMe();

    CommentVO queryById(Integer id);

    Integer insert(CommentDTO commentdto);

    boolean update(Integer id ,CommentDTO commentdto);
}

