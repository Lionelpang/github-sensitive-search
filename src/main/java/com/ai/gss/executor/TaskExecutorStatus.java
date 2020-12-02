package com.ai.gss.executor;

/**
 * 调度现成的状态
 * @author pangms
 * @date 2020/8/20
 */
public enum TaskExecutorStatus {
    Running("运行中", 1),
    Successe("空闲", 2),
    Error("异常推出", 3),
    Forbidden("服务端拒绝访问", 4),
    ;
    private String name;
    private int code;
    TaskExecutorStatus(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }
}
