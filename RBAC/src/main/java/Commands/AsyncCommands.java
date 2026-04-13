package Commands;

import Utils.BackgroundExecutor;
import Utils.ParallelReportGenerator;
import Repositories.Repo.*;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class AsyncCommands {

    private final BackgroundExecutor executor;
    private final ThreadSafeUserManager userManager;
    private final ThreadSafeAssignmentManager assignmentManager;

    public AsyncCommands(BackgroundExecutor executor,
                         ThreadSafeUserManager userManager,
                         ThreadSafeAssignmentManager assignmentManager) {
        this.executor = executor;
        this.userManager = userManager;
        this.assignmentManager = assignmentManager;
    }

    public void reportUsersAsync(Scanner scanner) {
        System.out.println("Starting background user report generation...");

        ParallelReportGenerator generator = new ParallelReportGenerator(userManager, assignmentManager);

        CompletableFuture<String> future = executor.submitTask(() ->
                generator.generateUserReportParallel()
        );

        future.thenAccept(result -> {
            System.out.println("\n=== Background Report Ready ===");
            System.out.println(result);
        }).exceptionally(ex -> {
            System.err.println("Error generating report: " + ex.getMessage());
            return null;
        });

        System.out.println("Report generation started in background. Check console later for results.");
    }

    public void saveDataAsync(Scanner scanner, String filename) {
        System.out.println("Starting background save operation...");

        CompletableFuture<Void> future = executor.submitRunnable(() -> {
            try {
                Thread.sleep(2000); // Simulate save operation
                System.out.println("Data saved to: " + filename);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        future.thenRun(() -> {
            System.out.println("Save operation completed for: " + filename);
        }).exceptionally(ex -> {
            System.err.println("Error saving data: " + ex.getMessage());
            return null;
        });

        System.out.println("Save started in background.");
    }
}