package com.vnjohn.zookeeper.configuration;

import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author vnjohn
 * @version 1.0
 * @date 2021/10/18 19:29
 */
public class TestConfig {
    ZooKeeper zk;

    private static Logger log = LoggerFactory.getLogger(TestConfig.class);

    @Before
    public void conn(){
        zk = ZkUtils.getZk();
    }

    @Test
    public void testConfig(){
        MyConfig myConfig = new MyConfig();
        WatchCallBack watchCallBack = new WatchCallBack();
        watchCallBack.setZk(zk);
        watchCallBack.setMyConfig(myConfig);
        watchCallBack.await();
        while (true){
            try {
                TimeUnit.MILLISECONDS.sleep(300);
                if (myConfig.getConfig().equals("")){
                    log.info("config 丢了");
                    watchCallBack.await();
                }else{
                    System.out.println(myConfig.getConfig());
                }
            } catch (InterruptedException e) {
                log.error("发生异常...");
                e.printStackTrace();
                break;
            }
        }
    }

    @After
    public void close(){
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
