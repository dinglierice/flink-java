package com.alibaba.fliggy.orcas.timeandwindow;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.aggregation.AggregationFunction;
import org.apache.flink.streaming.api.windowing.assigners.*;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.windows.Window;

import java.util.Arrays;
import java.util.Collection;

/**
 * @description：窗口算子
 * @author：dinglie
 * @date：2023/9/16 17:19
 */
public class TimeWindowJob {
    public static void main(String[] args) {
        // 初始化环境
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();

        // 温度流
        DataStreamSource<TempSensorRecord> tempRecordDataStreamSource = executionEnvironment.fromElements(new TempSensorRecord("123", 30d, 1682317312L),
                new TempSensorRecord("3", 83d, 1673732929L));

        // sensor流
        DataStreamSource<Tuple2<String, Long>> tuple2DataStreamSource = executionEnvironment.fromCollection(Arrays.asList(Tuple2.of("sensor_2", 10 * 1000L), Tuple2.of("sensor_7", 60 * 1000L)));

        // SimpleDemo：键值区分窗口算子 -> 先分配聚合 -> 再分配函数
        tempRecordDataStreamSource.keyBy(x -> x.getTemp()).window(new WindowAssigner<TempSensorRecord, Window>() {
            @Override
            public Collection<Window> assignWindows(TempSensorRecord tempSensorRecord, long l, WindowAssignerContext windowAssignerContext) {
                return null;
            }

            @Override
            public Trigger<TempSensorRecord, Window> getDefaultTrigger(StreamExecutionEnvironment streamExecutionEnvironment) {
                return null;
            }

            @Override
            public TypeSerializer<Window> getWindowSerializer(ExecutionConfig executionConfig) {
                return null;
            }

            @Override
            public boolean isEventTime() {
                return false;
            }
        }).reduce(new AggregationFunction<TempSensorRecord>() {
            @Override
            public TempSensorRecord reduce(TempSensorRecord tempSensorRecord, TempSensorRecord t1) throws Exception {
                return null;
            }
        });


        /**
         * 滚动窗口
         */
        // 按照事件时间排序 2参为偏移量
        tempRecordDataStreamSource.keyBy(TempSensorRecord::getDeviceId).window(TumblingEventTimeWindows.of(Time.seconds(1000), Time.seconds(1500))).aggregate(new TemperatureAverager<>());
        // 按照处理时间排序
        tempRecordDataStreamSource.keyBy(TempSensorRecord::getDeviceId).window(TumblingProcessingTimeWindows.of(Time.seconds(1000))).aggregate(new TemperatureAverager<>());

        /**
         * 滑动窗口
         */
        tempRecordDataStreamSource.keyBy(TempSensorRecord::getDeviceId).window(SlidingEventTimeWindows.of(Time.hours(1), Time.minutes(15))).aggregate(new TemperatureAverager<>());
        tempRecordDataStreamSource.keyBy(TempSensorRecord::getDeviceId).window(SlidingProcessingTimeWindows.of(Time.hours(1), Time.minutes(15))).aggregate(new TemperatureAverager<>());

        /**
         * 会话窗口
         */
        tempRecordDataStreamSource.keyBy(TempSensorRecord::getDeviceId).window(EventTimeSessionWindows.withGap(Time.hours(1))).aggregate(new TemperatureAverager<>());
        tempRecordDataStreamSource.keyBy(TempSensorRecord::getDeviceId).window(ProcessingTimeSessionWindows.withGap(Time.hours(1))).aggregate(new TemperatureAverager<>());

    }
}


