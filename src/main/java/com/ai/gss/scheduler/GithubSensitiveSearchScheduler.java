package com.ai.gss.scheduler;

import com.ai.gss.executor.GithubClient;
import com.ai.gss.executor.Task;
import com.ai.gss.executor.TaskExecutor;
import com.ai.gss.executor.TaskExecutorStatus;
import com.ai.gss.instance.YamlConfig;
import com.ai.gss.scheduler.reporter.Reporter;
import com.ai.gss.scheduler.reporter.SchedulerReporter;
import com.alibaba.fastjson.JSONObject;
import okhttp3.Headers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 调度器，负责整体行为的调度。
 *
 * @author pangms
 * @date 2020/8/18
 */
public class GithubSensitiveSearchScheduler implements SchedulerReporter {
    // token列表
    private List<GithubAccessToken> tokens = new ArrayList<>();
    // 索引数
    private int index = 0;

    private RunningCounter counter = new RunningCounter();

    // 任务队列
    // private LinkedBlockingQueue<Task> taskQueue = new LinkedBlockingQueue(1000, true);
    private LinkedBlockingQueue<Task> taskQueue = new LinkedBlockingQueue(1000);

    private int executorNum = 0;

    // 任务汇报
    private List<Reporter> reporters = new ArrayList<>();
    // 执行线程
    private List<Thread> executors = new ArrayList<>();

    private boolean existJobRunning = false;

    public GithubSensitiveSearchScheduler startWorks() {
        for (int i = 0; i < executorNum; i++) {
            Thread t = new Thread(new TaskExecutor(this)
                    .withReporter(this)
                    .withClient(GithubClient.newInstance()));
            executors.add(t);
            t.start();
        }
        return this;
    }

    public GithubSensitiveSearchScheduler stop() {
        executors.forEach(t-> t.stop());
        return this;
    }

    public GithubSensitiveSearchScheduler withExecutorNum(int num) {
        this.executorNum = num;
        return this;
    }

    public GithubSensitiveSearchScheduler addTokens(List<String> tokens) {
        tokens.forEach(t-> {
            this.tokens.add(new GithubAccessToken(t));
        });
        return this;
    }

    public GithubSensitiveSearchScheduler addToken(String token) {
        this.tokens.add(new GithubAccessToken(token));
        return this;
    }

    public GithubSensitiveSearchScheduler addReport(Reporter reporter){
        this.reporters.add(reporter);
        return this;
    }

    public GithubSensitiveSearchScheduler addTask(Task task) {
        if(task.qs().size() <= 0){
            throw new RuntimeException("The query must be not null");
        }

        task.setToken(this.client());
        try {
            this.taskQueue.put(task);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return this;
    }

    private synchronized GithubAccessToken client() {
        if (index >= tokens.size()) {
            index = 0;
        }
        int offset = index % tokens.size();
        index++;

        return tokens.get(offset);
    }

    public void report(Task task, String responseBody, Headers headers, TaskExecutorStatus status) {
        try {
            // decreace the num of work thread
            this.counter.decrease();

            this.reporters.forEach(r -> r.report(task, responseBody, status));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(TaskExecutorStatus.Forbidden.equals(status)){
            task.token().setXRateLimitRemaining(0);
            task.token().setXRateLimitReset(System.currentTimeMillis());
            this.putBackToQueue(task);
            // end the back.
            return;
        } else if(TaskExecutorStatus.Error.equals(status)) {
            this.putBackToQueue(task);
            // end the back.
            return;
        }

        if(headers != null) {
            Long XRateLimitReset = headers.get("X-RateLimit-Reset") != null
                    ? Long.valueOf(headers.get("X-RateLimit-Reset"))
                    : System.currentTimeMillis();
            Integer XRateLimitRemaining = headers.get("X-RateLimit-Remaining") != null
                    ? Integer.valueOf(headers.get("X-RateLimit-Remaining")) :
                    30;
            task.token().setXRateLimitRemaining(XRateLimitRemaining);
            task.token().setXRateLimitReset(XRateLimitReset);
        }

        if(task.page() == 1) {
            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            Integer count = (Integer) jsonObject.get("total_count");
            if(count == null) {
                this.putBackToQueue(task);
                // end the back.
                return;
            }
            int totalCount = task.size();
            for(int i = 2; totalCount < count && totalCount < 1000; i++) {
                Task newTask = new Task();
                newTask.setPage(i)
                        .setQs(task.qs())
                        .setExts(task.exts());
                this.addTask(newTask);
                totalCount += task.size();
            }
        }
    }

    private void putBackToQueue(Task task){
        System.out.println("put the task back to the Queue objct=" + task.toString() );
        this.addTask(task);
    }

    public Task fetchTask() throws InterruptedException {
        System.out.println("take one, current size:" + this.taskQueue.size() + "| thread id:" + Thread.currentThread().getId());
        try {
            Task task = this.taskQueue.take();
            // add one to the counter.
            System.out.println("increase cur work num:" + this.counter.increase());
            return task;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int workThreadNum() {
        // System.out.println("Thread work count:" + counter.get());
        return counter.get();
    }

    public boolean markJobRunning() {
        this.existJobRunning = true;
        return this.existJobRunning;
    }

    public boolean markJobFinish() {
        this.existJobRunning = false;
        return this.existJobRunning;
    }

    public boolean isExistJobRunning(){
        return this.existJobRunning;
    }

    public int taskQueueSize() {
        return taskQueue.size();
    }
}
