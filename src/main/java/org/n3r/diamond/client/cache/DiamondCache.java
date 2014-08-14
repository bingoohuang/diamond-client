package org.n3r.diamond.client.cache;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import org.joor.Reflect;
import org.n3r.diamond.client.DiamondStone;
import org.n3r.diamond.client.impl.DiamondSubstituter;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.n3r.diamond.client.impl.SnapshotMiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.*;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class DiamondCache {
    private final SnapshotMiner snapshotMiner;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Logger log = LoggerFactory.getLogger(DiamondCache.class);
    private Cache<DiamondStone.DiamondAxis, Future<Object>> cache = CacheBuilder.newBuilder().build();

    public DiamondCache(SnapshotMiner snapshotMiner) {
        this.snapshotMiner = snapshotMiner;
    }

    public Object getCache(final DiamondStone.DiamondAxis diamondAxis,
                           final String diamondContent,
                           final Object... dynamics) {
        final int dynamicsHasCode = Arrays.deepHashCode(dynamics);

        Callable<Future<Object>> callable = createFirstFutureCallable(diamondAxis, diamondContent, dynamics);

        Future<Object> cachedObject;
        try {
            cachedObject = cache.get(diamondAxis, callable);
        } catch (ExecutionException e) {
            log.error("get cache {} failed", diamondContent, e);
            return null;
        }

        Object object = futureGet(diamondAxis, diamondContent, cachedObject, dynamicsHasCode);
        if (!(object instanceof Cache)) return object;

        Cache<Integer, Future<Object>> subCache = (Cache<Integer, Future<Object>>) object;

        Callable<Future<Object>> dynamicCallable = getSecondFutureCallable(diamondAxis, diamondContent, dynamics);
        Future<Object> subCachedObject;
        try {
            subCachedObject = subCache.get(dynamicsHasCode, dynamicCallable);
        } catch (ExecutionException e) {
            log.error("get dynamic cache {} failed", diamondContent, e);
            return null;
        }

        return futureGet(diamondAxis, diamondContent, subCachedObject, dynamicsHasCode);
    }

    private Callable<Future<Object>> getSecondFutureCallable(final DiamondStone.DiamondAxis diamondAxis,
                                                             final String diamondContent,
                                                             final Object[] dynamics) {
        return new Callable<Future<Object>>() {
            @Override
            public Future<Object> call() throws Exception {
                return executorService.submit(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        Callable<Object> updater = createUpdater(diamondContent, diamondAxis, dynamics);
                        if (updater == null) return null;

                        return updateCache(updater, diamondAxis, diamondContent, dynamics);
                    }
                });
            }
        };
    }

    private Callable<Future<Object>> createFirstFutureCallable(final DiamondStone.DiamondAxis diamondAxis,
                                                               final String diamondContent,
                                                               final Object[] dynamics) {
        return new Callable<Future<Object>>() {
            @Override
            public Future<Object> call() throws Exception {
                return executorService.submit(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        Callable<Object> updater = createUpdater(diamondContent, diamondAxis, dynamics);
                        if (updater == null) return null;

                        if (isDynamicApplicable(updater)) {
                            return CacheBuilder.newBuilder().build();
                        } else {
                            return updateCache(updater, diamondAxis, diamondContent, dynamics);
                        }
                    }
                });
            }
        };
    }

    private Object futureGet(DiamondStone.DiamondAxis diamondAxis,
                             String diamondContent, Future<Object> future, int dynamicsHasCode) {
        try {
            return future.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException e) {   // 有限时间内不返回，尝试读取snapshot版本
            log.error("update cache {} timeout, try to use snapshot", diamondContent);
            Optional<Object> object = snapshotMiner.getCache(diamondAxis, dynamicsHasCode);
            if (object != null) return object.orNull();
        } catch (Exception e) {
            log.error("update cache {} failed", diamondContent, e);
        }

        try {
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            log.error("update cache {} failed", diamondContent, e);
        }

        return null;
    }

    private Object updateCache(Callable<Object> updater,
                               DiamondStone.DiamondAxis diamondAxis,
                               String diamondContent,
                               Object... dynamics) {
        int dynamicsHasCode = Arrays.deepHashCode(dynamics);

        log.info("start to update cache {}", diamondAxis);
        if (isEmpty(diamondContent)) return null;

        Object diamondCache = null;
        try {
            diamondCache = updater.call();
        } catch (Exception e) {
            log.error("{} called with exception", diamondContent, e);
            snapshotMiner.removeCache(diamondAxis, dynamicsHasCode);
        }

        snapshotMiner.saveCache(diamondAxis, diamondCache, dynamicsHasCode);
        log.info("end to update cache {}", diamondAxis);

        return diamondCache;
    }


    private Callable<Object> createUpdater(String diamondContent,
                                           DiamondStone.DiamondAxis diamondAxis,
                                           Object... dynamics) {
        Spec spec;
        int dynamicsHasCode = Arrays.deepHashCode(dynamics);

        try {
            String substitute = DiamondSubstituter.substitute(diamondContent, true);
            spec = SpecParser.parseSpecLeniently(substitute);
        } catch (Exception e) {
            log.error("parse {} failed", diamondContent, e);
            snapshotMiner.removeCache(diamondAxis, dynamicsHasCode);
            return null;
        }

        Object object = Reflect.on(spec.getName()).create().get();
        if (!(object instanceof Callable)) {
            log.error("{} cannot be parsed as Callable", diamondContent);
            snapshotMiner.removeCache(diamondAxis, dynamicsHasCode);
            return null;
        }

        if (object instanceof ParamsAppliable)
            ((ParamsAppliable) object).applyParams(spec.getParams());

        if (object instanceof DynamicsAppliable)
            ((DynamicsAppliable) object).setDynamics(dynamics);

        return (Callable) object;
    }

    public void close() {
        executorService.shutdownNow();
    }

    public Future<Object> updateDiamondCacheOnChange(final DiamondStone.DiamondAxis diamondAxis,
                                                     final String diamondContent) {
        Future<Object> cacheOptional = cache.getIfPresent(diamondAxis);
        if (cacheOptional == null) return cacheOptional;

        Callable<Object> task = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Callable<Object> updater = createUpdater(diamondContent, diamondAxis);
                if (updater == null) return null;

                if (isDynamicApplicable(updater)) {
                    removeCacheSnapshot(diamondAxis);
                    return null;
                } else {
                    final Object updated = updateCache(updater, diamondAxis, diamondContent);
                    cache.put(diamondAxis, Futures.immediateFuture(updated));
                    return updated;
                }
            }
        };

        return executorService.submit(task);
    }

    private boolean isDynamicApplicable(Callable<Object> updater) {
        return updater instanceof DynamicsAppliable;
    }

    public void removeCacheSnapshot(DiamondStone.DiamondAxis diamondAxis) {
        cache.invalidate(diamondAxis);
        snapshotMiner.removeAllCache(diamondAxis);
    }
}
