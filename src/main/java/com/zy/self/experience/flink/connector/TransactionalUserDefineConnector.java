package com.zy.self.experience.flink.connector;

import lombok.SneakyThrows;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.sink.TwoPhaseCommitSinkFunction;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @description: 事务数据汇
 * @date: 2023/10/2 10:04
 * @author: dinglie
 */
public class TransactionalUserDefineConnector {
    public static void main(String[] args) {

    }
}


class TransactionalFileSink extends TwoPhaseCommitSinkFunction<Tuple2<String, Double>, String, Object> {

    private BufferedWriter bufferedWriter;
    public TransactionalFileSink(TypeSerializer<String> transactionSerializer, TypeSerializer<Object> contextSerializer) {
        super(transactionSerializer, contextSerializer);
    }

    /**
     * 将记录写入当前事务中
     */
    @Override
    protected void invoke(String s, Tuple2<String, Double> stringDoubleTuple2, Context context) throws Exception {
        bufferedWriter.write(stringDoubleTuple2.toString());
        bufferedWriter.write("\n");
    }

    /**
     * 开启事务, 返回事务标识符
     */
    @Override
    protected String beginTransaction() throws Exception {
        String timeNow = LocalTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        int indexOfThisSubtask = getRuntimeContext().getIndexOfThisSubtask();
        String transactionalFile = timeNow + "-" + String.valueOf(indexOfThisSubtask);

        Path path = Paths.get("~/user");
        Files.createFile(path);
        bufferedWriter = Files.newBufferedWriter(path);

        return transactionalFile;
    }

    /**
     * Unit 预提交事务。预提交过后的事务将不再接收 额外的数据写入。
     * @param s
     * @throws Exception
     */
    @Override
    protected void preCommit(String s) throws Exception {
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    /**
     * 提交事务
     * Unit 提交指定事务。该操作必须是幂等的，即在多次调用的情况下它不会将记录多次写入输出系统。
     * @param s
     */
    @SneakyThrows
    @Override
    protected void commit(String s) {
        Path tPath = Paths.get("~/user/" + s);
        if (Files.exists(tPath)) {
            Path cPath = Paths.get("~/user/" + s);
            Files.move(tPath, cPath);
        }
    }

    /**
     * 终止事务
     * @param s
     */
    @Override
    @SneakyThrows
    protected void abort(String s) {
        Path tPath = Paths.get("~/user/" + s);
        if (Files.exists(tPath)) {
            Files.delete(tPath);
        }
    }
}