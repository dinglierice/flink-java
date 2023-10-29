package com.zy.self.experience.flink.statejob;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @description：实现一个能清理状态的键值分区函数
 * 对于一个小时内都没有新的键值更新的数据予以清楚
 * @author：dinglie
 * @date：2023/9/23 18:32
 */
public class StateClearJob {
    public static void main(String[] args) {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<TempSensorRecord> tempSensorRecordDataStreamSource = executionEnvironment.fromElements(new TempSensorRecord("1", 21d, 123382945L),
                new TempSensorRecord("2", 23d, 123382945L),
                new TempSensorRecord("2", 24d, 123382946L),
                new TempSensorRecord("3", 29d, 1254642452L)
        );

        KeyedStream<TempSensorRecord, String> tempSensorRecordStringKeyedStream = tempSensorRecordDataStreamSource.keyBy(TempSensorRecord::getDeviceId);
        tempSensorRecordStringKeyedStream.process(new KeyedProcessFunction<String, TempSensorRecord, Tuple3<String, Double, Double>>() {
            private final Double threshold = 1000d;
            // 最近一次温度
            private ValueState<Double> lastTempState;
            private ValueState<Long> lastTimerState;

            @Override
            public void open(Configuration parameters) throws Exception {
                ValueStateDescriptor<Double> lastTempState1 = new ValueStateDescriptor<>("lastTempState", Double.class);
                lastTempState = getRuntimeContext().getState(lastTempState1);
                ValueStateDescriptor<Long> lastTimerState1 = new ValueStateDescriptor<>("lastTimerState", Long.class);
                lastTimerState = getRuntimeContext().getState(lastTimerState1);
            }

            @Override
            public void processElement(TempSensorRecord tempSensorRecord, KeyedProcessFunction<String, TempSensorRecord, Tuple3<String, Double, Double>>.Context context, Collector<Tuple3<String, Double, Double>> collector) throws Exception {
                long lateTs = context.timestamp() + 3600 * 1000;
                Long curTs = lastTimerState.value();
                context.timerService().deleteEventTimeTimer(curTs);
                context.timerService().registerProcessingTimeTimer(lateTs);
                lastTimerState.update(lateTs);

                Double value = lastTempState.value();
                double abs = Math.abs(value - tempSensorRecord.getTemp());
                if (abs > threshold) {
                    collector.collect(Tuple3.of(tempSensorRecord.getDeviceId(), tempSensorRecord.getTemp(), abs));
                }
                lastTempState.update(tempSensorRecord.getTemp());
            }

            @Override
            public void onTimer(long timestamp, KeyedProcessFunction<String, TempSensorRecord, Tuple3<String, Double, Double>>.OnTimerContext ctx, Collector<Tuple3<String, Double, Double>> out) throws Exception {
                lastTempState.clear();
                lastTimerState.clear();
            }
        });
    }
}
