package com.shipin.entity.po;

import com.shipin.entity.query.BaseParam;

public class Fans extends BaseParam {

    private Integer userId;
    private Integer attentionId;
    private Integer fanId;
    private Integer clicks;
    private Boolean isCreateTime;
    private Boolean isClicks;

    public Boolean getIsCreateTime() {
        return isCreateTime;
    }

    public void setIsCreateTime(Boolean createTime) {
        isCreateTime = createTime;
    }

    public void setIsClicks(Boolean clicks) {
        isClicks = clicks;
    }

    public Boolean getIsClicks() {
        return isClicks;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getClicks() {
        return clicks;
    }

    public void setClicks(Integer clicks) {
        this.clicks = clicks;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getAttentionId() {
        return attentionId;
    }

    public void setAttentionId(Integer attentionId) {
        this.attentionId = attentionId;
    }

    public Integer getFanId() {
        return fanId;
    }

    public void setFanId(Integer fanId) {
        this.fanId = fanId;
    }
}
