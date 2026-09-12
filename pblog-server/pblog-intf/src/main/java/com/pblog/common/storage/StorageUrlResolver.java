package com.pblog.common.storage;

import com.pblog.common.domain.entity.User;
import com.pblog.common.domain.vo.ArticleVO;
import com.pblog.common.domain.vo.SeriesVO;
import com.pblog.common.domain.vo.UserAdminInfoVO;
import com.pblog.common.domain.vo.UserInfoVO;
import org.springframework.stereotype.Component;

@Component
public class StorageUrlResolver {

    private final FileAccessUrlBuilder fileAccessUrlBuilder;

    public StorageUrlResolver(FileAccessUrlBuilder fileAccessUrlBuilder) {
        this.fileAccessUrlBuilder = fileAccessUrlBuilder;
    }

    public String resolveAvatar(User user) {
        return user == null ? null : fileAccessUrlBuilder.build(user.getAvatarFileId());
    }

    public void resolveAvatar(UserInfoVO userInfo) {
        if (userInfo != null) {
            userInfo.setAvatarUrl(fileAccessUrlBuilder.build(userInfo.getAvatarFileId()));
        }
    }

    public void resolveAvatar(UserAdminInfoVO userInfo) {
        if (userInfo != null) {
            userInfo.setAvatarUrl(fileAccessUrlBuilder.build(userInfo.getAvatarFileId()));
        }
    }

    public void resolveArticle(ArticleVO article) {
        if (article != null) {
            article.setCoverImage(fileAccessUrlBuilder.build(article.getCoverFileId()));
        }
    }

    public void resolveSeries(SeriesVO series) {
        if (series != null) {
            series.setCoverImage(fileAccessUrlBuilder.build(series.getCoverFileId()));
        }
    }
}
