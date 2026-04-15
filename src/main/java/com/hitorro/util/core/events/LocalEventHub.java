/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core.events;

import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.thread.EnhancedThreadFactory;
import com.hitorro.util.core.thread.EnhancedThreadGroup;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * The hub is responsible for two things:
 * <p/>
 * 1) one registers event listeners (callbacks that listen for events that occur) 2) take events from event producers
 * and distribute them to registered event listeners (consumers).
 * <p/>
 * One should probably create one of these in a running system, unless you want to keep a bunch of private communication
 * buses.
 *
 * @author chris
 */
public class LocalEventHub implements EventListener {
    public static final String Name = "LocalEventHubAsyncNotifiier";
    public static EnhancedThreadGroup s_tg = new EnhancedThreadGroup(Name);
    private static final LocalEventHub s_localHub = new LocalEventHub();
    private ExecutorService m_executors;
    private final HashMap<String, WeakReferenceList<EventListener>> type = new HashMap<>();

    public LocalEventHub() {
        initThreadPool();
    }

    public static LocalEventHub get() {
        return s_localHub;
    }

    /**
     * Get list of registered listeners
     *
     * @return list of registered listeners
     */
    @SuppressWarnings("unchecked")
    public List<GenericKeyValue> getRegisteredListeners() {
        List<GenericKeyValue> kvList = new ArrayList<>();

        Iterator<Map.Entry<String, WeakReferenceList<EventListener>>> iter = type.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, WeakReferenceList<EventListener>> entry = iter.next();
            WeakReferenceList<EventListener> wl = entry.getValue();
            String key = entry.getKey();
            for (int i = wl.size() - 1; i >= 0; i--) {
                EventListener el = wl.get(i);
                if (el != null) {
                    kvList.add(new GenericKeyValue<>(key, el.eventName()));
                }
            }
        }
        return kvList;
    }

    public String eventName() {
        return "Local Hub";
    }


    /**
     * Determine if a topic has something listening on it at the moment.
     *
     * @param topic
     * @return
     */
    public synchronized boolean hasEventRegistered(String topic) {
        return type.get(topic) != null;
    }

    /**
     * Called by someone that wishes to notify this process of an event.  All Observers of this event are notified.
     * Returns list of listener names that were notified.
     */
    public synchronized List<String> eventListingNotified(String eventName, String subEvent, Object args) {
        List<String> notified = new ArrayList<>();
        WeakReferenceList<EventListener> l = type.get(eventName);
        boolean hasNulls = false;
        if (l != null) {
            synchronized (l) {
                int size = l.size();
                for (int i = 0; i < size; i++) {
                    EventListener el = l.get(i);
                    if (el == null) {
                        hasNulls = true;
                    } else {
                        notified.add(el.eventName());
                        safeExecuteEvent(eventName, subEvent, args, el);
                    }
                }
                if (hasNulls) {
                    l.removeNulls();
                }
            }
        }
        return notified;
    }

    public synchronized boolean event(String eventName, String subEvent, Object args) {
        WeakReferenceList<EventListener> l = type.get(eventName);
        boolean hasNulls = false;
        if (l != null) {
            synchronized (l) {
                int size = l.size();
                for (int i = 0; i < size; i++) {
                    EventListener el = l.get(i);
                    if (el == null) {
                        hasNulls = true;
                    } else {
                        safeExecuteEvent(eventName, subEvent, args, el);
                    }
                }
                if (hasNulls) {
                    l.removeNulls();
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Convenience method to fire an event with no arguments.
     */
    public boolean fire(String eventName, String subEvent) {
        return event(eventName, subEvent, null);
    }

    /**
     * Execute an event, catching exceptions so one bad listener doesn't prevent others from being notified.
     */
    private void safeExecuteEvent(String eventName, String subEvent, Object args, EventListener el) {
        try {
            if (el.runAsync()) {
                AsyncEventNotification async = new AsyncEventNotification(el, eventName, subEvent, args);
                m_executors.execute(async);
            } else {
                el.event(eventName, subEvent, args);
            }
        } catch (Exception e) {
            Log.util.error("Exception dispatching event '%s/%s' to listener '%s': %s",
                    eventName, subEvent, el.eventName(), e.getMessage());
        }
    }

    /**
     * Communicate to ALL observers that have registered with the LocalEventHub.
     */
    public void eventAll(String subEvent, Object args) {
        Set<String> keys = type.keySet();
        for (String s : keys) {
            event(s, subEvent, args);
        }
    }

    /**
     * Registration mechanism for an observer to register against the event hub for notification of events. Note that
     * the event list holds weak references. Therefore, the observer (i.e., the event handler) will disappear unless its
     * reference is held by another object.
     *
     * @param el        instance that honors the EventListener interface
     * @param eventName that the observer wishes to listen on.
     */
    public synchronized void addEventListener(EventListener el, String eventName) {
        WeakReferenceList<EventListener> l = type.get(eventName);
        if (l == null) {
            l = new WeakReferenceList<>();
            type.put(eventName, l);
        }

        synchronized (l) {
            l.addIfAbsent(el);
        }
    }

    /**
     * Explicitly remove a listener from a topic.
     *
     * @param el        the listener to remove
     * @param eventName the topic to remove it from
     * @return true if the listener was found and removed
     */
    public synchronized boolean removeEventListener(EventListener el, String eventName) {
        WeakReferenceList<EventListener> l = type.get(eventName);
        if (l != null) {
            synchronized (l) {
                return l.remove(el);
            }
        }
        return false;
    }

    /**
     * Returns the count of active (non-null) listeners for a given topic.
     */
    public synchronized int getListenerCount(String topic) {
        WeakReferenceList<EventListener> l = type.get(topic);
        if (l == null) {
            return 0;
        }
        synchronized (l) {
            int count = 0;
            for (int i = 0; i < l.size(); i++) {
                if (l.get(i) != null) {
                    count++;
                }
            }
            return count;
        }
    }

    /**
     * Shut down the executor service cleanly.
     */
    public void shutdown() {
        m_executors.shutdown();
        try {
            if (!m_executors.awaitTermination(5, TimeUnit.SECONDS)) {
                m_executors.shutdownNow();
            }
        } catch (InterruptedException e) {
            m_executors.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public boolean runAsync() {
        return false;
    }

    private void initThreadPool() {
        m_executors = Executors.newCachedThreadPool(
                new EnhancedThreadFactory("EventListenerThreadPool",
                        "EventThread(%s)", true));
    }
}

class AsyncEventNotification implements Runnable {
    private final EventListener m_el;
    private final String m_topic;
    private final String m_subTopic;
    private final Object m_args;

    public AsyncEventNotification(EventListener el, String topic, String subTopic, Object args) {
        m_el = el;
        m_topic = topic;
        m_subTopic = subTopic;
        m_args = args;
    }

    public void run() {
        try {
            m_el.event(m_topic, m_subTopic, m_args);
        } catch (Exception e) {
            Log.util.error("Async event '%s/%s' failed in listener '%s': %s",
                    m_topic, m_subTopic, m_el.eventName(), e.getMessage());
        }
    }
}
