package com.ai.gss.executor;

import com.ai.gss.executor.okhttp3.ObsoleteUrlFactory;
import okhttp3.Call;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author pangms
 * @date 2020/8/19
 */
public class GithubClient {
    private static final int PAGE_SIZE = 100;

    private String url = "https://api.github.com/search/code";
    private OkHttpClient client;
    // private static GithubClient githubUrlBuilder = new GithubClient();

    private GithubClient(){
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();

        builder.connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT));
        this.client = builder.build();
    }

    public static GithubClient newInstance(){
        return new GithubClient();
    }

    public Call call(Task task) {
        Request req = new Request.Builder().url(this.buildUrl(task))
                .addHeader("Authorization", "token " + task.token().getToken())
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("Time-Zone", "Asia/Shanghai")
                .build();

        return client.newCall(req);
    }

    public String buildUrl(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.url).append("?").append("sort=indexed&order=desc&per_page=")
                .append(task.size()).append("&page=").append(task.page());
        if(task.qs().size() > 0) {
            sb.append("&q=%22");
            task.qs().forEach(q -> sb.append(q).append("%22"));
        }

        if(task.exts().size() > 0) {
            task.exts().forEach(ext-> sb.append("+extension").append("%3A").append(ext));
        }
        return sb.toString();
    }

}
