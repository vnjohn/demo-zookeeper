package com.vnjohn.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import java.util.concurrent.CountDownLatch;

/**
 * @author vnjohn
 */
public class App {
    public static void main(String[] args) throws Exception {
        // ZK 是有 session 概念的,没有连接池概念
        // watch:观察,回调
        // watch 的注册只发生在读类型调用,
        // 第一类->new zk 时，传入的 watch，这个 watch 是 session 级别的，跟 path、node 没有关系
        CountDownLatch cd = new CountDownLatch(1);
        ZooKeeper zk = new ZooKeeper("192.168.56.8:2181,192.168.56.9:2181,192.168.56.10:2181,192.168.56.11:2181",
                3000, new Watcher() {
            /**
             * watch 的回调方法
             * @param event
             */
            @Override
            public void process(WatchedEvent event) {
                Event.KeeperState state = event.getState();
                Event.EventType type = event.getType();
                String path = event.getPath();
                System.out.println("new zk watch:"+event.toString());
                switch (state) {
                    case Unknown:
                        break;
                    case Disconnected:
                        break;
                    case NoSyncConnected:
                        break;
                    case SyncConnected:
                        System.out.println("connected");
                        cd.countDown();
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
                // 节点状态变更时的回调
                switch (type) {
                    case None:
                        break;
                    case NodeCreated:
                        break;
                    case NodeDeleted:
                        break;
                    case NodeDataChanged:
                        break;
                    case NodeChildrenChanged:
                        break;
                }
            }
        });
        cd.await();
        ZooKeeper.States state = zk.getState();
        switch (state) {
            case CONNECTING:
                System.out.println("ing.....................");
                break;
            case ASSOCIATING:
                break;
            case CONNECTED:
                System.out.println("ed.....................");
                break;
            case CONNECTEDREADONLY:
                break;
            case CLOSED:
                break;
            case AUTH_FAILED:
                break;
            case NOT_CONNECTED:
                break;
        }
        String pathName = zk.create("/ooxx", "olddata".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        Stat stat = new Stat();
        byte[] node = zk.getData("/ooxx", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("getData watch:"+event.toString());
                try {
                    // true default Watch 被重新注册->new zk 的那个 watch
                    zk.getData("/ooxx",true,stat);
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, stat);
        System.out.println(new String(node));
        // 触发回调
        Stat stat1 = zk.setData("/ooxx", "newdata".getBytes(), 0);
        // 还会触发吗？ 会,类似 CAS 版本号一致即可
        System.out.println(stat1.getVersion());
        zk.setData("/ooxx","newdata1".getBytes(),stat1.getVersion());

        // 异步回调
        System.out.println("---------------- async start ----------------------------");
        zk.getData("/ooxx", false, new AsyncCallback.DataCallback() {
            /**
             *
             * @param rc
             * @param path
             * @param ctx 上下文
             * @param data 节点数据
             * @param stat 元数据:各种事务 ID
             */
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                System.out.println("---------------- async call back ----------------------------");
                System.out.println("ctx:"+ctx.toString()+",data:"+new String(data));
            }
        },"abc");
        System.out.println("---------------- async over ----------------------------");

        // 如果故障停掉机器,zk 会有 fail-fast,但是也会快速的切换机器节点
        Thread.sleep(222222222);
    }
}
