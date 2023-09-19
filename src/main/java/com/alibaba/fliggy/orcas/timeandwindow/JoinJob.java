package com.alibaba.fliggy.orcas.timeandwindow;

import org.apache.flink.api.common.functions.FlatJoinFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.CoGroupedStreams;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.windowing.evictors.Evictor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.runtime.operators.windowing.TimestampedValue;
import org.apache.flink.util.Collector;

import java.util.Arrays;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/9/18 23:16
 */
public class JoinJob {
    public static void main(String[] args) {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        // 温度流
        DataStreamSource<TempSensorRecord> tempRecordDataStreamSource = executionEnvironment.fromElements(new TempSensorRecord("123", 30d, 1682317312L),
                new TempSensorRecord("3", 83d, 1673732929L));

        // sensor流
        DataStreamSource<Tuple2<String, Long>> tuple2DataStreamSource = executionEnvironment.fromCollection(Arrays.asList(Tuple2.of("sensor_2", 10 * 1000L), Tuple2.of("sensor_7", 60 * 1000L)));

        tempRecordDataStreamSource.keyBy(tempRecordDataStreamSource.getId()).intervalJoin(tuple2DataStreamSource.keyBy(stringLongTuple2 -> Tuple1.of(stringLongTuple2.f0)))
                .between(Time.milliseconds(-2), Time.milliseconds(1))
                .process(new ProcessJoinFunction<TempSensorRecord, Tuple2<String, Long>, Object>() {
                    @Override
                    public void processElement(TempSensorRecord tempSensorRecord, Tuple2<String, Long> stringLongTuple2, ProcessJoinFunction<TempSensorRecord, Tuple2<String, Long>, Object>.Context context, Collector<Object> collector) throws Exception {

                    }
                });

        // 基于窗口的join
        // 其原理是将两条 输入流中的元素分配到公共窗口中并在窗口完成 进行 Join
        tempRecordDataStreamSource
                .join(tuple2DataStreamSource)
                .where(TempSensorRecord::getDeviceId)
                .equalTo(x -> x.f0)
                .window(new LocalWindowAssigner())
                .trigger(new Trigger<CoGroupedStreams.TaggedUnion<TempSensorRecord, Tuple2<String, Long>>, TimeWindow>() {
                    @Override
                    public TriggerResult onElement(CoGroupedStreams.TaggedUnion<TempSensorRecord, Tuple2<String, Long>> tempSensorRecordTuple2TaggedUnion, long l, TimeWindow timeWindow, TriggerContext triggerContext) throws Exception {
                        return null;
                    }

                    @Override
                    public TriggerResult onProcessingTime(long l, TimeWindow timeWindow, TriggerContext triggerContext) throws Exception {
                        return null;
                    }

                    @Override
                    public TriggerResult onEventTime(long l, TimeWindow timeWindow, TriggerContext triggerContext) throws Exception {
                        return null;
                    }

                    @Override
                    public void clear(TimeWindow timeWindow, TriggerContext triggerContext) throws Exception {

                    }
                })
                .evictor(new Evictor<CoGroupedStreams.TaggedUnion<TempSensorRecord, Tuple2<String, Long>>, TimeWindow>() {
                    @Override
                    public void evictBefore(Iterable<TimestampedValue<CoGroupedStreams.TaggedUnion<TempSensorRecord, Tuple2<String, Long>>>> iterable, int i, TimeWindow timeWindow, EvictorContext evictorContext) {

                    }

                    @Override
                    public void evictAfter(Iterable<TimestampedValue<CoGroupedStreams.TaggedUnion<TempSensorRecord, Tuple2<String, Long>>>> iterable, int i, TimeWindow timeWindow, EvictorContext evictorContext) {

                    }
                }).apply(new FlatJoinFunction<TempSensorRecord, Tuple2<String, Long>, Object>() {
                    @Override
                    public void join(TempSensorRecord tempSensorRecord, Tuple2<String, Long> stringLongTuple2, Collector<Object> collector) throws Exception {

                    }
                });


        // TODO GoGroupFunction 和 join的底层逻辑了解

    }
}
