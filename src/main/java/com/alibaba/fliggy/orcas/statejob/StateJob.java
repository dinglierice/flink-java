package com.alibaba.fliggy.orcas.statejob;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/9/21 0:11
 */
public class StateJob {
    public static void main(String[] args) {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<TempSensorRecord> tempSensorRecordDataStreamSource = executionEnvironment.fromElements(new TempSensorRecord("1", 21d, 123382945L),
                new TempSensorRecord("2", 23d, 123382945L),
                new TempSensorRecord("2", 24d, 123382946L),
                new TempSensorRecord("3", 29d, 1254642452L)
        );

        KeyedStream<TempSensorRecord, String> tempSensorRecordStringKeyedStream = tempSensorRecordDataStreamSource.keyBy(TempSensorRecord::getDeviceId);

        tempSensorRecordStringKeyedStream.flatMap(new TemperatureAlertFunction(12d));
    }
}


class TemperatureAlertFunction extends RichFlatMapFunction<TempSensorRecord, Tuple3<String, Double, Double>> {
    private Double threshold;

    public TemperatureAlertFunction(Double threshold) {
        this.threshold = threshold;
    }

    private ValueState<Double> lastTempState;

    @Override
    public void open(Configuration parameters) throws Exception {
        ValueStateDescriptor<Double> lastTempStateDescriptor = new ValueStateDescriptor<>("Last Temperature", Double.class) ;
        lastTempState = getRuntimeContext().getState(lastTempStateDescriptor);
    }

    @Override
    public void flatMap(TempSensorRecord tempSensorRecord, Collector<Tuple3<String, Double, Double>> collector) throws Exception {
        // 从状态中获取上一次温度
        Double value = lastTempState.value();
        // 检查是否需要发出报警
        double abs = Math.abs(tempSensorRecord.getTemp() - value);
        if (abs > threshold) {
            collector.collect(Tuple3.of(tempSensorRecord.getDeviceId(), tempSensorRecord.getTemp(), abs));
        }
        lastTempState.update(tempSensorRecord.getTemp());
    }
}