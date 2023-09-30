package com.alibaba.fliggy.orcas.flink.timeandwindow;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SideOutputDataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.Arrays;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/9/19 0:10
 */
public class CopeWithDelayDataJob {
    public static void main(String[] args) {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        // 温度流
        DataStreamSource<TempSensorRecord> tempRecordDataStreamSource = executionEnvironment.fromElements(new TempSensorRecord("123", 30d, 1682317312L),
                new TempSensorRecord("3", 83d, 1673732929L));

        // sensor流
        DataStreamSource<Tuple2<String, Long>> tuple2DataStreamSource = executionEnvironment.fromCollection(Arrays.asList(Tuple2.of("sensor_2", 10 * 1000L), Tuple2.of("sensor_7", 60 * 1000L)));

        SingleOutputStreamOperator<Object> process = tempRecordDataStreamSource
                .keyBy(TempSensorRecord::getDeviceId)
                .window(TumblingEventTimeWindows.of(Time.hours(1)))
                .sideOutputLateData(new OutputTag<>("late-readings"))
                .process(new ProcessWindowFunction<TempSensorRecord, Object, String, TimeWindow>() {
                    @Override
                    public void process(String s, ProcessWindowFunction<TempSensorRecord, Object, String, TimeWindow>.Context context, Iterable<TempSensorRecord> iterable, Collector<Object> collector) throws Exception {

                    }
                });
        SideOutputDataStream<Object> sideOutput = process.getSideOutput(new OutputTag<>("side-output"));
    }
}
