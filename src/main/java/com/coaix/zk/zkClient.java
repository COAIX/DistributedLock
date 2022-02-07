package com.coaix.zk;
import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.junit.Test;
import java.io.IOException;
import java.util.List;

/**
 * Zookeeper模拟动态上下线监听
 */
public class zkClient {

    private final ZooKeeper zooKeeper;

    public zkClient() throws IOException {
        zooKeeper = new ZooKeeper("hadoop102:2181,hadoop103:2181,hadoop104:2181", 2 * 1000, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                //监听方法，在收到信息之后会调用监听
                try {
                    watchAction();
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Test
    public void create() throws IOException, KeeperException, InterruptedException {
        byte[] data = "111".getBytes();
        //创建节点/source/source-children0000000....
        String s = zooKeeper.create("/source/source-children", data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Create" + s);
    }


    public void watchAction() throws KeeperException, InterruptedException {
        System.out.println("------------------------WATCH----------------------");
        List<String> children = zooKeeper.getChildren("/source", true);
        children.forEach(System.out::println);
        System.out.println("------------------------WATCH----------------------");
    }


    @Test
    public void watchtest() throws KeeperException, InterruptedException {
        watchAction();
        //阻塞
        Thread.sleep(Long.MAX_VALUE);
    }

}
