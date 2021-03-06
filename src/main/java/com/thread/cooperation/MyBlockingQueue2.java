package com.thread.cooperation;

import java.util.ArrayDeque;
import java.util.Queue;

public  class MyBlockingQueue2<E> {
    private Queue<E> queue = null;
    private int limit;

    public MyBlockingQueue2(int limit) {
        this.limit = limit;
        queue = new ArrayDeque<>(limit);
    }

    public synchronized void put(E e) throws InterruptedException {
        while (queue.size() == limit) {
            wait();
        }
        queue.add(e);
        notifyAll();
    }

    public synchronized E take() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        E e = queue.poll();
        notifyAll();
        return e;
    }
}