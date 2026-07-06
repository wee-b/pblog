package com.pblog.user.module.business.like.task;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pblog.common.constant.RedisConstants;
import com.pblog.common.constant.TypeConstant;
import com.pblog.common.domain.dto.LikeDTO;
import com.pblog.common.domain.entity.Like;
import com.pblog.common.domain.entity.LikeCount;
import com.pblog.user.module.business.like.mapper.LikeCountMapper;
import com.pblog.user.module.business.like.mapper.LikeMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * 点赞数据定时同步任务
 * 替代原 RocketMQ 延迟消息，定期将 Redis 中的点赞状态同步到数据库
 */
@Slf4j
@Component
public class LikeSyncTask {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private LikeMapper likeMapper;

    @Resource
    private LikeCountMapper likeCountMapper;

    /**
     * 每60秒执行一次，将待同步的点赞数据写入数据库
     */
    @Scheduled(fixedRate = 60000)
    public void syncLikesToDatabase() {
        Set<String> pendingItems = stringRedisTemplate.opsForSet().members(RedisConstants.LIKE_PENDING_SET);
        if (pendingItems == null || pendingItems.isEmpty()) {
            return;
        }

        for (String item : pendingItems) {
            try {
                LikeDTO dto = JSON.parseObject(item, LikeDTO.class);
                String username = dto.getUsername();
                Integer targetId = dto.getTargetId();
                String targetType = dto.getTargetType();
                String operateType = dto.getOperateType();

                // 检查唯一Key的时间戳是否仍然匹配（防抖：如果用户再次操作，时间戳会更新）
                String uniqueKey = RedisConstants.LIKE_UNIQUE_KEY_PREFIX + username + ":" + targetType + ":" + targetId;
                String latestTimeStr = stringRedisTemplate.opsForValue().get(uniqueKey);
                if (latestTimeStr == null) {
                    // TTL 过期，直接移除
                    stringRedisTemplate.opsForSet().remove(RedisConstants.LIKE_PENDING_SET, item);
                    continue;
                }

                if (!latestTimeStr.equals(dto.getTimeStrap())) {
                    // 不是最新操作，忽略（用户后续有更新的操作在队列中）
                    stringRedisTemplate.opsForSet().remove(RedisConstants.LIKE_PENDING_SET, item);
                    continue;
                }

                // 同步到数据库
                syncLikeRecordToDb(username, targetId, targetType, operateType);
                syncLikeCountToDb(targetId, targetType);

                // 从待同步集合中移除
                stringRedisTemplate.opsForSet().remove(RedisConstants.LIKE_PENDING_SET, item);

                log.info("定时同步点赞成功，用户：{}，目标：{}:{}，操作：{}", username, targetType, targetId, operateType);

            } catch (Exception e) {
                log.error("定时同步点赞失败，数据：{}", item, e);
                // 不移除，下次重试
            }
        }
    }

    /**
     * 同步点赞/取消点赞记录到数据库
     */
    private void syncLikeRecordToDb(String username, Integer targetId, String targetType, String operateType) {
        LambdaQueryWrapper<Like> queryWrapper = new LambdaQueryWrapper<Like>()
                .eq(Like::getUsername, username)
                .eq(Like::getTargetId, targetId)
                .eq(Like::getTargetType, targetType);

        if (TypeConstant.LikeType.equals(operateType)) {
            if (likeMapper.selectCount(queryWrapper) == 0) {
                Like likeEntity = new Like();
                likeEntity.setUsername(username);
                likeEntity.setTargetId(targetId);
                likeEntity.setTargetType(targetType);
                likeMapper.insert(likeEntity);
                log.info("新增点赞记录：{}:{}:{}", username, targetType, targetId);
            }
        } else if (TypeConstant.UnlikeType.equals(operateType)) {
            int deleteCount = likeMapper.delete(queryWrapper);
            if (deleteCount > 0) {
                log.info("删除点赞记录：{}:{}:{}", username, targetType, targetId);
            }
        }
    }

    /**
     * 同步点赞总数到数据库
     */
    private void syncLikeCountToDb(Integer targetId, String targetType) {
        String countKey = RedisConstants.buildCountKey(targetType, targetId);
        String countStr = stringRedisTemplate.opsForValue().get(countKey);
        Integer total = countStr == null ? 0 : Integer.parseInt(countStr);

        LambdaQueryWrapper<LikeCount> countQueryWrapper = new LambdaQueryWrapper<LikeCount>()
                .eq(LikeCount::getTargetId, targetId)
                .eq(LikeCount::getTargetType, targetType);
        LikeCount existCount = likeCountMapper.selectOne(countQueryWrapper);

        if (existCount != null) {
            LambdaUpdateWrapper<LikeCount> updateWrapper = new LambdaUpdateWrapper<LikeCount>()
                    .eq(LikeCount::getTargetId, targetId)
                    .eq(LikeCount::getTargetType, targetType)
                    .set(LikeCount::getTotal, total);
            likeCountMapper.update(null, updateWrapper);
        } else {
            LikeCount likeCount = new LikeCount();
            likeCount.setTargetId(targetId);
            likeCount.setTargetType(targetType);
            likeCount.setTotal(total);
            likeCountMapper.insert(likeCount);
        }
    }
}
