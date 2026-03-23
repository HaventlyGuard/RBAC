package test.java;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ThreadTest {
    private static final int NUMBER_OF_THREADS = 5;
    private static final int CALCULATION_LENGTH = 50;
    private static final int MIN_DELAY_MS = 50;
    private static final int MAX_DELAY_MS = 150;

    private final List<ProgressInfo> progressInfos = new ArrayList<>();
    private final AtomicBoolean calculationComplete = new AtomicBoolean(false);
    private final CountDownLatch completionLatch;

    public ThreadTest(int threadCount, int calculationLength) {
        this.completionLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            progressInfos.add(new ProgressInfo(i + 1, calculationLength));
        }
    }

    public void start() {
        System.out.println("Многопоточный расчёт начат");
        System.out.println("Количество потоков: " + progressInfos.size());
        System.out.println("Длина расчёта: " + CALCULATION_LENGTH + " шагов");
        System.out.println("=".repeat(80));

        ExecutorService executor = Executors.newFixedThreadPool(progressInfos.size());


        for (int i = 0; i < progressInfos.size(); i++) {
            final int threadIndex = i;
            executor.submit(() -> calculate(threadIndex));
        }

        Thread displayThread = new Thread(this::updateDisplay);
        displayThread.setDaemon(true);
        displayThread.start();

        try {
            completionLatch.await();
            calculationComplete.set(true);
            Thread.sleep(200);

            System.out.println("\n" + "=".repeat(80));
            System.out.println("Все расчёты завершены!\n");

            for (ProgressInfo info : progressInfos) {
                System.out.printf("Поток #%d (ID: %d) - Время: %.2f сек%n",
                        info.getThreadNumber(),
                        info.getThreadId(),
                        info.getElapsedTime() / 1000.0);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdownNow();
        }
    }

    private void calculate(int threadIndex) {
        ProgressInfo info = progressInfos.get(threadIndex);
        info.setThreadId(Thread.currentThread().getId());
        info.setStartTime(System.currentTimeMillis());

        Random random = new Random();

        for (int step = 1; step <= CALCULATION_LENGTH; step++) {
            try {
                int delay = MIN_DELAY_MS + random.nextInt(MAX_DELAY_MS - MIN_DELAY_MS + 1);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            info.setProgress(step);
        }

        info.setEndTime(System.currentTimeMillis());
        completionLatch.countDown();
    }

    private void updateDisplay() {
        int lastHash = 0;

        while (!calculationComplete.get()) {
            System.out.print("\r\033[" + (progressInfos.size() * 3 + 5) + "A");

            System.out.println("Многопоточный расчёт (в процессе)");
            System.out.println("=".repeat(80));

            for (ProgressInfo info : progressInfos) {
                System.out.printf("Поток #%d (ID: %d)%n",
                        info.getThreadNumber(), info.getThreadId());

                printProgressBar(info.getProgress(), CALCULATION_LENGTH);
                int percent = (info.getProgress() * 100) / CALCULATION_LENGTH;
                System.out.printf(" %d%%", percent);

                if (info.getProgress() >= CALCULATION_LENGTH) {
                    System.out.printf(" [Завершён за %.2f сек]%n", info.getElapsedTime() / 1000.0);
                } else {
                    System.out.println();
                }

                System.out.println();
            }

            System.out.println("=".repeat(80));

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void printProgressBar(int current, int total) {
        int barWidth = 50;
        int filledWidth = (current * barWidth) / total;

        System.out.print("[");
        for (int i = 0; i < barWidth; i++) {
            if (i < filledWidth) {
                System.out.print("=");
            } else if (i == filledWidth && current < total) {
                System.out.print(">");
            } else {
                System.out.print(" ");
            }
        }
        System.out.print("]");
    }

    private static class ProgressInfo {
        private final int threadNumber;
        private final int totalSteps;
        private volatile int progress;
        private volatile long threadId;
        private volatile long startTime;
        private volatile long endTime;

        public ProgressInfo(int threadNumber, int totalSteps) {
            this.threadNumber = threadNumber;
            this.totalSteps = totalSteps;
            this.progress = 0;
            this.threadId = 0;
            this.startTime = 0;
            this.endTime = 0;
        }

        public int getThreadNumber() { return threadNumber; }
        public int getProgress() { return progress; }
        public long getThreadId() { return threadId; }
        public long getElapsedTime() {
            return (endTime > 0 ? endTime : System.currentTimeMillis()) - startTime;
        }

        public void setProgress(int progress) { this.progress = progress; }
        public void setThreadId(long threadId) { this.threadId = threadId; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
    }

    public static void main(String[] args) {
        ThreadTest calculation = new ThreadTest(
                NUMBER_OF_THREADS,
                CALCULATION_LENGTH
        );
        calculation.start();
    }
}
