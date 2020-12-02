package com.ai.gss.executor;

import com.ai.gss.scheduler.GithubSensitiveSearchScheduler;
import com.ai.gss.scheduler.reporter.SchedulerReporter;
import okhttp3.Headers;

/**
 * @author pangms
 * @date 2020/8/20
 */
public class TaskExecutorReport implements SchedulerReporter {
    private GithubSensitiveSearchScheduler scheduler;

    public TaskExecutorReport(GithubSensitiveSearchScheduler scheduler) {
        this.scheduler = scheduler;
    }
    @Override
    public void report(Task task, String responseBody, Headers headers, TaskExecutorStatus status) {

    }

}
