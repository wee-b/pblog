package com.pblog.user.module.business.series.controller;

import com.pblog.common.domain.dto.SeriesDTO;
import com.pblog.common.domain.dto.SeriesPageQueryDTO;
import com.pblog.common.domain.result.PageResult;
import com.pblog.common.domain.result.ResponseResult;
import com.pblog.common.domain.vo.ArticleVO;
import com.pblog.common.domain.vo.SeriesDetailVO;
import com.pblog.common.domain.vo.SeriesVO;
import com.pblog.user.module.business.series.service.SeriesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/series")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;

    @PostMapping("/pageQuery")
    public ResponseResult<PageResult> pageQuery(@Valid @RequestBody SeriesPageQueryDTO queryDTO) {
        return ResponseResult.success(seriesService.pageQuery(queryDTO));
    }

    @GetMapping("/featured")
    public ResponseResult<List<SeriesVO>> featured(
            @RequestParam(defaultValue = "4") Integer limit) {
        return ResponseResult.success(seriesService.featured(limit));
    }

    @GetMapping("/queryById/{id}")
    public ResponseResult<SeriesDetailVO> queryById(@PathVariable Integer id) {
        return ResponseResult.success(seriesService.queryById(id));
    }

    @PostMapping("/mine/pageQuery")
    public ResponseResult<PageResult> myPageQuery(@Valid @RequestBody SeriesPageQueryDTO queryDTO) {
        return ResponseResult.success(seriesService.myPageQuery(queryDTO));
    }

    @GetMapping("/mine/queryById/{id}")
    public ResponseResult<SeriesDetailVO> queryMineById(@PathVariable Integer id) {
        return ResponseResult.success(seriesService.queryMineById(id));
    }

    @GetMapping("/candidateArticles")
    public ResponseResult<List<ArticleVO>> candidateArticles() {
        return ResponseResult.success(seriesService.candidateArticles());
    }

    @PostMapping("/insert")
    public ResponseResult<Integer> insert(@Valid @RequestBody SeriesDTO dto) {
        return ResponseResult.success(seriesService.insert(dto));
    }

    @PutMapping("/update/{id}")
    public ResponseResult<String> update(@PathVariable Integer id, @Valid @RequestBody SeriesDTO dto) {
        seriesService.update(id, dto);
        return ResponseResult.success("合集修改成功");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseResult<String> delete(@PathVariable Integer id) {
        seriesService.delete(id);
        return ResponseResult.success("合集删除成功");
    }
}
