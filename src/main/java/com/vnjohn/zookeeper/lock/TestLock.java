package com.vnjohn.zookeeper.lock;

import com.vnjohn.zookeeper.configuration.ZkUtils;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vnjohn
 * @version 1.0
 * @date 2021/10/20 18:28
 */
public class TestLock {
    private ZooKeeper zk;

    private Logger logger = LoggerFactory.getLogger(TestLock.class);

    @Before
    public void conn(){
        zk = ZkUtils.getZk();
    }

    @After
    public void close() throws InterruptedException {
        zk.close();
    }

    @Test
    public void tryLock(){
        // 模拟多线程并发争抢锁
        for (int i = 0; i < 10; i++) {
            new Thread(){
                @Override
                public void run() {
                    WatchCallBack watchCallBack = new WatchCallBack();
                    watchCallBack.setZk(zk);
                    String threadName = Thread.currentThread().getName();
                    watchCallBack.setThreadName(threadName);
                    //每一个线程：
                    //抢锁
                    watchCallBack.tryLock();
                    //干活
                    System.out.println(threadName+" working...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //释放锁
                    watchCallBack.unLock();


                }
            }.start();
        }
        while(true){

        }

        /*for (int i = 0; i < 10; i++) {
            new Thread(){
                @Override
                public void run() {
                    WatchCallBack watchCallBack = new WatchCallBack();
                    watchCallBack.setZk(zk);
                    watchCallBack.setThreadName(Thread.currentThread().getName());
                    watchCallBack.tryLock();
                    logger.info("{} 正在干活....",watchCallBack.getThreadName());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    watchCallBack.unLock();
                }
            }.start();
        }
        while (true){}
*/
    }
}
