package com.ck.utils;


import com.alibaba.fastjson.JSONObject;
import com.ck.function.FunctionUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 线程池工具类
 *
 * @author cyk
 * @since 2020-01-01
 */
public abstract class ThreadPoolUtil {
    private static final Logger log = Logger.getLogger(ThreadPoolUtil.class.getName());

    /**
     * 线程池大小
     */
    public static int ThreadPoolSize = 50;

    /**
     * 线程池
     */
    private static final ExecutorService threadPool = Executors.newScheduledThreadPool(ThreadPoolSize);

    /**
     * 启动线程执行指定方法
     *
     * @param keyExtractor
     * @param t
     * @param <T>
     * @param <R>
     */
    public static <T, R> void run(FunctionUtils.BaseFunction<? super T, ? extends R> keyExtractor, T t) {
        threadPool.execute(() -> keyExtractor.apply(t));
    }

    /**
     * 执行Runnable任务
     *
     * @param task
     */
    public static void run(Runnable task) {
        threadPool.execute(task);
    }

    /**
     * 线程休眠
     *
     * @param millis 毫秒值
     */
    public static void sleep(int millis) {
        try {
            if (millis > 0)
                Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.warning(String.format("Thread sleep Error:%s", e.getMessage()));
        }
    }

    /**
     * 获取线程循环执行
     *
     * @param task   任务
     * @param millis 每次循环线程休眠时长/毫秒值
     * @param onOff  循环终止开关
     */
    public static void runWhile(Runnable task, int millis, Boolean onOff) {
        threadPool.execute(() -> {
            while (onOff != null && onOff) {
                task.run();
                sleep(millis);
            }
        });
    }


    /**
     * 获取线程循环执行
     *
     * @param keyExtractor 需要执行的方法
     * @param t            目标对象实例
     * @param millis       每次循环线程休眠时长/毫秒值
     * @param onOff        循环终止开关
     */
    public static <T, R> void runWhile(FunctionUtils.BaseFunction<? super T, ? extends R> keyExtractor, T t, int millis, Boolean onOff) {
        threadPool.execute(() -> {
            while (onOff != null && onOff) {
                keyExtractor.apply(t);
                sleep(millis);
            }
        });
    }

    /**
     * 启动定时器
     * 循环执行
     *
     * @param task   任务
     * @param first  首次执行延迟/毫秒值
     * @param period 每次运行间隔/毫秒值
     * @return
     */
    public static Timer schedule(Runnable task, int first, int period) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, first, period);
        return timer;
    }

    /**
     * 启动定时器
     * 执行一次
     *
     * @param task  任务
     * @param first 首次执行延迟/毫秒值
     * @return
     */
    public static Timer schedule(Runnable task, int first) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, first);
        return timer;
    }

    /**
     * 创建 ThreadPoolExecutor 线程池
     *
     * @param threadName    创建的线程名称 默认 ThreadPool-Class.getSimpleName-线程序号
     * @param corePoolSize  最小线程数 默认2
     * @param maxPoolSize   最大线程数 默认6
     * @param keepAliveTime 大于最小线程数之外的闲置线程保持存活时间，单位毫秒 默认30000L
     * @param queryCapacity 任务阻塞队列长度 默认500
     * @param handler       最大线程数量及最大阻塞队列全部被占用是的处理逻辑，true 继续阻塞添加到队列，false 抛弃任务。默认true
     * @return
     */
    public static ThreadPoolExecutor createThreadPoolExecutor(String threadName, int corePoolSize, int maxPoolSize, long keepAliveTime, int queryCapacity, boolean handler) {
        AtomicInteger number = new AtomicInteger(0);
        if (threadName == null || "".equals(threadName.trim()))
            threadName = "ThreadPool-" + ThreadPoolUtil.class.getSimpleName();
        String finalThreadName = threadName;
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(queryCapacity), (Runnable runnable) -> new Thread(runnable, finalThreadName + "-" + number.getAndIncrement())
                , (Runnable runnable, ThreadPoolExecutor executor) -> {
            if (!handler) {
                log.info(String.format("Thread pool [%s] is exhausted, executor=%s", finalThreadName, executor.toString()));
            } else if (!executor.isShutdown()) {
                try {
                    // 添加一个元素， 如果队列满，则阻塞
                    executor.getQueue().put(runnable);
                } catch (InterruptedException e) {
                    // should not be interrupted
                }
            }
        });
    }

    /**
     * 批量任务执行
     *
     * @param tasks
     */
    public static void runBatchTask(Runnable... tasks) {
        ThreadPoolExecutor executor = createThreadPoolExecutor("BatchTask", 4, 8, 1000 * 30, 500, true);

        CompletableFuture<Void> result = CompletableFuture.allOf(Stream.of(tasks)
                .map(r -> CompletableFuture.runAsync(r, executor)).toArray(CompletableFuture[]::new));

        try {
            result.get();
        } catch (InterruptedException | ExecutionException e) {
            log.log(Level.WARNING, "CompletableFuture 批量任务执行异常", e);
            throw new RuntimeException("CompletableFuture 批量任务执行异常", e);
        }
        executor.shutdown();
    }

    /**
     * 批量任务执行
     *
     * @param collection
     * @param function
     * @param <T>
     */
    public static <T> void runBatchTask(Collection<T> collection, Consumer<? super T> function) {
        runBatchTask(8, collection, function);
    }

    /**
     * 批量任务执行
     *
     * @param maxPoolSize
     * @param collection
     * @param function
     * @param <T>
     */
    public static <T> void runBatchTask(int maxPoolSize, Collection<T> collection, Consumer<? super T> function) {

        if (collection == null || collection.isEmpty()) return;
        ThreadPoolExecutor executor = createThreadPoolExecutor("BatchTask", 2, maxPoolSize, 1000 * 30, 500, true);

        try {
            CompletableFuture<Void> result = CompletableFuture.allOf(
                    collection.stream().map(i -> CompletableFuture.runAsync(() ->
                            function.accept(i), executor)).toArray(CompletableFuture[]::new));

            result.get();
        } catch (InterruptedException | ExecutionException e) {
            log.log(Level.WARNING, "CompletableFuture 批量任务执行异常", e);
            throw new RuntimeException("CompletableFuture 批量任务执行异常", e);
        } finally {
            executor.shutdown();
        }
    }

    /**
     * 按照指定长度裁切List为多份，最后不满足长度的剩余数据为一份
     *
     * @param size   每份长度
     * @param source 原List集合
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> splitListBySize(int size, List<T> source) {
        if (source == null) return null;
        if (size < 1) size = source.size();

        int finalSize = size;
        long limit = (long) Math.ceil(source.size() / (double) size);
        return Stream.iterate(0, i -> i + 1).limit(limit)
                .map(i -> source.stream().skip(i * finalSize).limit(finalSize).collect(Collectors.toList())).collect(Collectors.toList());
    }

    /**
     * 将集合均分为指定的份数
     *
     * @param page
     * @param source
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> splitListByAverage(int page, List<T> source) {
        if (source == null) return null;
        if (page < 1 || page > source.size()) page = source.size();
        long limit = (source.size() + page - 1) / page;
        return Stream.iterate(0, n -> n + 1).limit(page)
                .map(a -> source.stream().skip(a * limit).limit(limit).collect(Collectors.toList())).collect(Collectors.toList());
    }


    public static void main(String[] args) {
        List<String> strings = Arrays.asList("字符串1", "字符串2", "字符串3", "字符串4", "字符串5", "字符串6", "字符串7", "字符串8", "字符串9");
        List<Integer> integers = Arrays.asList(4, 5, 6);
//        runBatchTask(strings, integers).action(System.out::println, System.out::println);
        List<List<String>> data = splitListBySize(100, strings);
        System.out.println(JSONObject.toJSONString(data));

        data = splitListByAverage(3, strings);
        System.out.println(data);

        runBatchTask(data, i -> {
            System.out.println(i.size());
        });

    }
}
