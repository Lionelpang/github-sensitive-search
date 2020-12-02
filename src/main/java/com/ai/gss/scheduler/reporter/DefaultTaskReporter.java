package com.ai.gss.scheduler.reporter;

import com.ai.gss.executor.Task;
import com.ai.gss.executor.TaskExecutorStatus;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author pangms
 * @date 2020/9/23
 */
public class DefaultTaskReporter implements Reporter {

    private LinkedBlockingQueue<TaskReportEntity> taskQueue = new LinkedBlockingQueue(1000);

    private ReentrantLock reentrantLock = new ReentrantLock();

    @Override
    public void report(Task task, String responseBody, TaskExecutorStatus status) {
        try {
            TaskReportEntity entity = new TaskReportEntity();
            entity.setTask(task);
            entity.withResponseBody(responseBody);
            entity.withStatus(status);
            this.taskQueue.put(entity);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public TaskReportEntity fetchReport() {
        try {
            return this.taskQueue.poll();
        }catch (IllegalMonitorStateException e){
            e.printStackTrace();
            //
            return fetchReport();
        }
    }
}
