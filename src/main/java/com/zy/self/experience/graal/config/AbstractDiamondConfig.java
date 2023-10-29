package com.zy.self.experience.graal.config;

import lombok.Getter;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * @description:
 * @date: 2023/10/8 15:50
 * @author: dinglie
 */
public abstract class AbstractDiamondConfig {
    protected static ScriptEngineManager scriptEngineManager;
    protected static ScriptEngine scriptEngine;

    @Getter
    protected Invocable inv;

    static {
        scriptEngineManager = new ScriptEngineManager();
        scriptEngine = scriptEngineManager.getEngineByName("js");
    }

    public abstract void init();
}
