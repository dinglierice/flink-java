import com.alibaba.fliggy.orcas.graal.GraalJsMain;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.junit.Test;

import java.util.Random;
/**
 * @description:
 * @date: 2023/10/8 16:39
 * @author: dinglie
 */
public class TestMultiThread {


    @Test
    public void testWithFlink() throws Exception {
        GraalJsMain graalJsMain = new GraalJsMain();

        StreamExecutionEnvironment streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> stringDataStreamSource = streamExecutionEnvironment.fromElements(
                "96","8","43","1","20","67","70","54","59","21","67","27","31","51","30","25","7","56","81","7","37","14","44","63","28","41","98","64","55","26","32","99","42","28","17","82","58","70","23","96","52","35","84","66","19","44","74","64","70","40","43","28","82","38","96","84","56","25","25","47","9","64","69","49","39","58","48","60","23","99","26","68","97","18","82","95","46","88","9","7","79","57","66","68","0","55","52","61","16","94","87","11","63","96","63","57","96","78","38","40");
        stringDataStreamSource.flatMap((FlatMapFunction<String, Object>) (s, collector) -> System.out.println("raw num:" + s + ", rev num is:" + graalJsMain.execute(s))).setParallelism(1);

        streamExecutionEnvironment.execute("test");
    }

    @Test
    public void makeNums() {

        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 100; i++) {
            int randomNumber = random.nextInt(100); // 生成0到99之间的随机数
            sb.append(randomNumber).append(",");
        }

        // 删除最后一个逗号
        sb.deleteCharAt(sb.length() - 1);

        String randomNumbersString = sb.toString();
        String[] numbers = randomNumbersString.split(",");
        for (String number : numbers) {
            System.out.println(number);
        }
    }
}
