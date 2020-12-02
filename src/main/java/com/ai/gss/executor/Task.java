package com.ai.gss.executor;

import com.ai.gss.scheduler.GithubAccessToken;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 任务对象
 * @author pangms
 * @date 2020/8/19
 */
public class Task {
    @JsonProperty("group")
    private String group = "default";
    // 查询记录
    @JsonProperty("qs")
    private Set<String> qs = new HashSet<>();
    // 扩展
    @JsonProperty("exts")
    private Set<String> exts = new HashSet<>();

    private int page = 1;

    private int size = 100;

    private GithubAccessToken token;

    // 请求地址
    private String url = "https://api.github.com/search/code";

    private int errorCount = 0;

    private Date lastErrorTime = null;

    public Task addQ(String q){
        this.qs.add(q);
        return this;
    }

    @JsonProperty("qs")
    public Task setQs(Set<String> qs){
        this.qs.addAll(qs);
        return this;
    }

    public Task addExt(String ext){
        this.exts.add(ext);
        return this;
    }

    @JsonProperty("exts")
    public Task setExts(Set<String> exts){
        this.exts.addAll(exts);
        return this;
    }

    public Task setToken(String token) {
        this.token = new GithubAccessToken(token);
        return this;
    }

    public Task setToken(GithubAccessToken token) {
        this.token = token;
        return this;
    }

    public Task setSize(int size) {
        this.size = size;
        return this;
    }

    public Task setPage(int page) {
        this.page = page;
        return this;
    }

    public Set<String> qs() {
        return qs;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void increaceErrorCount() {
        this.errorCount++;
    }

    public void resetErrorCount() {
        this.errorCount = 0;
    }

    public Date getLastErrorTime() {
        return lastErrorTime;
    }

    public void setLastErrorTime(Date lastErrorTime) {
        this.lastErrorTime = lastErrorTime;
    }

    public Set<String> exts() {
        return exts;
    }

    public int page() {
        return page;
    }

    public GithubAccessToken token() {
        return token;
    }

    public String url() {
        return url;
    }

    public int size(){
        return this.size;
    }

    public String getGroup() {
        return group;
    }

    @JsonProperty("group")
    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return "Task{" +
                "qs=" + qs +
                ", exts=" + exts +
                ", page=" + page +
                ", size=" + size +
                ", token='" + token + '\'' +
                ", url='" + url + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
