package com.tencent.wstt.gt.datasource.engine;

/**
 *采集目标可变的接口。
 * 比如pid，在测试过程中被测目标的进程号经常有改变的情况，如果想延续原来的采集任务，可以中途改变它。
 */
public interface TargetChangeable<T> {
    T getTarget();
    void setTarget(T t);
}
