package com.ai.gss.scheduler.reporter;

import com.ai.gss.executor.Task;
import com.ai.gss.executor.TaskExecutorStatus;
import okhttp3.Headers;

/**
 * @author pangms
 * @date 2020/8/18
 */
public interface SchedulerReporter {

    void report(Task task, String responseBody, Headers headers, TaskExecutorStatus status);
}
