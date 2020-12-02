package com.ai.gss.scheduler.reporter;

import com.ai.gss.executor.Task;
import com.ai.gss.executor.TaskExecutorStatus;

/**
 * @author pangms
 * @date 2020/9/23
 */
public class TaskReportEntity {
    private Task task;

    private String responseBody;

    private TaskExecutorStatus status;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void withResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public TaskExecutorStatus getStatus() {
        return status;
    }

    public void withStatus(TaskExecutorStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TaskReportEntity{" +
                "task=" + task +
                ", responseBody='" + responseBody + '\'' +
                '}';
    }
}
