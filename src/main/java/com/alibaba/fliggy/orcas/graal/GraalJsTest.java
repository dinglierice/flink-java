package com.alibaba.fliggy.orcas.graal;


import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;

import javax.script.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @description:
 * @date: 2023/9/27 10:05
 * @author: dinglie
 */
public class GraalJsTest {
    public static void main(String[] args) throws ScriptException, IOException, NoSuchMethodException {

        String jsDemo = FileUtils.readFileToString(new File("/Users/hollis/Desktop/work_space/flink-demo/src/main/resources/demo.js"));

        ScriptEngineManager manager = new ScriptEngineManager();
        // 这里用js 可以正常显示
        ScriptEngine engine = manager.getEngineByName("js");
        CompiledScript script = ((Compilable) engine).compile(jsDemo);
        engine.eval(jsDemo);
        Invocable fun = (Invocable) engine;
        System.out.println(JSON.toJSONString(fun.invokeFunction("test")));
        System.out.println(fun.invokeFunction("test").toString());
        System.out.println("graal done");

        // 改成用nashorn不可以
//        ScriptEngine nashorn = manager.getEngineByName("nashorn");
//        nashorn.eval(jsDemo);
//        Invocable funNashorn = (Invocable) engine;
//        System.out.println(JSON.toJSONString(funNashorn.invokeFunction("test")));

        new Thread(() -> {
            try {
                // Create ScriptEngine for this thread (with a shared polyglot Engine)
                ScriptEngine engine1 = manager.getEngineByName("js");
                script.eval(engine1.getContext());
            } catch (ScriptException scriptException) {
                scriptException.printStackTrace();
            }
        }).start();
        script.eval();
    }
}
