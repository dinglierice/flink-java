package com.zy.self.experience.flink.timeandwindow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.aggregation.AggregationFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.*;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.util.Collector;

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


        /**
         * 函数: 计算每15秒的最低温度
         * reduce 类型不变，存在依赖
         */
        tempRecordDataStreamSource
                .map(x -> Tuple2.of(x.getDeviceId(), x.getTemp()))
                .keyBy(x -> x.f0)
                .window(TumblingEventTimeWindows.of(Time.minutes(15)))
                .reduce((x, y) -> Tuple2.of(x.f0, Math.min(x.f1, y.f1)));

        /**
         * aggregation函数, 中间类型可定，支持变换
         */
        tempRecordDataStreamSource
                .map(x -> Tuple2.of(x.getDeviceId(), x.getTemp()))
                .keyBy(x -> x.f0)
                .window(TumblingEventTimeWindows.of(Time.minutes(15)))
                // 输入类型 IN， 累 加器类 型 ACC 以 及结果类型 OUT
                .aggregate(new AggregateFunction<Tuple2<String, Double>, Tuple3<String, Double, Integer>, Tuple2<String, Double>>() {
                    @Override
                    public Tuple3<String, Double, Integer> createAccumulator() {
                        return Tuple3.of("", 0d, 0);
                    }

                    @Override
                    public Tuple3<String, Double, Integer> add(Tuple2<String, Double> stringDoubleTuple2, Tuple3<String, Double, Integer> stringDoubleIntegerTuple3) {
                        return Tuple3.of(stringDoubleTuple2.f0, stringDoubleTuple2.f1 + stringDoubleIntegerTuple3.f1, 1 + stringDoubleIntegerTuple3.f2 );
                    }

                    @Override
                    public Tuple2<String, Double> getResult(Tuple3<String, Double, Integer> stringDoubleIntegerTuple3) {
                        return Tuple2.of(stringDoubleIntegerTuple3.f0, stringDoubleIntegerTuple3.f1/stringDoubleIntegerTuple3.f2);
                    }

                    @Override
                    public Tuple3<String, Double, Integer> merge(Tuple3<String, Double, Integer> stringDoubleIntegerTuple3, Tuple3<String, Double, Integer> acc1) {
                        return Tuple3.of(stringDoubleIntegerTuple3.f0, stringDoubleIntegerTuple3.f1 + acc1.f1, stringDoubleIntegerTuple3.f2 + acc1.f2);
                    }
                });


        /**
         * ProcessWindowFunction 全量聚合
         * 计算每个传感器在每个窗口内的最低温和 最高温
         */
        tempRecordDataStreamSource.keyBy(TempSensorRecord::getDeviceId).window(TumblingEventTimeWindows.of(Time.minutes(15)))
                .process(new ProcessWindowFunction<TempSensorRecord, Object, String, TimeWindow>() {
                    @Override
                    public void process(String key, ProcessWindowFunction<TempSensorRecord, Object, String, TimeWindow>.Context context, Iterable<TempSensorRecord> iterable, Collector<Object> collector) throws Exception {
                        double min = Double.MAX_VALUE;
                        double max = Double.MIN_VALUE;
                        long windowEnd = context.window().getEnd();
                        for (TempSensorRecord tempSensorRecord : iterable) {
                            if (tempSensorRecord.getTemp() > max) {
                                max = tempSensorRecord.getTemp();
                            }
                            if (tempSensorRecord.getTemp() < min) {
                                min = tempSensorRecord.getTemp();
                            }
                        }
                        collector.collect(Tuple4.of(key, min, max, windowEnd));
                    }
                });


        /**
         * 很多情况下用于窗口的逻辑都可以表示为增量聚合
         * 只不过还需要访问 窗口的元数据或状态
         * ReduceFunction或AggregateFunction与功能更强的ProcessWindowFunction组合使用
         * 对分配给窗口的元素立即执行聚合，随后当窗口触发器触发时，再将聚合后的结果传给 ProcessWindowFunction
         * 在DataStreamAPI中，实现上述过程的途径是将 ProcessWindowFunction作为reduce()或 aggregate()方怯的第二个参数
         *
         *
         * 为每个传感器每5秒发出一次最高和最低温度以及窗口的结束时间戳 。
         */
        tempRecordDataStreamSource
                .map(x -> Tuple3.of(x.getDeviceId(), x.getTemp(), x.getTemp()))
                .keyBy(x -> x.f0)
                .window(TumblingEventTimeWindows.of(Time.minutes(15)))
                .reduce((x, y) -> Tuple3.of(x.f0, Math.min(x.f1, y.f1), Math.max(x.f2, y.f2)),
                        new ProcessWindowFunction<Tuple3<String, Double, Double>, MinMaxTemp, String, TimeWindow>() {
                            @Override
                            public void process(String s, ProcessWindowFunction<Tuple3<String, Double, Double>, MinMaxTemp, String, TimeWindow>.Context context, Iterable<Tuple3<String, Double, Double>> iterable, Collector<MinMaxTemp> collector) {
                                double min = Double.MAX_VALUE;
                                double max = Double.MIN_VALUE;
                                long windowEnd = context.window().getEnd();
                                for (Tuple3<String, Double, Double> tempSensorRecord : iterable) {
                                    if (tempSensorRecord.f2 > max) {
                                        max = tempSensorRecord.f2;
                                    }
                                    if (tempSensorRecord.f1 < min) {
                                        min = tempSensorRecord.f1;
                                    }
                                }
                                collector.collect(new MinMaxTemp(s, min, max, windowEnd));
                            }
                        });
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class MinMaxTemp {
        private String id;
        private Double min;
        private Double max;
        private Long endTs;
    }
}


