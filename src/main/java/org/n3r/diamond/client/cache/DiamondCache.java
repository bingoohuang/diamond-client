package org.n3r.diamond.client.cache;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.Futures;
import lombok.val;
import org.n3r.diamond.client.DiamondAxis;
import org.n3r.diamond.client.impl.DiamondSubstituter;
import org.n3r.diamond.client.impl.DiamondUtils;
import org.n3r.diamond.client.impl.SnapshotMiner;

import java.util.Arrays;
import java.util.concurrent.*;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.n3r.diamond.client.impl.DiamondLogger.log;

public class DiamondCache {
    private final SnapshotMiner snapshotMiner;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Cache<DiamondAxis, Future<Object>> cache = CacheBuilder.newBuilder().build();

    public DiamondCache(SnapshotMiner snapshotMiner) {
        this.snapshotMiner = snapshotMiner;
    }

    public Object getCache(final DiamondAxis diamondAxis,
                           final String diamondContent,
                           final Object... dynamics) {
        final int dynamicsHasCode = Arrays.deepHashCode(dynamics);

        val callable = createFirstFutureCallable(diamondAxis, diamondContent, dynamics);

        Future<Object> cachedObject;
        try {
            cachedObject = cache.get(diamondAxis, callable);
        } catch (ExecutionException e) {
            log().error("get cache {} failed with error {}", diamondContent, Throwables.getStackTraceAsString(e));
            return null;
        }

        Object object = futureGet(diamondAxis, diamondContent, cachedObject, dynamicsHasCode);
        if (!(object instanceof Cache)) return object;

        val subCache = (Cache<Integer, Future<Object>>) object;

        val dynamicCallable = getSecondFutureCallable(diamondAxis, diamondContent, dynamics);
        Future<Object> subCachedObject;
        try {
            subCachedObject = subCache.get(dynamicsHasCode, dynamicCallable);
        } catch (ExecutionException e) {
            log().error("get dynamic cache {} failed with {}", diamondContent, Throwables.getStackTraceAsString(e));
            return null;
        }

        return futureGet(diamondAxis, diamondContent, subCachedObject, dynamicsHasCode);
    }

    private Callable<Future<Object>> getSecondFutureCallable(final DiamondAxis diamondAxis,
                                                             final String diamondContent,
                                                             final Object[] dynamics) {
        return () -> executorService.submit(() -> {
            Callable<Object> updater = createUpdater(diamondAxis, diamondContent, dynamics);
            if (updater == null) return null;

            Optional<Object> objectOptional = updateCache(updater, diamondAxis, diamondContent, dynamics);
            return objectOptional != null ? objectOptional.orNull() : null;
        });
    }

    private Callable<Future<Object>> createFirstFutureCallable(final DiamondAxis diamondAxis,
                                                               final String diamondContent,
                                                               final Object[] dynamics) {
        return () -> executorService.submit(() -> {
            Callable<Object> updater = createUpdater(diamondAxis, diamondContent, dynamics);
            if (updater == null) return null;

            if (isDynamicApplicable(updater)) {
                return CacheBuilder.newBuilder().build();
            } else {
                Optional<Object> objectOptional = updateCache(updater, diamondAxis, diamondContent, dynamics);
                return objectOptional != null ? objectOptional.orNull() : null;
            }
        });
    }

    private Object futureGet(DiamondAxis diamondAxis,
                             String diamondContent, Future<Object> future, int dynamicsHasCode) {
        try {
            return future.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException e) {   // 有限时间内不返回，尝试读取snapshot版本
            log().error("update cache {} timeout, try to use snapshot", diamondContent);
            Optional<Object> object = snapshotMiner.getCache(diamondAxis, dynamicsHasCode);
            if (object != null) return object.orNull();
        } catch (Exception e) {
            log().error("update cache {} failed with error {}", diamondContent, Throwables.getStackTraceAsString(e));
        }

        try {
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            log().error("update cache {} failed with error {}", diamondContent, Throwables.getStackTraceAsString(e));
        }

        return null;
    }

    private Optional<Object> updateCache(Callable<Object> updater,
                                         DiamondAxis diamondAxis,
                                         String diamondContent,
                                         Object... dynamics) {
        log().info("start to update cache {}", diamondAxis);
        if (isEmpty(diamondContent)) return null;

        Object diamondCache;
        try {
            diamondCache = updater.call();
        } catch (Exception e) {
            log().error("{} called with exception", diamondContent, e);
            return null;
        }

        int dynamicsHasCode = Arrays.deepHashCode(dynamics);
        snapshotMiner.saveCache(diamondAxis, diamondCache, dynamicsHasCode);
        log().info("end to update cache {}", diamondAxis);

        return Optional.fromNullable(diamondCache);
    }


    private Callable<Object> createUpdater(final DiamondAxis diamondAxis, String diamondContent, Object... dynamics) {
        String substitute = DiamondSubstituter.substitute(diamondContent, true, diamondAxis.group, diamondAxis.dataId, null);
        Callable object = DiamondUtils.parseObject(substitute, Callable.class);
        if (object == null) {
            log().error("{} cannot be parsed as Callable", diamondContent);
            return null;
        }

        if (object instanceof DynamicsAppliable)
            ((DynamicsAppliable) object).setDynamics(dynamics);

        return object;
    }

    public void close() {
        executorService.shutdownNow();
    }

    public Future<Object> updateDiamondCacheOnChange(final DiamondAxis diamondAxis,
                                                     final String diamondContent) {
        Future<Object> cacheOptional = cache.getIfPresent(diamondAxis);
        if (cacheOptional == null) return cacheOptional;

        Callable<Object> task = () -> {
            Callable<Object> updater = createUpdater(diamondAxis, diamondContent);
            if (updater == null) return null;

            if (isDynamicApplicable(updater)) {
                removeCacheSnapshot(diamondAxis);
                return null;
            } else {
                final Optional<Object> updated = updateCache(updater, diamondAxis, diamondContent);
                if (updated != null) cache.put(diamondAxis, Futures.immediateFuture(updated.orNull()));
                return updated;
            }
        };

        return executorService.submit(task);
    }

    private boolean isDynamicApplicable(Callable<Object> updater) {
        return updater instanceof DynamicsAppliable;
    }

    public void removeCacheSnapshot(DiamondAxis diamondAxis) {
        cache.invalidate(diamondAxis);
        snapshotMiner.removeAllCache(diamondAxis);
    }
}
