package Repositories.Repo;

import Repositories.Interfaces.Command;

import java.util.*;

public class CommandParser {

    private final Map<String, Command> commands;
    private final Map<String, String> commandDescriptions;
    private final Map<String, List<String>> commandAliases;

    public CommandParser() {
        this.commands = new LinkedHashMap<>();
        this.commandDescriptions = new LinkedHashMap<>();
        this.commandAliases = new HashMap<>();
    }

    public void registerCommand(String name, String description, Command command) {
        commands.put(name.toLowerCase(), command);
        commandDescriptions.put(name.toLowerCase(), description);
    }

    public void registerCommandWithAliases(String name, List<String> aliases, String description, Command command) {
        registerCommand(name, description, command);
        commandAliases.put(name.toLowerCase(), aliases.stream().map(String::toLowerCase).toList());

        for (String alias : aliases) {
            commands.put(alias.toLowerCase(), command);
            commandDescriptions.put(alias.toLowerCase(), "Alias for: " + name);
        }
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        Command command = commands.get(commandName.toLowerCase());
        if (command == null) {
            System.out.println("Unknown command: '" + commandName + "'. Type 'help' for available commands.");
            return;
        }

        try {
            command.execute(scanner, system);
        } catch (Exception e) {
            System.out.println("Error executing command: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();

        executeCommand(commandName, scanner, system);
    }

    public void printHelp() {
        System.out.println("\nAvailable Commands\n");

        System.out.println("USER MANAGEMENT:");
        printCommandsInCategory("user");

        System.out.println("\nROLE MANAGEMENT:");
        printCommandsInCategory("role");

        System.out.println("\nASSIGNMENT MANAGEMENT:");
        printCommandsInCategory("assign");

        System.out.println("\nPERMISSION MANAGEMENT:");
        printCommandsInCategory("permission");

        System.out.println("\nSYSTEM COMMANDS:");
        printCommandsInCategory("system");

        System.out.println("\nSEARCH COMMANDS:");
        printCommandsInCategory("search");

        System.out.println("\nUse: <command> -? or <command> --help for command-specific help");
    }

    private void printCommandsInCategory(String category) {
        commands.entrySet().stream()
                .filter(entry -> !commandAliases.containsKey(entry.getKey()))
                .filter(entry -> entry.getKey().startsWith(category) ||
                        (category.equals("system") &&
                                (entry.getKey().equals("help") ||
                                        entry.getKey().equals("stats") ||
                                        entry.getKey().equals("clear") ||
                                        entry.getKey().equals("exit") ||
                                        entry.getKey().equals("save") ||
                                        entry.getKey().equals("load"))))
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String desc = commandDescriptions.get(entry.getKey());
                    System.out.printf("  %-25s - %s\n", entry.getKey(), desc);

                    List<String> aliases = commandAliases.get(entry.getKey());
                    if (aliases != null && !aliases.isEmpty()) {
                        System.out.printf("    aliases: %s\n", String.join(", ", aliases));
                    }
                });
    }

    public Set<String> getCommandNames() {
        return commands.keySet();
    }

    public boolean hasCommand(String name) {
        return commands.containsKey(name.toLowerCase());
    }
}