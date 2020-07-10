package org.n3r.diamond.client.impl;

import org.n3r.diamond.client.DiamondAxis;
import org.n3r.diamond.client.DiamondListener;
import org.n3r.diamond.client.DiamondStone;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import static org.n3r.diamond.client.impl.DiamondLogger.log;

class DiamondAllListener implements DiamondListener {
    private final ConcurrentMap<DiamondAxis, CopyOnWriteArrayList<DiamondListener>> allListeners = new ConcurrentHashMap<>();

    public void accept(final DiamondStone diamondStone) {
        CopyOnWriteArrayList<DiamondListener> listeners = allListeners.get(diamondStone.getDiamondAxis());
        if (listeners == null || listeners.isEmpty()) {
            // log.debug("[notify-listener] no listener for {}", diamondStone.getDiamondAxis());
            return;
        }

        for (DiamondListener listener : listeners) {
            try {
                notifyListener(diamondStone, listener);
            } catch (Throwable t) {
                log().error("call listener error, {}", diamondStone.getDiamondAxis(), t);
            }
        }
    }


    private void notifyListener(final DiamondStone diamondStone, final DiamondListener listener) {
        if (listener == null) return;

        log().info("call listener {} for {}", listener, diamondStone.getDiamondAxis());

        Runnable job = () -> {
            try {
                listener.accept(diamondStone);
            } catch (Throwable t) {
                log().error("listener error {}", listener, t);
            }
        };

        if (null != listener.getExecutor()) {
            listener.getExecutor().execute(job);
        } else {
            job.run();
        }
    }

    public void removeManagerListeners(DiamondAxis diamondAxis) {
        allListeners.remove(diamondAxis);
    }

    public void addDiamondListener(DiamondAxis diamondAxis, DiamondListener addListener) {
        if (null == addListener) return;

        CopyOnWriteArrayList<DiamondListener> listenerList = allListeners.get(diamondAxis);
        if (listenerList == null) {
            listenerList = new CopyOnWriteArrayList<>();
            CopyOnWriteArrayList<DiamondListener> oldList = allListeners.putIfAbsent(diamondAxis, listenerList);
            if (oldList != null) listenerList = oldList;
        }

        listenerList.add(addListener);
    }

    public void removeDiamondListener(DiamondAxis diamondAxis, DiamondListener addListener) {
        if (null == addListener) return;

        CopyOnWriteArrayList<DiamondListener> listenerList = allListeners.get(diamondAxis);
        if (listenerList != null) listenerList.remove(addListener);
    }

    public ExecutorService getExecutor() {
        return null;
    }
}
