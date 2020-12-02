package com.ai.gss.executor;

import com.ai.gss.scheduler.GithubSensitiveSearchScheduler;
import com.ai.gss.scheduler.reporter.SchedulerReporter;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Response;

import java.util.Date;

/**
 * 任务执行器。
 * @author pangms
 * @date 2020/8/18
 */
public class TaskExecutor implements Runnable {
    private final static long TASK_DIV_START_TIME = 2 * 60 * 1000;
    private final static int TASK_RETRY_TIME = 5;
    // 自动回报接口，每次调度完成后上报消息。
    private SchedulerReporter reporter;

    private GithubClient client;

    private GithubSensitiveSearchScheduler scheduler;

    public TaskExecutor(GithubSensitiveSearchScheduler scheduler){
        this.scheduler = scheduler;
    }

    public TaskExecutor withClient(GithubClient client) {
        this.client = client;
        return this;
    }

    public TaskExecutor withReporter(SchedulerReporter reporter){
        this.reporter = reporter;
        return this;
    }

    @Override
    public void run() {
        Task task = null;
        while (true) {
            try {
                System.out.println("Thread id:" + Thread.currentThread().getId() + " will take the task:");
                task = this.scheduler.fetchTask();
                System.out.println("Thread id:" + Thread.currentThread().getId() + "|Task:" + task);

                if(task.getErrorCount() > TASK_RETRY_TIME && task.getLastErrorTime() != null
                        && System.currentTimeMillis() - task.getLastErrorTime().getTime() <= TASK_DIV_START_TIME){
                    this.reporter.report(task, "", null, TaskExecutorStatus.Error);
                } else {
                    task.resetErrorCount();
                    task.setLastErrorTime(null);
                }

                // if the github
                long taskForbiddenTime = System.currentTimeMillis() - task.token().getXRateLimitReset();
                if(taskForbiddenTime <= 0 && task.token().getXRateLimitRemaining() <= 1) {
                    // sleep wait for next rage
                    System.out.println(String.format("the token(%s) reset time(%s), limit($s) will sleep %s",
                            task.token().getToken(), String.valueOf(task.token().getXRateLimitReset())
                            , String.valueOf(task.token().getXRateLimitRemaining())
                            , String.valueOf(taskForbiddenTime + 100)));
                    // sleep a miniture
                    sleep();
                }

                Call call = client.call(task);
                Response response = call.execute();
                Headers headers = response.headers();
                response.code();
                String body = response.body().string();
                if(response.code() == 403){
                    this.reporter.report(task, body, headers, TaskExecutorStatus.Forbidden);
                } else if (response.isSuccessful()) {
                    this.reporter.report(task, body, headers, TaskExecutorStatus.Successe);
                } else {
                    sleep();
                    this.reporter.report(task, body, headers, TaskExecutorStatus.Error);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if(task.getLastErrorTime() != null) {
                    task.setLastErrorTime(new Date());
                }
                task.increaceErrorCount();

                try {
                    sleep();
                    this.reporter.report(task, e.getMessage(), null, TaskExecutorStatus.Error);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void sleep() throws InterruptedException {
        System.out.println("Thread is is " + Thread.currentThread().getId() + " will sleep one minute.");
        Thread.sleep(60*1000);
    }
}
