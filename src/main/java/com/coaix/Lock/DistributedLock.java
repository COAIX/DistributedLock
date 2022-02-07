package com.coaix.Lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * 基于Zookeeper的分布式锁
 */
public class DistributedLock {

    private final ZooKeeper zooKeeper;
    private final CountDownLatch connect = new CountDownLatch(1);
    private final CountDownLatch wake = new CountDownLatch(1);
    private String preNode;
    private String currentNode;


    public DistributedLock() throws IOException, InterruptedException, KeeperException {
        //创建zookeeper客户端
        String connectString = "hadoop102:2181,hadoop103:2181,hadoop104:2181";
        zooKeeper = new ZooKeeper(connectString, 2 * 1000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    connect.countDown();
                }

                if (event.getType()==Event.EventType.NodeDeleted && Objects.equals(event.getPath(), preNode)) {
                    wake.countDown();
                }
            }
        });
        //如果没有连接成功zookeeper等待
        connect.await();
        //查看zookeeper中是否存在locks节点，如果不存在创建
        Stat stat = zooKeeper.exists("/locks", false);
        if (stat == null) {
            zooKeeper.create("/locks", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

    /**
     * 利用zookeeper上分布式锁
     */
    public void lock() throws KeeperException, InterruptedException {
        //在locks建立节点scp-0000....
        currentNode = zooKeeper.create("/locks/scp-", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("zk-lock建立锁 "+currentNode);
        //获取建立的节点在locks节点下的位置
        List<String> children = zooKeeper.getChildren("/locks", false);
        int index = children.indexOf(currentNode.substring("/locks/".length()));
        // 如果在第0位，则立刻获取这个锁
        if (index == 0) {
            return;
        } else if (index < 0) {
            // 如果小于0，则出现错误
            System.out.println("----------数据ERROR----------");
        } else {
            // 如果大于0，则将所在位置的前一位锁节点记录，阻塞进程，直到监视进程发现前节点消失释放进程
            preNode = "/locks/"+children.get(index - 1);
            zooKeeper.getData(preNode,true,new Stat());
            wake.await();
        }
    }

    /**
     * 释放锁
     */
    public void releaseLock() throws KeeperException, InterruptedException {
        zooKeeper.delete(currentNode,-1);
    }

}
