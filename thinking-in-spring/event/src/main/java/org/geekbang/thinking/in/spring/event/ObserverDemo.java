package org.geekbang.thinking.in.spring.event;

import java.util.Observable;
import java.util.Observer;

/**
 * {@link Observer} Demo
 *
 * @author ajin
 */

public class ObserverDemo {

    public static void main(String[] args) {
        EventObservable observable = new EventObservable();


        // 添加(观察者）监听者
        observable.addObserver(new EventObserver());

        // 发布消息、事件
        observable.notifyObservers("Hello,World!");
    }

    static class EventObservable extends Observable {
        @Override
        public synchronized void setChanged() {
            super.setChanged();
        }

        @Override
        public void notifyObservers(Object arg) {
            setChanged();
            super.notifyObservers(arg);
            clearChanged();
        }
    }


    /**
     * 观察者
     */
    static class EventObserver implements Observer {

        /**
         * @param o       被观察对象
         * @param message 数据
         */
        @Override
        public void update(Observable o, Object message) {
            System.out.println("收到事件" + message);
        }
    }
}
