package util;

import java.util.Scanner;

public class UXClass {
    public static void pressToContinue() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n\t\t\tPress any letter to continue...");
        scanner.nextLine();
    }

}
