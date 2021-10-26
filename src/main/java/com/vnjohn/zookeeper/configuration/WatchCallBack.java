package com.vnjohn.zookeeper.configuration;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author vnjohn
 * @version 1.0
 * @date 2021/10/18 19:42
 */
public class WatchCallBack implements Watcher,AsyncCallback.StatCallback,AsyncCallback.DataCallback {

    private static Logger log = LoggerFactory.getLogger(WatchCallBack.class);

    private ZooKeeper zk;

    private MyConfig myConfig;

    private CountDownLatch cc = new CountDownLatch(1);

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public MyConfig getMyConfig() {
        return myConfig;
    }

    public void setMyConfig(MyConfig myConfig) {
        this.myConfig = myConfig;
    }

    public void await(){
        zk.exists("/appConf", this,this,"abc");
        try {
            cc.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent event) {
        // 节点的变更
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                zk.getData("/appConf",this,this,"abc");
                break;
            case NodeDeleted:
                // 容忍性
                myConfig.setConfig("");
                cc = new CountDownLatch(1);
                break;
            case NodeDataChanged:
                zk.getData("/appConf",this,this,"abc");
                break;
            case NodeChildrenChanged:
                break;
        }
    }

    /**
     * 是否存在配置的回调
     */
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        // 说明已存在配置
        if (stat!=null){
            log.info("当前路径:{}",path);
            zk.getData(path,this,this,"abc");
        }
    }

    /**
     * 获取数据的回调
     */
    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        if (data!=null){
            log.info("获取数据:{}",new String(data));
            myConfig.setConfig(new String(data));
            // 获取数据后关闭门栓
            cc.countDown();
        }
    }
}
