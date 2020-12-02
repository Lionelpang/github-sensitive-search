package com.ai.gss.scheduler;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author pangms
 * @date 2020/9/27
 */
public class GithubAccessToken {
    public String token;
    public long XRateLimitReset = 0;
    public int XRateLimitRemaining = 30;

    private ReentrantLock lock = new ReentrantLock();

    public GithubAccessToken(String token) {
        this.token = token;
    }
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getXRateLimitReset() {
        return XRateLimitReset;
    }

    public void setXRateLimitReset(long XRateLimitReset) {
        lock.lock();
        try {
            this.XRateLimitReset = XRateLimitReset;
        } finally {
            lock.unlock();
        }
    }

    public int getXRateLimitRemaining() {
        return XRateLimitRemaining;
    }

    public void setXRateLimitRemaining(int XRateLimitRemaining) {
        lock.lock();
        try {
            this.XRateLimitRemaining = XRateLimitRemaining;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return "GithubAccessToken{" +
                "token='" + token + '\'' +
                ", XRateLimitReset=" + XRateLimitReset +
                ", XRateLimitRemaining=" + XRateLimitRemaining +
                ", lock=" + lock +
                '}';
    }
}
