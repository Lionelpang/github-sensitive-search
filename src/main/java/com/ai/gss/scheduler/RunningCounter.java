package com.ai.gss.scheduler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author pangms
 * @date 2020/9/27
 */
public class RunningCounter {

    private AtomicInteger counter = new AtomicInteger();

    public int increase() {
        return counter.getAndIncrement();
    }

    public int decrease(){
        return counter.decrementAndGet();
    }

    public int get(){
        return this.counter.get();
    }
}
