package Repositories.Interfaces;

import Repositories.Repo.RBACSystem;

import java.util.Scanner;

@FunctionalInterface
public interface Command {
    void execute(Scanner scanner, RBACSystem system);
}