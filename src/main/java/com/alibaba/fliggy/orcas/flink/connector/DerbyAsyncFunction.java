package com.alibaba.fliggy.orcas.flink.connector;

import com.alibaba.fliggy.orcas.flink.statejob.TempSensorRecord;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.executiongraph.ExecutionGraphCheckpointPlanCalculatorContext;
import org.apache.flink.streaming.api.functions.async.AsyncFunction;
import org.apache.flink.streaming.api.functions.async.ResultFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @description: DerbyAsyncFunction
 * @date: 2023/10/2 15:03
 * @author: dinglie
 */
public class DerbyAsyncFunction implements AsyncFunction<TempSensorRecord, Tuple2<String, String>> {
    private final ExecutorService executorService = Executors.newCachedThreadPool();


    @Override
    public void asyncInvoke(TempSensorRecord tempSensorRecord, ResultFuture<Tuple2<String, String>> resultFuture) throws Exception {

    }

    @Override
    public void timeout(TempSensorRecord input, ResultFuture<Tuple2<String, String>> resultFuture) throws Exception {
        AsyncFunction.super.timeout(input, resultFuture);
    }
}
