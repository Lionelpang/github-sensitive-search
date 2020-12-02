package com.ai.gss.instance;

import com.ai.gss.instance.jobs.CrontabSchedule;
import com.ai.gss.scheduler.GithubSensitiveSearchScheduler;
import com.ai.gss.scheduler.reporter.DefaultTaskReporter;
import com.ai.gss.util.YamlToObject;
import org.quartz.SchedulerException;

import java.io.IOException;

/**
 * @author pangms
 * @date 2020/9/24
 */
public class SendInstance {

    public static final void main(String[] args) throws IOException {
        String filePath = "classpath:tasks.yaml";

        if(args.length > 0) {
            // if has the param，get the file
            filePath = args[0];
        }

        System.out.println("filePath:" + filePath);
        YamlConfig config = YamlToObject.parseByClassPath(filePath, YamlConfig.class);

        DefaultTaskReporter taskReporter = new DefaultTaskReporter();
        GithubSensitiveSearchScheduler scheduler = new GithubSensitiveSearchScheduler().withExecutorNum(5);
        // add the taskReporter
        scheduler.addReport(taskReporter);

        // add the tokens
        config.getTokens().forEach(token -> scheduler.addToken(token));

        // start the works
        scheduler.startWorks();

        CrontabSchedule instance =new CrontabSchedule(scheduler, taskReporter, config);
        try {
            instance.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        while(true){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
