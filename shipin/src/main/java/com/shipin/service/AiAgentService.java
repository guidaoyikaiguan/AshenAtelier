package com.shipin.service;

import com.shipin.entity.po.Comment;
import com.shipin.entity.po.Danmaku;
import com.shipin.entity.po.Video;

public interface AiAgentService {

    void moderateVideo(Video video);

    void moderateComment(Comment comment);

    void moderateDanmaku(Danmaku danmaku);

    void enrichVideoTags(Video video);

    String chatWithAi(Integer userId, String message);
}
