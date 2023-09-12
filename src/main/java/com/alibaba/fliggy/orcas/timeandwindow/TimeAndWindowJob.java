package com.alibaba.fliggy.orcas.timeandwindow;

import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.operators.TimestampedCollector;
import org.apache.flink.streaming.api.windowing.time.Time;

import javax.annotation.Nullable;
import java.time.Duration;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/9/11 23:46
 */
public class TimeAndWindowJob {
    public static void main(String[] args) {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<Integer> integerDataStreamSource = executionEnvironment.fromElements(1, 2, 3, 45);
        // 自定义时间和水位线策略
        integerDataStreamSource.assignTimestampsAndWatermarks(new WatermarkStrategy<Integer>() {
            @Override
            public WatermarkGenerator<Integer> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
                return null;
            }
        });

        // 直接采用默认策略
        DataStreamSource<Tuple2<Long, Integer>> integerDataStreamSource2 = executionEnvironment
                .fromElements(Tuple2.of(16944482440000L, 1)
                , Tuple2.of(16944482440010L, 2)
                , Tuple2.of(16944482440020L, 3));

        WatermarkStrategy<Tuple2<Long, Integer>> tuple2WatermarkStrategy = WatermarkStrategy
                .<Tuple2<Long, Integer>>forBoundedOutOfOrderness(Duration.ofSeconds(20))
                .withTimestampAssigner((event, timestamp) -> event.f0)
                .withIdleness(Duration.ofSeconds(1))
                .withWatermarkAlignment("alignment-group-1", Duration.ofSeconds(20), Duration.ofSeconds(1));

        integerDataStreamSource2.assignTimestampsAndWatermarks(tuple2WatermarkStrategy);


        executionEnvironment.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        executionEnvironment.getConfig().setAutoWatermarkInterval(5000);


        // 两个常见的水位线分配器
        // 如果是单调递增
        integerDataStreamSource2.assignTimestampsAndWatermarks(new AscendingTimestampExtractor() {
            @Override
            public long extractAscendingTimestamp(Object o) {
                return 0;
            }
        });

        // 一般很难满足单调递增的情况，通常都会乱序。如果乱序，AscendingTimestampExtractor需要异常处理机制
        integerDataStreamSource2.assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<Tuple2<Long, Integer>>(Time.seconds(10)) {
            @Override
            public long extractTimestamp(Tuple2<Long, Integer> longIntegerTuple2) {
                return 0;
            }
        });

        // 定点水位分配器
        // 基于某些事件去做判断
        integerDataStreamSource2.assignTimestampsAndWatermarks(new AssignerWithPunctuatedWatermarks<Tuple2<Long, Integer>>() {
            private Long boundary = 60 * 1000L;

            @Nullable
            @Override
            public org.apache.flink.streaming.api.watermark.Watermark checkAndGetNextWatermark(Tuple2<Long, Integer> longIntegerTuple2, long l) {
                if (l == longIntegerTuple2.f0) {
                    return new org.apache.flink.streaming.api.watermark.Watermark(l - boundary);
                }
                return null;
            }

            @Override
            public long extractTimestamp(Tuple2<Long, Integer> longIntegerTuple2, long l) {
                return longIntegerTuple2.f1;
            }
        });
    }

    // WatermarkGenerators
    // A periodic generator usually observes the incoming events via onEvent() and then emits a watermark when the framework calls onPeriodicEmit().
    // A puncutated generator will look at events in onEvent() and wait for special marker events or punctuations that carry watermark information in the stream
    public static class BoundedOutOfOrdernessGenerator implements WatermarkGenerator<Long> {
        private final long maxOutOfOrderness = 3500; // 3.5 seconds

        private long currentMaxTimestamp;

        @Override
        public void onEvent(Long t, long l, WatermarkOutput watermarkOutput) {
            // If emit here , A Punctuated WatermarkGenerator
            currentMaxTimestamp = Math.max(currentMaxTimestamp, l);
        }

        @Override
        public void onPeriodicEmit(WatermarkOutput watermarkOutput) {
            // If emit here, A Periodic WatermarkGenerator
            watermarkOutput.emitWatermark(new Watermark(currentMaxTimestamp - maxOutOfOrderness - 1));
        }
    }

    public static class TimeLagWatermarkGenerator implements WatermarkGenerator<Long> {
        private final long maxTimeLag = 5000;
        @Override
        public void onEvent(Long aLong, long l, WatermarkOutput watermarkOutput) {

        }

        @Override
        public void onPeriodicEmit(WatermarkOutput watermarkOutput) {
            watermarkOutput.emitWatermark(new Watermark(System.currentTimeMillis() - maxTimeLag));
        }
    }

    public static class PeriodicAssigner implements AssignerWithPeriodicWatermarks<Long> {
        private Long bound = 60 * 1000L;
        private Long maxTs = Long.MIN_VALUE;
        @Nullable
        @Override
        public org.apache.flink.streaming.api.watermark.Watermark getCurrentWatermark() {
            return new org.apache.flink.streaming.api.watermark.Watermark(maxTs - bound);
        }

        @Override
        public long extractTimestamp(Long aLong, long l) {
            return 0;
        }
    }
}
