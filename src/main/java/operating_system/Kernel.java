package operating_system;

import configuration.Configuration;
import memory.PrimaryMemory;
import memory.SecondaryMemory;
import paging_table.PagingTable;
import paging_table.PagingTableRow;
import process.ProcessPage;
import util.UXClass;


public class Kernel {

    private static void verifyIfProcessIsStillReady(String processId, PrimaryMemory primaryMemory, ProcessController processController) {
        // Si el proceso ya no tiene páginas en memoria principal, se debe sacar de la ready queue

        System.out.println("\n\tVerifying if the process with id " + processId + " is still in MP...");

        // Se obtiene la paging table de la memoria principal
        PagingTable pagingTable = primaryMemory.getPagingTables().get(processId);
        boolean processIsStillInPrimaryMemory = false;

        // Se recorre la paging table para verificar si el proceso aún tiene páginas en memoria principal
        for (PagingTableRow row : pagingTable.getTable().values()) {
            if (row.isPresentInPrimaryMemory()) {
                processIsStillInPrimaryMemory = true;
                break;
            }
        }

        System.out.println("\n\tThe process with id " + processId + " is still in MP: " + processIsStillInPrimaryMemory);

        // Si el proceso ya no tiene páginas en memoria principal, se debe sacar de la ready queue y suspenderlo
        if (!processIsStillInPrimaryMemory) {
            System.out.println("\n\tThe process with id " + processId + " was suspended.");
            processController.suspendProcess(processId);
        }

        UXClass.pressToContinue();
    }
    private static void swapProcessPageToSecondMemory(PrimaryMemory primaryMemory, SecondaryMemory secondaryMemory, ProcessController processController) {
        // Si no hay frames libres, se debe hacer un reemplazo
        // Se debe buscar el processPage que se usó último y removerlo de la memoria principal
        // Luego, agregarlo a la memoria secundaria
        // Y modificar la paging table del proceso

        primaryMemory.showCacheProcessPages();

        System.out.println("\nThe last recently used process page will be swapped to secondary memory...");

        UXClass.pressToContinue();

        ProcessPage processPageRemoved = primaryMemory.removeLastUsedProcessPage();
        System.out.println("\nThe process page with id " +  processPageRemoved.getProcessOwner().getId() + " - " + processPageRemoved.getId()  +  " was removed from primary memory.");

        primaryMemory.showMemory();

        UXClass.pressToContinue();

        System.out.println("\n\tThe process page with id " + processPageRemoved.getProcessOwner().getId() + " - " + processPageRemoved.getId() + " is added to secondary memory.");
        secondaryMemory.addPage(processPageRemoved);

        secondaryMemory.showMemory();

        UXClass.pressToContinue();

        // Se debe modificar la paging table del proceso
        System.out.println("\n\tThe paging table of the process removed, with id " + processPageRemoved.getProcessOwner().getId() + ", was modified.");
        PagingTable pagingTable = primaryMemory.getPagingTables().get(processPageRemoved.getProcessOwner().getId());

        // Se debe buscar la fila de la paging table que corresponde al processPage que se removió
        // Y se debe modificar el frameId y el inMainMemory
        pagingTable.setRowInformation(processPageRemoved.getId(), -1, false, false);

        pagingTable.showTable();

        UXClass.pressToContinue();

        // Se debe verificar si el proceso aún tiene páginas en memoria principal, si no, se debe sacar de la ready queue
        verifyIfProcessIsStillReady(processPageRemoved.getProcessOwner().getId(), primaryMemory, processController);

        System.out.println("\n\tThe paging table of the process with id " + processPageRemoved.getProcessOwner().getId() + " was modified.");
        pagingTable.showTable();

        primaryMemory.getPagingTables().put(processPageRemoved.getProcessOwner().getId(), pagingTable);
    }
    private static void saveProcessPageInPrimaryMemory(int pageNumber, ProcessPage processPageToAdd,
                                                       PrimaryMemory primaryMemory, SecondaryMemory secondaryMemory, ProcessController processController) {

        if (primaryMemory.getFreeFrames()==0) {

            primaryMemory.showMemory();
            System.out.println("\n\tNo empty frames in primary memory. Swapping...");
            UXClass.pressToContinue();

            swapProcessPageToSecondMemory(primaryMemory, secondaryMemory, processController);
        }

        System.out.println("Primary memory before adding the page");
        primaryMemory.showMemory();

        // =====================
        UXClass.pressToContinue();
        // =====================

        int frameInPrincipalMemory = primaryMemory.addPage(processPageToAdd);
        System.out.println("Primary memory after adding the page");
        primaryMemory.showMemory();

        UXClass.pressToContinue();

        // Get paging table of process
        PagingTable pagingTable = primaryMemory.getPagingTables().get(processPageToAdd.getProcessOwner().getId());

        System.out.println("\n\tThe paging table of the process with id " + processPageToAdd.getProcessOwner().getId() + " was modified.");
        pagingTable.setRowInformation(pageNumber, frameInPrincipalMemory, true, false);

        pagingTable.showTable();

        primaryMemory.getPagingTables().put(processPageToAdd.getProcessOwner().getId(), pagingTable);
    }

    public static void addProcessPageToPrimaryMemory(PagingTable pagingTable, int pageNumber, int logicalAddress,
                                                     SecondaryMemory secondaryMemory, PrimaryMemory primaryMemory, ProcessController processController) {
        String idProcess = pagingTable.getProcessOwner().getId();


        // add page to primary memory from secondary memory
        // Necesito
        // 1. Buscar el process page en la memoria secundaria
        // ¿Cómo busco el process page en la memoria secundaria?
        // Necesito el offset del logical address
        // Este lo divido entre el tamaño de la página
        int processPageId = logicalAddress/Configuration.PAGE_SIZE;
        // Ese resultado es el id del process page
        // Entonces, busco el process page con ese id en la memoria secundaria mediante un bucle
        // Y lo saco de la memoria secundaria

        System.out.println("\nLet's remove the process page with id " + idProcess + " - " + processPageId + " from secondary memory.");

        System.out.println("\nThe secondary memory before: ");
        secondaryMemory.showMemory();

        // =====================
        UXClass.pressToContinue();
        // =====================

        ProcessPage processPageToAdd = secondaryMemory.freeFrameByProcessPageId(processPageId, idProcess);
        System.out.println("\nThe secondary memory after: ");
        secondaryMemory.showMemory();

        UXClass.pressToContinue();

        // 2. Agregar el process page a la memoria principal
        System.out.println("\nLet's add the process page with id " + idProcess + " - " + processPageId + " to primary memory.");
        saveProcessPageInPrimaryMemory(pageNumber, processPageToAdd, primaryMemory, secondaryMemory, processController);

        // =====================
        UXClass.pressToContinue();
        // =====================

        //
        if (!processController.getRunningProcess().equals(idProcess)) {
            processController.addProcessToReadyQueue(idProcess);
        }
    }
}
