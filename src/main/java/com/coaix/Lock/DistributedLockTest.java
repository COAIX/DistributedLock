package com.coaix.Lock;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;

/**
 * @author LiaoWei
 * @date 2022-02-07 20:39
 */
public class DistributedLockTest {
    public static void main(String[] args) throws InterruptedException, IOException, KeeperException {
        DistributedLock lock1 = new DistributedLock();
        DistributedLock lock2 = new DistributedLock();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lock1.lock();
                    System.out.println("线程 1 获取锁");
                    Thread.sleep(5*1000);
                    System.out.println("线程 1 释放锁");
                    lock1.releaseLock();
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lock2.lock();
                    System.out.println("线程 2 获取锁");
                    Thread.sleep(5*1000);
                    System.out.println("线程 2 释放锁");
                    lock2.releaseLock();
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }
}
