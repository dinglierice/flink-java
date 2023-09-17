package com.alibaba.fliggy.orcas.timeandwindow;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.WindowAssigner;
import org.apache.flink.streaming.api.windowing.triggers.EventTimeTrigger;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import java.util.Collection;
import java.util.Collections;

/**
 * @description：将件按照每30秒滚动窗口进行分组的自定义窗口
 * @author：dinglie
 * @date：2023/9/17 11:34
 */
public class LocalWindowAssigner extends WindowAssigner<Object, TimeWindow> {
    private long windowSize = 30 * 1000L;

    @Override
    public Collection<TimeWindow> assignWindows(Object o, long ts, WindowAssignerContext windowAssignerContext) {
        long startTime = ts - ts % windowSize;
        long endTime = startTime + windowSize;
        return Collections.singletonList(new TimeWindow(startTime, endTime));
    }

    @Override
    public Trigger<Object, TimeWindow> getDefaultTrigger(StreamExecutionEnvironment streamExecutionEnvironment) {
        return EventTimeTrigger.create();
    }

    @Override
    public TypeSerializer<TimeWindow> getWindowSerializer(ExecutionConfig executionConfig) {
        return new TimeWindow.Serializer();
    }

    @Override
    public boolean isEventTime() {
        return true;
    }
}
