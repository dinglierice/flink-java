package com.alibaba.fliggy.orcas;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.IterativeStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/9/10 10:30
 */
public class IterationJob {
    public static void main(String[] args) {
        StreamExecutionEnvironment streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<Long> someIntegers = streamExecutionEnvironment.generateSequence(0, 1000);

        IterativeStream<Long> iteration = someIntegers.iterate();
        DataStream<Long> minusOne = iteration.map(new MinusMap());

        DataStream<Long> stillGreaterThanZero = minusOne.filter((value)-> value > 0);

        iteration.closeWith(stillGreaterThanZero);
        DataStream<Long> lessThanZero = minusOne.filter(new FilterFunction<Long>() {
            @Override
            public boolean filter(Long value) throws Exception {
                return (value <= 0);
            }
        });
    }

    public static final class MinusMap implements MapFunction<Long, Long> {
        @Override
        public Long map(Long aLong) throws Exception {
            return aLong - 1;
        }
    }
}
