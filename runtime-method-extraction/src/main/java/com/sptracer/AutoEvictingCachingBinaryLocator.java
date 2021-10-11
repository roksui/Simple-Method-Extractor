package com.sptracer;

import com.sptracer.util.ExecutorUtils;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.pool.TypePool;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoEvictingCachingBinaryLocator extends AgentBuilder.PoolStrategy.WithTypePoolCache {

    private final WeakConcurrentMap<ClassLoader, TypePool.CacheProvider> cacheProviders = new WeakConcurrentMap
            .WithInlinedExpunction<ClassLoader, TypePool.CacheProvider>();
    private final ScheduledExecutorService executorService;

    public AutoEvictingCachingBinaryLocator() {
        this(TypePool.Default.ReaderMode.EXTENDED);
    }

    public AutoEvictingCachingBinaryLocator(TypePool.Default.ReaderMode readerMode) {
        super(readerMode);
        executorService = Executors.newScheduledThreadPool(1, new ExecutorUtils.NamedThreadFactory("type-pool-cache-evicter"));
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                cacheProviders.clear();
            }
        }, 5, 1, TimeUnit.MINUTES);
    }

    @Override
    protected TypePool.CacheProvider locate(ClassLoader classLoader) {
        classLoader = classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
        TypePool.CacheProvider cacheProvider = cacheProviders.get(classLoader);
        while (cacheProvider == null) {
            cacheProvider = TypePool.CacheProvider.Simple.withObjectType();
            TypePool.CacheProvider previous = cacheProviders.putIfAbsent(classLoader, cacheProvider);
            if (previous != null) {
                cacheProvider = previous;
            }
        }
        return cacheProvider;
    }

    /**
     * Shuts down the internal thread pool
     */
    public void close() {
        executorService.shutdown();
    }
}
