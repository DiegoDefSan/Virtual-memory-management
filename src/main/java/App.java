import configuration.Configuration;
import operating_system.OperatingSystem;
import util.UXClass;

import java.util.LinkedList;
import java.util.Scanner;

public class App {
    private final OperatingSystem operatingSystem;

    public App() {
        operatingSystem = new OperatingSystem();
    }

    private int menuStarting() {
        Scanner scanner = new Scanner(System.in);
        int option;
        do {
            System.out.println("\n\t\t===== Menu =====");
            System.out.println("\t1. Execute next instruction");
            System.out.println("\t2. Create process");
            System.out.println("\t3. Show memory");
            System.out.println("\t4. Show paging table");
            System.out.println("\t5. Show queues");
            System.out.println("\t6. Show processes");
            System.out.println("\t0. Exit");
            System.out.print("\tOption: ");
            option = scanner.nextInt();
            System.out.println();
        } while (!(option >= 0 && option <= 6));
        return option;
    }

    private boolean verifyUserInputInstruction(String instruction, int processSize) {

        String[] instructionParts = instruction.split(" ");

        if (instructionParts.length == 0) {
            System.out.println("Invalid instruction. You must enter a valid instruction.");
            return false;
        }

        if (instructionParts.length > 3) {
            System.out.println("Invalid instruction. You must enter a valid instruction.");
            return false;
        }

        if (instructionParts.length == 1 && !instructionParts[0].equals("T")) {
            System.out.println("Invalid instruction. You must enter a valid instruction.");
            return false;
        }

        if (instructionParts.length == 2) {
            if (!instructionParts[0].equals("P") && !instructionParts[0].equals("I") && !instructionParts[0].equals("R") && !instructionParts[0].equals("W")) {
                System.out.println("Invalid instruction. You must enter a valid instruction.");
                return false;
            }

            int logicalAddress = Integer.parseInt(instructionParts[1]);
            if (logicalAddress > processSize) {
                System.out.println("Invalid instruction. You must enter a valid instruction.");
                return false;
            }
        }

        return true;

    }

    private void menuNewProcess() {
        Scanner scanner = new Scanner(System.in);
        String id;
        int size;

        System.out.print("Process ID: ");
        id = scanner.nextLine();
        System.out.print("Process size (Bytes): ");
        size = scanner.nextInt();

        LinkedList<String> instructions = new LinkedList<>();
        String instruction;

        System.out.println("Enter the instructions for the process. Enter 'T' to finish.");
        System.out.println("\t- P /logical addres/: Process instruction");
        System.out.println("\t- I /logical addres/: Process in/out instruction");
        System.out.println("\t- R /logical addres/: Read instruction");
        System.out.println("\t- W /logical addres/: Write instruction");

        scanner.nextLine();
        do {
            System.out.print("Instruction: ");
            instruction = scanner.nextLine();

            if (verifyUserInputInstruction(instruction, size)) {
                instructions.add(instruction);
            }

        } while (!instruction.equals("T"));

        operatingSystem.createProcessByUserInput(id, size, instructions);
    }

    private void showMemories() {
        operatingSystem.showPrincipalMemory();
        operatingSystem.showSecondaryMemory();
    }

    private void showPagingTables() {
        operatingSystem.showPagingTables();
    }

    public void init() {
        boolean running = true;
        int option;

        System.out.println("Memory details:");

        System.out.println("\tPrincipal Memory Size: " + Configuration.PRIMARY_MEMORY_SIZE);
        System.out.println("\tSecond Memory Size: " + Configuration.SECONDARY_MEMORY_SIZE);
        System.out.println("\tPage size: " + Configuration.FRAME_SIZE);
        System.out.println("\tBits virtual address: " + Configuration.LOGICAL_ADDRESS_BITS);

        while (running) {
            option = menuStarting();

            if (option == 0) {
                running = false;
                continue;
            }

            switch (option) {
                case 1:
                    if (operatingSystem.isAnyProcessInSystem())
                        operatingSystem.executeNextInstruction();
                    else {
                        System.out.println("\n\tThere are no processes in the system.");
                    }
                    break;
                case 2:
                    menuNewProcess();
                    break;
                case 3:
                    showMemories();
                    break;
                case 4:
                    showPagingTables();
                    break;
                case 5:
                    operatingSystem.showQueues();
                    break;
                case 6:
                    operatingSystem.showProcesses();
                    break;
            }

            UXClass.pressToContinue();
        }
    }
}
