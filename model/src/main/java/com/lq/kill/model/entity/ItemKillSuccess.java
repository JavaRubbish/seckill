package com.lq.kill.model.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class  ItemKillSuccess {
    private String code;
    //商品id
    private Integer itemId;
    //待秒杀商品序号
    private Integer killId;

    private String userId;

    private Byte status;

    private Date createTime;

    private Integer differTime;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getKillId() {
        return killId;
    }

    public void setKillId(Integer killId) {
        this.killId = killId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getDifferTime() {
        return differTime;
    }

    public void setDifferTime(Integer differTime) {
        this.differTime = differTime;
    }
}
