package com.ai.gss.scheduler;

import com.ai.gss.executor.Task;
import com.ai.gss.executor.TaskExecutorStatus;
import com.ai.gss.scheduler.reporter.Reporter;
import com.alibaba.fastjson.JSONObject;

/**
 * @author pangms
 * @date 2020/9/7
 */
public abstract class AbstractDefaultTaskReporter implements Reporter {
    private final static int DEFAULT_PAGE_SIZE = 1000;
    private GithubSensitiveSearchScheduler scheduler;

    public AbstractDefaultTaskReporter(GithubSensitiveSearchScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void report(Task task, String responseBody, TaskExecutorStatus status) {
        if (status == TaskExecutorStatus.Error) {
            // 放回到调度队列中继续进行调度。
            this.scheduler.addTask(task);
            // 并且结束。
            return;
        }

        if (task.page() == 1) {
            // 如果当前页码为1， 则按照返回结果进行分页，最大页码数为1000（按照github官方规定，最大页码是1000)
            JSONObject obj = (JSONObject) JSONObject.parse(responseBody);
            Integer totalCount = (Integer) obj.get("total_count");
            if (totalCount == null) {
                System.out.printf("the task response error, not found the total_count file\n");
                throw new RuntimeException("the task response error, not found the total_count file\n");
            }

            int page = totalCount / DEFAULT_PAGE_SIZE;
            page = page > DEFAULT_PAGE_SIZE? DEFAULT_PAGE_SIZE: page;
            for (int size = 2; size <= page; size++) {
                Task t = new Task();
                t.setSize(1000).setPage(size);

                task.qs().forEach(q -> t.addQ(q));
                task.exts().forEach(q -> t.addExt(q));
                scheduler.addTask(task);
            }
        }

        this.dealResponse(responseBody);
    }

    protected abstract void dealResponse(String responseBody);
}
