package com.ai.gss.instance.jobs;

import com.ai.gss.instance.YamlConfig;
import com.ai.gss.scheduler.GithubSensitiveSearchScheduler;
import com.ai.gss.scheduler.reporter.DefaultTaskReporter;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author pangms
 * @date 2020/9/25
 */
public class CrontabSchedule {
    private GithubSensitiveSearchScheduler githubSensitiveSearchScheduler;
    private DefaultTaskReporter taskReporter;
    private YamlConfig config;

    public CrontabSchedule(GithubSensitiveSearchScheduler searchSchedule, DefaultTaskReporter taskReporter, YamlConfig config){
        this.githubSensitiveSearchScheduler = searchSchedule;
        this.taskReporter = taskReporter;
        this.config = config;
    }

    public void start() throws SchedulerException {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();

        // create the job
        this.searchJob(scheduler);

        //4、执行
        System.out.println("--------scheduler start ! ------------");
        scheduler.start();
    }

    private void searchJob(Scheduler scheduler) throws SchedulerException {
        // 2、创建JobDetail实例，并与PrintWordsJob类绑定(Job执行内容)
        JobDataMap map =  new JobDataMap();
        map.put(SearchCollectionJob.GITHUB_SENSITIVE_SEARCHS_CHEDULER_NAME, githubSensitiveSearchScheduler);
        map.put(SearchCollectionJob.GITHUB_SENSITIVE_SEARCH_REPORTER_NAME, taskReporter);
        map.put(SearchCollectionJob.GITHUB_SENSITIVE_SEARCH_CONFIG_NAME, config);

        JobDetail jobDetail = JobBuilder.newJob(SearchCollectionJob.class)
                .withIdentity("GITHUB_SENSITIVE_SEARCH_JOB", "Local_Search")
                .setJobData(map)
                .build();
        // 3、构建Trigger实例,每隔1s执行一次
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("LOCAL_SEARCH_TRIGGER", "LOCAL_SEARCH_TRIGGER_GROUP1")
                .startNow()//立即生效
                .withSchedule(CronScheduleBuilder.cronSchedule(config.getSchedule()))
                .build();//一直执行

        scheduler.shutdown();
        scheduler.scheduleJob(jobDetail, trigger);
        scheduler.clear();
    }

}
