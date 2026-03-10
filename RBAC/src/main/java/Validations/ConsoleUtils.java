package Validations;

import Models.*;
import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_CYAN = "\u001B[36m";

    private ConsoleUtils() {
    }

    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(ANSI_CYAN + "? " + ANSI_RESET + message + ": ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty() && !required) {
                return "";
            }

            if (input.isEmpty() && required) {
                System.out.println(ANSI_RED + " This field is required." + ANSI_RESET);
                continue;
            }

            return input;
        }
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(ANSI_CYAN + "? " + ANSI_RESET + message + " (" + min + "-" + max + "): ");
            String input = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.println(ANSI_RED + " Please enter a number between " + min + " and " + max + ANSI_RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(ANSI_RED + " Please enter a valid number." + ANSI_RESET);
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(ANSI_CYAN + "? " + ANSI_RESET + message + " (yes/no): ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("yes") || input.equals("y")) {
                return true;
            } else if (input.equals("no") || input.equals("n")) {
                return false;
            } else {
                System.out.println(ANSI_RED + " Please enter 'yes' or 'no'." + ANSI_RESET);
            }
        }
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Options list cannot be empty");
        }

        System.out.println("\n" + ANSI_YELLOW + message + ANSI_RESET);

        for (int i = 0; i < options.size(); i++) {
            T option = options.get(i);
            String display;

            if (option instanceof User) {
                display = ((User) option).username();
            } else if (option instanceof Role) {
                display = ((Role) option).name();
            } else if (option instanceof Permission) {
                display = ((Permission) option).format();
            } else {
                display = option.toString();
            }

            System.out.printf("  %d. %s\n", i + 1, display);
        }

        int choice = promptInt(scanner, "Choose", 1, options.size());
        return options.get(choice - 1);
    }

    public static void printSuccess(String message) {
        System.out.println(ANSI_GREEN + " " + message + ANSI_RESET);
    }

    public static void printError(String message) {
        System.out.println(ANSI_RED + " " + message + ANSI_RESET);
    }

    public static void printWarning(String message) {
        System.out.println(ANSI_YELLOW + " " + message + ANSI_RESET);
    }

    public static void printInfo(String message) {
        System.out.println(ANSI_BLUE + " " + message + ANSI_RESET);
    }

    public static void printHeader(String title) {
        System.out.println(FormatUtils.formatHeader(title));
    }

    public static void waitForEnter(Scanner scanner) {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public static void clearScreen() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }
}