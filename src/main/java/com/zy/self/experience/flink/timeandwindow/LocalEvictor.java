package com.zy.self.experience.flink.timeandwindow;

import org.apache.flink.streaming.api.windowing.evictors.Evictor;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.runtime.operators.windowing.TimestampedValue;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/9/17 13:53
 */
public class LocalEvictor implements Evictor<TempRecord, TimeWindow> {
    //选择性地删除元素。在窗 口函数之前调用
    @Override
    public void evictBefore(Iterable<TimestampedValue<TempRecord>> iterable, int i, TimeWindow timeWindow, EvictorContext evictorContext) {

    }

    //在函数调用之后
    @Override
    public void evictAfter(Iterable<TimestampedValue<TempRecord>> iterable, int i, TimeWindow timeWindow, EvictorContext evictorContext) {

    }
}
