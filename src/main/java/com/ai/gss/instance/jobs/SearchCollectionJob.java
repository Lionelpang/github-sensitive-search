package com.ai.gss.instance.jobs;

import com.ai.gss.executor.Task;
import com.ai.gss.executor.TaskExecutorStatus;
import com.ai.gss.instance.Poster;
import com.ai.gss.instance.YamlConfig;
import com.ai.gss.scheduler.GithubSensitiveSearchScheduler;
import com.ai.gss.scheduler.reporter.DefaultTaskReporter;
import com.ai.gss.scheduler.reporter.TaskReportEntity;
import com.ai.gss.util.YamlToObject;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pangms
 * @date 2020/9/25
 */
public class SearchCollectionJob implements Job {

    public final static String GITHUB_SENSITIVE_SEARCHS_CHEDULER_NAME = "scheduler";
    public final static String GITHUB_SENSITIVE_SEARCH_REPORTER_NAME = "reporter";
    public final static String GITHUB_SENSITIVE_SEARCH_CONFIG_NAME = "config";

    public YamlConfig dispatchTasks(GithubSensitiveSearchScheduler scheduler, YamlConfig config) throws IOException {
        for (Task task : config.getTasks()) {
            System.out.println("add the task(" + task.toString()+ ") to the queue");
            task.qs().forEach(q-> {
                Task t = new Task();
                t.addQ(q);
                t.setExts(task.exts());
                t.setGroup(task.getGroup());
                scheduler.addTask(t);
            });

        }

        return config;
    }

    @Override
    public void execute(JobExecutionContext context) {
        System.out.println("the job start");

        GithubSensitiveSearchScheduler scheduler = (GithubSensitiveSearchScheduler) context.getJobDetail().getJobDataMap().get(GITHUB_SENSITIVE_SEARCHS_CHEDULER_NAME);
        DefaultTaskReporter reporter = (DefaultTaskReporter) context.getJobDetail().getJobDataMap().get(GITHUB_SENSITIVE_SEARCH_REPORTER_NAME);
        YamlConfig config = (YamlConfig) context.getJobDetail().getJobDataMap().get(SearchCollectionJob.GITHUB_SENSITIVE_SEARCH_CONFIG_NAME);
        if(scheduler.isExistJobRunning()) {
            System.out.println("Exsit job is running exit!");
            return;
        }

        scheduler.markJobRunning();
        try {
            config = this.dispatchTasks(scheduler, config);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // start the report
        System.out.println("start the collection thread collect the response");
        ResponseCollector collector = new ResponseCollector(reporter);
        Thread collectorThread = new Thread(collector);
        collectorThread.start();

        while(true) {
            int workThreadNum = scheduler.workThreadNum();
            boolean collectorIsStop = collector.isStop();
            int notTodoTaskNum = scheduler.taskQueueSize();
            System.out.println("collectorStatus:" + collectorIsStop +"|workThreadNum:" + workThreadNum + "|taskNum:" + notTodoTaskNum);
            if(collectorIsStop && workThreadNum <= 0 && notTodoTaskNum <= 0) {
                collectorThread.stop();

                System.out.println("result:" + collector.getResult());
                sendResponse(config, collector.getResult());
                break;
            }

            try {
                System.out.println("wait for the collector end");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        scheduler.markJobFinish();
    }

    private void sendResponse(YamlConfig config, Map<String, Map<String, List<String>>> content){
        Poster poster = new Poster();
        try {
            poster.send(config.getReceivers(), config.getMailAccount(), config.getSmtp()
            , config.getMailAccount(), config.getMailAccountPwd(), formatMailContent(content));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatMailContent(Map<String, Map<String, List<String>>> content) {
        // Map<String, Map<String, List<String>>> root = new HashMap<>();
        JSONObject root =new JSONObject();

        content.forEach((task, qs)->{
            // Map<String, List<String>> qsMap = new HashMap<>();
            JSONObject qsMap = new JSONObject();
            root.put(task, qsMap);

            qs.forEach((rsname, contents) -> {
                // List<String> rses = new ArrayList<>();
                JSONArray rses = new JSONArray();
                qsMap.put(rsname, rses);

                contents.forEach(c->{
                    JSONObject jsonObject = JSONObject.parseObject(c);
                    JSONArray items = (JSONArray) jsonObject.get("items");
                    if(items != null && items.size() > 0) {
                        for (Object item : items) {
                            JSONObject i = (JSONObject) item;
                            String result = String.format("%s:%s"
                                    , i.get("name").toString()
                                    , i.get("html_url").toString());
                            rses.add(result);
                        }
                    }
                });
            });
        });


        String pretty = JSON.toJSONString(root, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat);

        return pretty;
    }
}

class ResponseCollector implements Runnable {
    // private static final long DEFAULT_STOP_TIME = 2*60*60*1000;
    private static final long DEFAULT_STOP_TIME = 60*1000;

    private DefaultTaskReporter reporter;

    private long lastCollectResponseTime = 0l;

    private Map<String, Map<String, List<String>>> map = new HashMap<>();;

    ResponseCollector(DefaultTaskReporter reporter){
        this.reporter = reporter;
    }

    @Override
    public void run() {

        while(true) {
            TaskReportEntity entity = reporter.fetchReport();
            if(entity == null) {
                // if the result is empty, sleep 1 seconds
                try {
                    Thread.sleep(1000);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
            }

            // recorde the time of last collect
            lastCollectResponseTime = System.currentTimeMillis();

            if(entity.getStatus().getCode() != TaskExecutorStatus.Successe.getCode()){
                continue;
            }

            Map<String, List<String>> groupMap = map.get(entity.getTask().getGroup());
            if(groupMap == null) {
                groupMap = new HashMap<>();
                map.put(entity.getTask().getGroup(), groupMap);
            }

            String qsString = String.join("-", entity.getTask().qs());
            List<String> msgs = groupMap.get(qsString);
            if(msgs == null) {
                msgs = new ArrayList<>();
                groupMap.put(qsString, msgs);
            }

            msgs.add(entity.getResponseBody());
        }
    }

    public boolean isStop() {
        return System.currentTimeMillis() - lastCollectResponseTime >= DEFAULT_STOP_TIME;
    }

    public Map<String, Map<String, List<String>>> getResult() {
        return this.map;
    }
}
