import Models.*;
import Repositories.Repo.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTest {

    private static final int THREAD_COUNT = 10;
    private static final int OPERATIONS_PER_THREAD = 50;

    @Test
    public void testConcurrentOperations() throws InterruptedException {
        System.out.println("=== Starting Load Test ===\n");

        ThreadSafeUserManager userManager = new ThreadSafeUserManager();
        ThreadSafeRoleManager roleManager = new ThreadSafeRoleManager();
        ThreadSafeAssignmentManager assignmentManager = new ThreadSafeAssignmentManager(userManager, roleManager);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int t = 0; t < THREAD_COUNT; t++) {
            final int threadId = t;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                Random random = new Random();

                for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
                    try {
                        int operation = random.nextInt(3);

                        switch (operation) {
                            case 0:
                                String username = "user_" + threadId + "_" + i;
                                User user = User.validate(username, "Test User", username + "@test.com");
                                userManager.add(user);
                                successCount.incrementAndGet();
                                break;

                            case 1:
                                List<User> users = userManager.findAll();
                                if (!users.isEmpty()) {
                                    User randomUser = users.get(random.nextInt(users.size()));
                                    userManager.findByUsername(randomUser.username());
                                    successCount.incrementAndGet();
                                }
                                break;

                            case 2:
                                userManager.count();
                                successCount.incrementAndGet();
                                break;
                        }

                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        System.err.println("Error in thread " + threadId + ": " + e.getMessage());
                    }
                }
            }, executor);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long endTime = System.currentTimeMillis();

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("\n=== Load Test Results ===");
        System.out.println("Threads: " + THREAD_COUNT);
        System.out.println("Operations per thread: " + OPERATIONS_PER_THREAD);
        System.out.println("Total operations: " + (THREAD_COUNT * OPERATIONS_PER_THREAD));
        System.out.println("Successful: " + successCount.get());
        System.out.println("Failed: " + failureCount.get());
        System.out.println("Time: " + (endTime - startTime) + " ms");
        System.out.println("Final users count: " + userManager.count());
    }
}