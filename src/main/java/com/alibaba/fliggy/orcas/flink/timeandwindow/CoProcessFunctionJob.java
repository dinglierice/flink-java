package com.alibaba.fliggy.orcas.flink.timeandwindow;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Arrays;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/9/15 0:00
 */
public class CoProcessFunctionJob {
    private static final String TAG = "a";
    public static void main(String[] args) {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<TempRecord> tempRecordDataStreamSource = executionEnvironment.fromElements(new TempRecord("123", 30d, 1682317312L),
                new TempRecord("3", 83d, 1673732929L));

        DataStreamSource<Tuple2<String, Long>> tuple2DataStreamSource = executionEnvironment.fromCollection(Arrays.asList(Tuple2.of("sensor_2", 10 * 1000L), Tuple2.of("sensor_7", 60 * 1000L)));


        // 基于过滤开 流对传感器读数流进行动态过滤
        SingleOutputStreamOperator<Object> process = tempRecordDataStreamSource.connect(tuple2DataStreamSource).keyBy(k -> k.getDeviceId(), v -> v.f0).process(new CoProcessFunction<TempRecord, Tuple2<String, Long>, Object>() {
            // 初始化一个开关
            ValueState<Boolean> filterSwitch = getRuntimeContext().getState(new ValueStateDescriptor<>("filterSwith", Boolean.class));
            // 用于保存当前活动的停止计时器的时间戳
            ValueState<Long> disableTimer = getRuntimeContext().getState(new ValueStateDescriptor<>("timer", Long.class));
            @Override
            public void processElement1(TempRecord tempRecord, CoProcessFunction<TempRecord, Tuple2<String, Long>, Object>.Context context, Collector<Object> collector) throws Exception {
                if(filterSwitch.value()) {
                    collector.collect(tempRecord);
                }
            }

            @Override
            public void processElement2(Tuple2<String, Long> stringLongTuple2, CoProcessFunction<TempRecord, Tuple2<String, Long>, Object>.Context context, Collector<Object> collector) throws Exception {
                // 开启读数转发
                filterSwitch.update(true);
                // 设置停止计时器
                long timerTimeStamp = context.timerService().currentProcessingTime() + stringLongTuple2.f1;
                Long value = disableTimer.value();
                if (timerTimeStamp > value) {
                    context.timerService().deleteEventTimeTimer(value);
                    context.timerService().registerProcessingTimeTimer(timerTimeStamp);
                    disableTimer.update(timerTimeStamp);
                }
            }

            @Override
            public void onTimer(long timestamp, CoProcessFunction<TempRecord, Tuple2<String, Long>, Object>.OnTimerContext ctx, Collector<Object> out) throws Exception {
                filterSwitch.clear();
                disableTimer.clear();
                super.onTimer(timestamp, ctx, out);
            }
        });


    }
}

@Data
@AllArgsConstructor
class TempRecord {

    private String deviceId;

    private Double temp;

    private Long eventTime;
}
