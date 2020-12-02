package com.ai.gss.instance;

import com.ai.gss.executor.Task;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author pangms
 * @date 2020/9/24
 */
public class YamlConfig {
    @JsonProperty("receivers")
    private List<String> receivers;

    @JsonProperty("tasks")
    private List<Task> tasks;

    @JsonProperty("smtp")
    private String smtp;

    @JsonProperty("worksNum")
    private String worksNum;

    @JsonProperty("mailAccount")
    private String mailAccount;

    @JsonProperty("mailAccountPwd")
    private String mailAccountPwd;

    @JsonProperty("tokens")
    private List<String> tokens;

    @JsonProperty("schedule")
    private String schedule;

    public List<String> getReceivers() {
        return receivers;
    }

    @JsonProperty("receivers")
    public void setReceivers(List<String> receivers) {
        this.receivers = receivers;
    }

    public String getSmtp() {
        return smtp;
    }

    @JsonProperty("smtp")
    public void setSmtp(String smtp) {
        this.smtp = smtp;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    @JsonProperty("tasks")
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public String getMailAccount() {
        return mailAccount;
    }

    @JsonProperty("mailAccount")
    public void setMailAccount(String mailAccount) {
        this.mailAccount = mailAccount;
    }

    public String getMailAccountPwd() {
        return mailAccountPwd;
    }

    @JsonProperty("mailAccountPwd")
    public void setMailAccountPwd(String mailAccountPwd) {
        this.mailAccountPwd = mailAccountPwd;
    }

    @JsonProperty("tokens")
    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public String getSchedule() {
        return schedule;
    }

    @JsonProperty("schedule")
    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getWorksNum() {
        return worksNum;
    }

    @JsonProperty("worksNum")
    public void setWorksNum(String worksNum) {
        this.worksNum = worksNum;
    }

    @Override
    public String toString() {
        return "YamlConfig{" +
                "receivers=" + receivers +
                ", tasks=" + tasks +
                ", smtp='" + smtp + '\'' +
                ", mailAccount='" + mailAccount + '\'' +
                ", mailAccountPwd='" + mailAccountPwd + '\'' +
                ", tokens=" + tokens +
                '}';
    }
}
