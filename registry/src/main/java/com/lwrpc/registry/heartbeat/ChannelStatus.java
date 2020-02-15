package com.lwrpc.registry.heartbeat;

import lombok.Data;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class ChannelStatus {
    //重新活跃时间
    private volatile  long reActive;
    //是否获取
    private volatile boolean active;
    //持续重新活跃的次数
    private AtomicInteger reActiveCount = new AtomicInteger(0);
    //持续不活跃的次数
    private AtomicInteger inActiveCount = new AtomicInteger(0);
    //对应的channelId
    private String channelId;
    //开始计数时间
    private volatile long InActive;
    public ChannelStatus() {

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChannelStatus that = (ChannelStatus) obj;
        return reActive == that.reActive &&
                active == that.active &&
                InActive == that.InActive &&
                Objects.equals(reActiveCount, that.reActiveCount) &&
                Objects.equals(inActiveCount, that.inActiveCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.reActive,this.active, this.reActiveCount, this.inActiveCount, this.InActive);
    }
}


