package com.alibaba.fliggy.orcas;

import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.*;

/**
 * @description: reduce示例
 * @date: 2023/9/10 16:20
 * @author: dinglie
 */
public class WordCountReduce {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        Tuple2<String, List<String>> tuple1 = new Tuple2<>();
        tuple1.f0 = "en";
        tuple1.f1 = Collections.singletonList("tea");

        Tuple2<String, List<String>> tuple2 = new Tuple2<>();
        tuple2.f0 = "fr";
        tuple2.f1 = Collections.singletonList("cake");

        Tuple2<String, List<String>> tuple3 = new Tuple2<>();
        tuple3.f0 = "en";
        tuple3.f1 = Collections.singletonList("cake");

        DataStreamSource<Tuple2<String, List<String>>> tuple2DataStreamSource = executionEnvironment.fromElements(tuple1, tuple2, tuple3);

        tuple2DataStreamSource
                .keyBy(0)
                .reduce((x, y) -> {
                    x.f1.addAll(y.f1);
                    return new Tuple2<>(x.f0, x.f1);
                });

        executionEnvironment.execute();
    }
}
