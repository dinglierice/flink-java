package com.zy.self.experience.flink.statejob;

import org.apache.flink.api.common.state.*;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;

/**
 * @description：联结的广播状态的具体实现
 * 支持在运行时动态配置传感器阈值
 * @author：dinglie
 * @date：2023/9/23 15:17
 */
public class JoinedFlow {
    public static void main(String[] args) {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        // 初始化两个事件
        DataStreamSource<TempSensorRecord> tempSensorRecordDataStreamSource = executionEnvironment.fromElements(new TempSensorRecord("1", 21d, 123382945L),
                new TempSensorRecord("2", 23d, 123382945L),
                new TempSensorRecord("2", 24d, 123382946L),
                new TempSensorRecord("3", 29d, 1254642452L)
        );

        DataStreamSource<ThresholdUpdate> thresholdUpdateDataStreamSource = executionEnvironment.fromElements(new ThresholdUpdate("1", 21d), new ThresholdUpdate("1", 21d));

        KeyedStream<TempSensorRecord, String> tempSensorRecordStringKeyedStream = tempSensorRecordDataStreamSource.keyBy(TempSensorRecord::getDeviceId);
        MapStateDescriptor<String, Double> broadcastStateDescriptor = new MapStateDescriptor<>("threshoulds", String.class, Double.class);

        BroadcastStream<ThresholdUpdate> broadcast = thresholdUpdateDataStreamSource.broadcast(broadcastStateDescriptor);

        // 联键值分区传感数据流和广播的规则流
        SingleOutputStreamOperator alert = tempSensorRecordStringKeyedStream.connect(broadcast).process(new UpdatableTemperatureAlertFunction());
    }
}

class UpdatableTemperatureAlertFunction extends KeyedBroadcastProcessFunction<String, TempSensorRecord, ThresholdUpdate, Tuple3<String, Double, Double>> {
    private static final MapStateDescriptor<String, Double> broadcastStateDescriptor = new MapStateDescriptor<>("threshoulds", String.class, Double.class);

    private ValueState<Double> lastTempState;

    @Override
    public void open(Configuration parameters) throws Exception {
        ValueStateDescriptor<Double> lastTemp = new ValueStateDescriptor<>("lastTemp", Double.class);
        lastTempState = getRuntimeContext().getState(lastTemp);
    }

    @Override
    public void processElement(TempSensorRecord tempSensorRecord, KeyedBroadcastProcessFunction<String, TempSensorRecord, ThresholdUpdate, Tuple3<String, Double, Double>>.ReadOnlyContext readOnlyContext, Collector<Tuple3<String, Double, Double>> collector) throws Exception {
        ReadOnlyBroadcastState<String, Double> broadcastState = readOnlyContext.getBroadcastState(broadcastStateDescriptor);
        if (broadcastState.contains(tempSensorRecord.getDeviceId())) {
            Double aDouble = broadcastState.get(tempSensorRecord.getDeviceId());
            Double value = lastTempState.value();
            double abs = Math.abs(value - aDouble);
            if (abs > aDouble) {
                collector.collect(Tuple3.of(tempSensorRecord.getDeviceId(), tempSensorRecord.getTemp(), abs));
            }
        }

    }

    @Override
    public void processBroadcastElement(ThresholdUpdate thresholdUpdate, KeyedBroadcastProcessFunction<String, TempSensorRecord, ThresholdUpdate, Tuple3<String, Double, Double>>.Context context, Collector<Tuple3<String, Double, Double>> collector) throws Exception {
        // 获取广播状态引用对象
        BroadcastState<String, Double> broadcastState = context.getBroadcastState(broadcastStateDescriptor);
        if(thresholdUpdate.getThreshold() != 0) {
            broadcastState.put(thresholdUpdate.getId(), thresholdUpdate.getThreshold());
        } else {
            broadcastState.remove(thresholdUpdate.getId());
        }
    }
}
