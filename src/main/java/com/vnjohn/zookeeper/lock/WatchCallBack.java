package com.vnjohn.zookeeper.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author vnjohn
 * @version 1.0
 * @date 2021/10/20 18:30
 */
public class WatchCallBack implements Watcher, AsyncCallback.StringCallback,AsyncCallback.Children2Callback,AsyncCallback.StatCallback {

    private Logger logger = LoggerFactory.getLogger(WatchCallBack.class);
    private ZooKeeper zk;
    private String threadName;
    private String pathName;

    CountDownLatch cc = new CountDownLatch(1);

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }


    /**
     * 节点变更时会调用的方法
     * @param event
     */
    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                logger.info("节点 delete callback......");
                zk.getChildren("/",false,this ,"abc");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
        }
    }

    /**
     * 节点新建以后会进入到的方法
     */
    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        logger.info("thread name:{}",threadName);
        if (name!=null){
            logger.info("create node info path:{},name:{}",path,name);
            // 新建状态不去获取子节点的话,countDownLatch 就会一直阻塞
            this.setPathName(name);
            zk.getChildren("/",false,this,"abc");
        }
    }


    /**
     * 获取子节点数据的回调
     */
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        Collections.sort(children);
        // pathName 会带 / 所以去除第一个字符
        int index = children.indexOf(pathName.substring(1));
        if (index==0){
            // 说明是第一个,获取到锁
            logger.info("{} i am first {}.... ",threadName,pathName);
            // 太快了  执行会乱套  要么在下面重新设置新的数据,要么加载延迟
            try {
//                zk.setData("/",threadName.getBytes(),-1);
                cc.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            // 等你要去监控别人的节点时,可能别人已经掉线了,这个是保证措施
            String upPathName = children.get(index - 1);
            logger.info("{} up children node info {}",pathName,upPathName);
            // Watcher 者应该是当前对象
            zk.exists("/"+upPathName,this,this,"abc");
        }
    }

    /**
     * 节点是否存在时的回调
     */
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        /*if (stat!=null){
            logger.info("node is exists {}",pathName);
//            zk.getChildren("/",false,this,"abc");
        }*/
    }

    public void tryLock(){
        try {
            System.out.println(threadName + "  create....");
            zk.create("/lock",threadName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL,this,"abc");
            cc.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 如果 tryLock 新建状态不去获取子节点的话,countDownLatch 就会一直阻塞
     */
    public void unLock() {
        try {
            zk.delete(pathName,-1);
            logger.info("{} over work {}.....",threadName,pathName);
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }
}
