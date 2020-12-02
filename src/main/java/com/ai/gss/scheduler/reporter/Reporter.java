package com.ai.gss.scheduler.reporter;

import com.ai.gss.executor.Task;
import com.ai.gss.executor.TaskExecutorStatus;

/**
 * 上报完成记录
 * @author pangms
 * @date 2020/8/19
 */
public interface Reporter {

    void report(Task task, String responseBody , TaskExecutorStatus status);
}
