package com.alibaba.fliggy.orcas.timeandwindow;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.windowing.windows.Window;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/9/17 13:27
 */
public class LocalTrigger extends Trigger<TempSensorRecord, TimeWindow> {
    @Override
    public TriggerResult onElement(TempSensorRecord tempSensorRecord, long l, TimeWindow timeWindow, TriggerContext triggerContext) throws Exception {
        ValueState<Boolean> firstSeen = triggerContext.getPartitionedState(new ValueStateDescriptor<Boolean>("firstSeen", Boolean.class));
        if (!firstSeen.value()) {
            long nextTs = triggerContext.getCurrentWatermark() + (1000 - triggerContext.getCurrentWatermark() % 1000);
            triggerContext.registerEventTimeTimer(nextTs);
            triggerContext.registerEventTimeTimer(timeWindow.getEnd());
            firstSeen.update(true);
        }
        return TriggerResult.CONTINUE;
    }

    @Override
    public TriggerResult onProcessingTime(long l, TimeWindow timeWindow, TriggerContext triggerContext) throws Exception {
        return TriggerResult.CONTINUE;
    }

    @Override
    public TriggerResult onEventTime(long l, TimeWindow timeWindow, TriggerContext triggerContext) throws Exception {
        if(l == timeWindow.getEnd()) {
            return TriggerResult.FIRE_AND_PURGE;
        }
        long l1 = triggerContext.getCurrentWatermark() + 1000 - triggerContext.getCurrentWatermark() % 1000;
        if (l1 < timeWindow.getEnd()) {
            triggerContext.registerEventTimeTimer(l1);
        }
        return TriggerResult.FIRE;
    }

    @Override
    public void clear(TimeWindow timeWindow, TriggerContext triggerContext) throws Exception {
        ValueState<Boolean> firstSeen = triggerContext.getPartitionedState(new ValueStateDescriptor<Boolean>("firstSeen", Boolean.class));
        firstSeen.clear();
    }
}
