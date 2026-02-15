package org.example;

import Models.User;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        System.out.println(" Test User");

        try {
            User user1 = User.validate("john_doe", "John Doe", "john@example.com");
            System.out.println("Success: " + user1.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Fail: " + e.getMessage());
        }

        try {
            User user2 = User.validate("jo", "John Doe", "john@example.com");
            System.out.println("Success: " + user2.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Fail: " + e.getMessage());
        }

        try {
            User user3 = User.validate("thisusernameiswaytoolong123", "John Doe", "john@example.com");
            System.out.println("Success: " + user3.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Fail: " + e.getMessage());
        }

        try {
            User user4 = User.validate("john@doe", "John Doe", "john@example.com");
            System.out.println("Success: " + user4.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Fail: " + e.getMessage());
        }

        try {
            User user5 = User.validate("джон_доу", "John Doe", "john@example.com");
            System.out.println("Success: " + user5.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Fail: " + e.getMessage());
        }

        try {
            User user6 = User.validate("jane_doe", "Jane Doe", "jane.example.com");
            System.out.println("Success: " + user6.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Fail: " + e.getMessage());
        }

        try {
            User user7 = User.validate("bob", "Bob Smith", "bob@example");
            System.out.println("Success: " + user7.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Fail: " + e.getMessage());
        }

        try {
            User user8 = User.validate("alice", "Alice Wonder", "alice@");
            System.out.println("Success: " + user8.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Fail: " + e.getMessage());
        }

        try {
            User user9 = User.validate("charlie", "", "charlie@example.com");
            System.out.println("Success: " + user9.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Fail: " + e.getMessage());
        }
    }
}
