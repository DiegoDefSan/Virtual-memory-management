package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class ReadFile {
    private static final String path = "src/main/java/configuration/data.txt";
    private static final HashMap<String, Queue<String>> processInstructions = new HashMap<>();

    public static HashMap<String, Queue<String>> read() {
        File file = new File(path);
        try {
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                String[] inputArray = line.split(" ");
                String processId = inputArray[0];
                String flag = inputArray[1];
                String address = (!flag.equals("T")) ? inputArray[2] : "";

                String instruction = flag + " " + address;

                if (processInstructions.containsKey(processId)) {
                    processInstructions.get(processId).add(instruction);
                } else {
                    Queue<String> instructions = new LinkedList<>();
                    instructions.add(instruction);
                    processInstructions.put(processId, instructions);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return processInstructions;
    }
}
