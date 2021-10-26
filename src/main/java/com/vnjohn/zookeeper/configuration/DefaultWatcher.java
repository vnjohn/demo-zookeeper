package com.vnjohn.zookeeper.configuration;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import java.util.concurrent.CountDownLatch;

/**
 * @author vnjohn
 * @version 1.0
 * @date 2021/10/18 19:01
 */
public class DefaultWatcher implements Watcher {
    private CountDownLatch cc;

    public void setCc(CountDownLatch cc) {
        this.cc = cc;
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getState()) {
            case Unknown:
                break;
            case Disconnected:
                break;
            case NoSyncConnected:
                break;
            case SyncConnected:
                cc.countDown();
                break;
            case AuthFailed:
                break;
            case ConnectedReadOnly:
                break;
            case SaslAuthenticated:
                break;
            case Expired:
                break;
        }

        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                System.out.println("节点 delete callback......");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
        }
    }
}
