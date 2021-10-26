package com.vnjohn.zookeeper.configuration;

import org.apache.zookeeper.ZooKeeper;
import java.util.concurrent.CountDownLatch;

/**
 * @author vnjohn
 * @version 1.0
 * @date 2021/10/18 18:59
 */
public class ZkUtils {
    private static ZooKeeper zk;

    private static String address = "192.168.56.8:2181,192.168.56.9:2181,192.168.56.10:2181,192.168.56.11:2181/testConfig";

    private static DefaultWatcher watcher = new DefaultWatcher();

    // 判断连接是否成功的门栓
    private static CountDownLatch init = new CountDownLatch(1);

    public static ZooKeeper getZk() {
        try {
            watcher.setCc(init);
            zk = new ZooKeeper(address,3000,watcher);
            init.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return zk;
    }
}
