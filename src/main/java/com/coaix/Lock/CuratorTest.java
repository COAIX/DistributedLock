package com.coaix.Lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * 利用curator框架实现分布式锁
 */
public class CuratorTest {

    public static void main(String[] args) throws Exception {
        CuratorFramework client;
        InterProcessMutex lock1 = new InterProcessMutex(getClient(), "/locks");
        InterProcessMutex lock2 = new InterProcessMutex(getClient(), "/locks");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lock1.acquire();
                    System.out.println(Thread.currentThread().getName()+" 获取锁");
                    Thread.sleep(3*1000);
                    System.out.println(Thread.currentThread().getName()+" 获取锁");
                    lock1.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lock1.acquire();
                    System.out.println(Thread.currentThread().getName()+" 获取锁");
                    Thread.sleep(3*1000);
                    System.out.println(Thread.currentThread().getName()+" 释放锁");
                    lock1.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    public static CuratorFramework getClient() {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("hadoop102:2181,hadoop103:2181,hadoop104:2181", 2*1000, 2*1000, new ExponentialBackoffRetry(3*1000, 3));
        curatorFramework.start();
        System.out.println("----------------zooKeeper启动--------------");
        return curatorFramework;
    }
}
