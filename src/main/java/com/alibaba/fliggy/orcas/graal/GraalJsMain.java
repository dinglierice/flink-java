package com.alibaba.fliggy.orcas.graal;

import com.alibaba.fliggy.orcas.graal.config.AbstractDiamondConfig;
import com.alibaba.fliggy.orcas.graal.config.DiamondFunctionConfigurationHolder;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptException;
import java.io.Serializable;

/**
 * @description: mock主程序入口
 * @date: 2023/10/8 15:50
 * @author: dinglie
 */
public class GraalJsMain implements Serializable {
    private static final AbstractDiamondConfig abstractDiamondConfig;

    static {
        abstractDiamondConfig = new DiamondFunctionConfigurationHolder();
        abstractDiamondConfig.init();
    }

    public String execute(String s) throws ScriptException, NoSuchMethodException {
        Invocable inv = abstractDiamondConfig.getInv();
        return String.valueOf(inv.invokeFunction("testAdd", s));
    }

    public static void main(String[] args) throws ScriptException, NoSuchMethodException {
        AbstractDiamondConfig abstractDiamondConfig = new DiamondFunctionConfigurationHolder();
        abstractDiamondConfig.init();

        Invocable inv = abstractDiamondConfig.getInv();
        System.out.println(inv.invokeFunction("test").toString());

        Integer i = 0;
        Integer testAdd =  (Integer) inv.invokeFunction("testAdd", i);
        System.out.println(testAdd);
    }
}
