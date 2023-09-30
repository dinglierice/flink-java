package com.alibaba.fliggy.orcas.flink;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.lang.reflect.Type;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/9/10 20:20
 */
public class DataStreamUnion {
    static final OutputTag<Object> outputTag = new OutputTag<Object>("side-output"){};
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<Integer> integerDataStreamSource1 = executionEnvironment.fromElements(1, 2, 3, 4, 5);
        DataStreamSource<Integer> integerDataStreamSource2 = executionEnvironment.fromElements(7, 4, 1, 3, 6);
        DataStreamSource<String> integerDataStreamSource3 = executionEnvironment.fromElements("7", "4", "1", "3", "6");

        // 将两条流合并为一条流
        // 结果是先进先出的一条新流
         DataStream<Integer> union = integerDataStreamSource1.union(integerDataStreamSource2);

        // 如果想要流之间有关联性操作
        ConnectedStreams<Integer, String> connect = integerDataStreamSource1.connect(integerDataStreamSource3);
        connect.map(new CoMapFunction<Integer, String, Object>() {
            @Override
            public Object map1(Integer integer) throws Exception {
                return null;
            }

            @Override
            public Object map2(String s) throws Exception {
                return null;
            }
        });

        connect.flatMap(new CoFlatMapFunction<Integer, String, Object>() {
            @Override
            public void flatMap1(Integer integer, Collector<Object> collector) throws Exception {

            }

            @Override
            public void flatMap2(String s, Collector<Object> collector) throws Exception {

            }
        });


        // 如果想要实现join的效果
        // 1、对两个联结后的数据流按键值分区
        DataStreamSource<Tuple2<Integer, Long>> tuple2DataStreamSource = executionEnvironment.fromElements(new Tuple2<Integer, Long>(1, 5L), new Tuple2<Integer, Long>(2, 6L));
        DataStreamSource<Tuple2<Integer, Long>> tuple2DataStreamSource1 = executionEnvironment.fromElements(new Tuple2<Integer, Long>(1, 6L), new Tuple2<Integer, Long>(2, 10L));
        ConnectedStreams<Tuple2<Integer, Long>, Tuple2<Integer, Long>> tuple2Tuple2ConnectedStreams = tuple2DataStreamSource.connect(tuple2DataStreamSource1).keyBy(0, 0);
        ConnectedStreams<Tuple2<Integer, Long>, Tuple2<Integer, Long>> connect1 = tuple2DataStreamSource.keyBy(0).connect(tuple2DataStreamSource1.keyBy(0));


        // 在flink1.13中 split and select 已经被删除了
        SingleOutputStreamOperator<Object> process = tuple2DataStreamSource.process(new ProcessFunction<Tuple2<Integer, Long>, Object>() {

            @Override
            public void processElement(Tuple2<Integer, Long> integerLongTuple2, ProcessFunction<Tuple2<Integer, Long>, Object>.Context context, Collector<Object> collector) throws Exception {
                collector.collect(integerLongTuple2);
                context.output(outputTag, "sideout-" + integerLongTuple2);
            }
        });

        SideOutputDataStream<Object> sideOutput = process.getSideOutput(outputTag);
        sideOutput.print();
        executionEnvironment.execute();

        // 设置并行度
        tuple2DataStreamSource.setParallelism(5);

        // 元组
        executionEnvironment.fromElements(Tuple2.of(1, 3), Tuple2.of(5, 6));


        // 指定类型信息
        integerDataStreamSource1.map((x) -> x + 1).returns(Types.INT);
        tuple2DataStreamSource.map((x) -> x).returns(Types.TUPLE(Types.INT, Types.STRING));

        // 指定键值
        // 可以指定属性
        tuple2DataStreamSource
                .keyBy(x -> x.f0)
                .keyBy(0)
                .keyBy("f0").keyBy(new KeySelector<Tuple2<Integer, Long>, Object>() {
            @Override
            public Object getKey(Tuple2<Integer, Long> integerLongTuple2) throws Exception {
                return null;
            }
        });

        // 富函数
        tuple2DataStreamSource.map(new RichMapFunction<Tuple2<Integer, Long>, Object>() {
            @Override
            public void open(Configuration parameters) throws Exception {
                RuntimeContext runtimeContext = getRuntimeContext();

                super.open(parameters);
            }

            @Override
            public void close() throws Exception {
                super.close();
            }

            @Override
            public Object map(Tuple2<Integer, Long> integerLongTuple2) throws Exception {
                return null;
            }
        });
    }
}
