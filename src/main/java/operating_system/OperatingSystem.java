package operating_system;

import configuration.Configuration;
import memory.PrimaryMemory;
import memory.SecondaryMemory;
import paging_table.PagingTable;
import paging_table.PagingTableRow;
import process.Instruction;
import process.Process;
import process.ProcessPage;
import util.ReadFile;
import util.UXClass;

import java.util.*;

public class OperatingSystem {
    private final PrimaryMemory primaryMemory;
    private final SecondaryMemory secondaryMemory;
    private final ProcessController processController;
    private final HashMap<String, Queue<String>> processInstructions;

    public OperatingSystem() {
        this.primaryMemory = new PrimaryMemory(Configuration.PRIMARY_MEMORY_SIZE, Configuration.NUMBER_OF_FRAMES_PRIMARY_MEMORY);
        this.secondaryMemory = new SecondaryMemory(Configuration.SECONDARY_MEMORY_SIZE, Configuration.NUMBER_OF_FRAMES_SECONDARY_MEMORY);
        processController = new ProcessController();

        processInstructions = ReadFile.read();

        createProcessStarting();

    }

    public boolean isProcessAlreadyCreated(String id) {
        boolean alreadyCreated = primaryMemory.getPagingTables().containsKey(id);

        if (alreadyCreated) {
            System.out.println("Process " + id + " already exists.");
        }

        return alreadyCreated;
    }
    private boolean isLogicalAddressBiggerThanProcess (int logicalAddress, PagingTable pagingTable) {
        if (logicalAddress > pagingTable.getProcessOwner().getSize()) {
            System.out.println("Logical address is bigger than process size");
            return true;
        }
        return false;
    }

    private boolean isBinaryAddressBiggerThanBits (String binaryAddress) {
        if (binaryAddress.length() > Configuration.LOGICAL_ADDRESS_BITS) {
            System.out.println("Logical address size is bigger than " + Configuration.LOGICAL_ADDRESS_BITS + " bits.");
            return true;
        }
        return false;
    }

    private boolean isProcessSizeBiggerThanMemory (int size) {
        if (size > Configuration.SECONDARY_MEMORY_SIZE) {
            System.out.println("Process size is bigger than memory size.");
            return true;
        }
        return false;
    }

    private boolean isNewProcess(String id) {
        boolean isNewProcess = !primaryMemory.getPagingTables().containsKey(id);

        if (isNewProcess) {
            System.out.println("Process " + id + " already exists.");
        }

        return isNewProcess;
    }

    private boolean isProcessPagesMoreThanMemoryPages (int pagesNeeded) {
        if (pagesNeeded > secondaryMemory.getFreeFrames()) {
            System.out.println("Process pages are more than memory pages.");
            return true;
        }
        return false;
    }

    private void createProcessStarting() {
        // Recorrer el hashmap de procesos

        for (String processId: processInstructions.keySet()) {
            // Se obtiene la instrucción de creacion, que es la primera
            String instruction = processInstructions.get(processId).poll();

            // Se separa la instrucción por espacios
            String[] instructionArray = instruction.split(" ");

            // Se obtiene el tamaño del proceso
            int size = Integer.parseInt(instructionArray[1]);

            // Se crea el proceso
            createProcess(processId, size);

        }

    }

    public void createProcessByUserInput(String id, int size, Queue<String> instructions) {
        createProcess(id, size);
        processInstructions.put(id, instructions);
    }

    public void createProcess(String id, int size) {

        System.out.println("Creating process " + id);

        if (isProcessAlreadyCreated(id) || isProcessSizeBiggerThanMemory(size)) return;

        int pagesNeeded = (int) Math.ceil((double) size / Configuration.PAGE_SIZE);

        if (isProcessPagesMoreThanMemoryPages(pagesNeeded)) return;

        Process process = new Process(id, size);
        List<ProcessPage> pagesFromProcess = new ArrayList<>();


        for (int pageIndex = 0; pageIndex < pagesNeeded; pageIndex++) {

            int sizeProcessPage;
            if (pageIndex == pagesNeeded - 1) {
                sizeProcessPage = size - (pageIndex * Configuration.PAGE_SIZE);
            } else {
                sizeProcessPage = Configuration.PAGE_SIZE;
            }

            pagesFromProcess.add(new ProcessPage(process, pageIndex, sizeProcessPage));
        }

        process.setPages(pagesFromProcess);


        System.out.println("\tPages needed: " + pagesNeeded);

        UXClass.pressToContinue();

        System.out.println("\nAdding the process pages to the secondary memory...");

        // Agregar los processPages a la memoria secundaria
        for (ProcessPage processPage : process.getPages()) {
            secondaryMemory.addPage(processPage);
        }

        secondaryMemory.showMemory();

        UXClass.pressToContinue();

        // Se crea la tabla de páginas del proceso
        PagingTable pagingTable = new PagingTable(process, pagesNeeded);

        System.out.println("\nCreating the paging table and adding it to the primary memory");
        pagingTable.showTable();

        UXClass.pressToContinue();

        // Se agrega la tabla de páginas del proceso a la memoria principal
        primaryMemory.addPagingTable(pagingTable);

        processController.addProcess(process);

        System.out.println("\nProcess created!");
        System.out.println("Now, the first page of the process is going to be brought to the primary memory...");

        UXClass.pressToContinue();

        Kernel.addProcessPageToPrimaryMemory(pagingTable, 0, 0, secondaryMemory, primaryMemory, processController);
    }

    private void dealWithPageFault(String id, int logicalAddress) {

        System.out.println("\t\tBringing the fault page to the primary memory...");

        PagingTable pagingTable = primaryMemory.getPagingTables().get(id);

        String binaryAddress = Integer.toBinaryString(logicalAddress);
        while (binaryAddress.length() < Configuration.LOGICAL_ADDRESS_BITS) {
            binaryAddress = "0" + binaryAddress;
        }

        if (isBinaryAddressBiggerThanBits(binaryAddress)) return;

        // Obtener el page number y offset del logical address
        int pageNumber = Integer.parseInt(binaryAddress.substring(0, Configuration.BITS_PAGE_NUMBER), 2);

        System.out.println("\t\tPage number to bring: " + pageNumber);

        Kernel.addProcessPageToPrimaryMemory(pagingTable, pageNumber, logicalAddress, secondaryMemory, primaryMemory, processController);

        System.out.println("\n\t\tThe page fault was solved!");

        System.out.println("Now, the process " + id + " is going to be unblocked!");

    }

    private void executeInstruction(String id, Instruction intruction, int logicAddress) {

        PagingTable pagingTable = primaryMemory.getPagingTables().get(id);

        if (isNewProcess(id)) return;

        if (isLogicalAddressBiggerThanProcess(logicAddress, pagingTable)) return;

        // Obtener el logical address en base 2
        String binaryAddress = Integer.toBinaryString(logicAddress);
        while (binaryAddress.length() < Configuration.LOGICAL_ADDRESS_BITS) {
            binaryAddress = "0" + binaryAddress;
        }

        System.out.println("\tLogical address: " + binaryAddress);

        if (isBinaryAddressBiggerThanBits(binaryAddress)) return;

        // Obtener el page number y offset del logical address
        int pageNumber = Integer.parseInt(binaryAddress.substring(0, Configuration.BITS_PAGE_NUMBER), 2);
        int offset = Integer.parseInt(binaryAddress.substring(Configuration.BITS_PAGE_NUMBER), 2);

        System.out.println("\tPage number: " + pageNumber);
        System.out.println("\tOffset: " + offset);

        System.out.println("\nNow, let's look for the page " + pageNumber + " in the paging table of the process " + id);

        // =====================
        UXClass.pressToContinue();
        // =====================
        System.out.println("Looking for the page " + pageNumber + " in the paging table of the process " + id);
        pagingTable.showTable();

        // Verificar si la página está en memoria principal
        PagingTableRow row = pagingTable.getTable().get(pageNumber);
        if (row.isPresentInPrimaryMemory()) {

            processInstructions.get(id).poll();

            System.out.println("\t\tPage was found in the primary memory!");

            System.out.println("\t\tThe instruction is being executing...");

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (intruction == Instruction.PROCESS) {
                System.out.println("\n\t\t\t\tThe instruction is being processing...");
            } else if (intruction == Instruction.IN_OUT) {
                System.out.println("\n\t\t\t\tThe instruction is being analyzed by the I/O device...");
            } else if (intruction == Instruction.READ) {
                System.out.println("\n\t\t\t\tThe instruction is being read...");
            } else if (intruction == Instruction.WRITE) {
                System.out.println("\n\t\t\t\tThe instruction is being written...");
            }

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println("\n\n\t\tThe instruction was executed!");

            UXClass.pressToContinue();

            System.out.println("\n\tSo, the cache and the paging table are updated...");
            // =====================
            UXClass.pressToContinue();
            // =====================

            primaryMemory.updateProcessPage(row.getFrameId(), id, pageNumber, intruction == Instruction.WRITE);
            System.out.println("\nLRU Cache is updated...");
            primaryMemory.showCacheProcessPages();
            // =====================
            UXClass.pressToContinue();
            // =====================
            System.out.println("\nPaging table is updated...");
            pagingTable.showTable();

            return;
        }

        System.out.println("Page does not found in the primary memory!");

        System.out.println("\t\t\t PAGE FAULT!");

        System.out.println("The process " + id + " is going to be blocked!");

        List<String> idProcessesInMP = primaryMemory.getProcessesInMemory();

        processController.addProcessToBlockedQueueFromExecuting(idProcessesInMP);
    }

    public void executeNextInstruction() {

        System.out.println("Ready queue: "+ processController.getReadyQueue());
        System.out.println("Ready-suspended queue: "+ processController.getReadySuspendedQueue());
        System.out.println("Blocked queue: "+ processController.getBlockedQueue());
        System.out.println("Blocked-suspended queue: "+ processController.getBlockedSuspendedQueue());
        System.out.println("Process running: "+ processController.getRunningProcess());

        String processId = processController.getTheSettingRunningProcess();
        String instruction;
        boolean isObtainedFromBlocked = false;

        if (!processId.isEmpty()) {
            System.out.println("\nNext process instruction to execute: " + processId);
            instruction = processInstructions.get(processId).peek();
        } else {

            System.out.println("\n\tThere are not processes in the ready queue");
            System.out.println("\tSo, we are going to get a process from the blocked queue");

            processId = processController.getTheBlockedProcess();
            System.out.println("\nProcess from blocked queue: " + processId);
            instruction = processInstructions.get(processId).peek();

            isObtainedFromBlocked = true;
        }

        UXClass.pressToContinue();

        // Impresión del proceso con su instrucción
        System.out.println("Instruction to execute: " + processId + " " + instruction);

        // Se separa la instrucción por espacios
        String[] instructionArray = instruction.split(" ");

        // Se obtiene el flag de la instrucción
        String flag = instructionArray[0];

        // Se obtiene la dirección lógica de la instrucción
        int logicAddress = (instructionArray.length > 1) ? Integer.parseInt(instructionArray[1]) : 0;

        if (isObtainedFromBlocked) {

            System.out.println("\nLet's deal with the page fault of the process " + processId);

            // =====================
            UXClass.pressToContinue();
            // =====================

            dealWithPageFault(processId, logicAddress);
            return;
        }
        // Se ejecuta la instrucción
        switch (flag) {
            case "P":
                process(processId, logicAddress);
                break;
            case "R":
                read(processId, logicAddress);
                break;
            case "W":
                write(processId, logicAddress);
                break;
            case "I":
                processInOut(processId, logicAddress);
                break;
            case "T":
                finishProcess(processId);
                break;
        }
    }

    public void process(String id, int logicalAddress) {
        executeInstruction(id, Instruction.PROCESS, logicalAddress);
    }

    public void processInOut(String id, int logicalAddress) {
        executeInstruction(id, Instruction.IN_OUT, logicalAddress);
    }

    public void write(String id, int logicalAddress) {
        executeInstruction(id, Instruction.WRITE, logicalAddress);
    }

    public void read(String id, int logicalAddress) {
        executeInstruction(id, Instruction.READ, logicalAddress);
    }

    public void finishProcess(String id) {
        if (isNewProcess(id)) return;

        primaryMemory.removeProcessPageByProcessId(id);
        secondaryMemory.removeProcessPageByProcessId(id);
        processInstructions.remove(id);
        processController.removeProcess(id);
    }

    public void showPrincipalMemory() {
        System.out.println("Principal Memory:");
        primaryMemory.showMemory();
    }

    public void showSecondaryMemory() {
        System.out.println("Secondary Memory:");
        secondaryMemory.showMemory();
    }

    public void showPagingTables() {
        primaryMemory.showTables();
    }

    public void showQueues() {
        processController.showQueues();
    }

    public void showProcesses() {
        processController.showProcesses();
    }

    public boolean isAnyProcessInSystem() {
        return !processController.getProcessList().isEmpty();
    }
}
