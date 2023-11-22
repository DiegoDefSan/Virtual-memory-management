package memory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import paging_table.PagingTable;
import process.ProcessPage;
import util.LRUCache;
import util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class PrimaryMemory extends Memory {

    private LRUCache<Integer, Pair<String, Integer>> cacheProcessPages;
    private HashMap<String, PagingTable> pagingTables;
    public PrimaryMemory(int size, int frames) {
        super(size, frames);

        this.cacheProcessPages = new LRUCache<>(frames);
        this.pagingTables = new HashMap<>();
    }

    @Override
    public int addPage(ProcessPage processPage) {
        searchForFreeFrame();

        storage.put(pointer, processPage);
        cacheProcessPages.put(pointer, new Pair<>(processPage.getProcessOwner().getId(), processPage.getId()));

        freeFrames--;

        return pointer;
    }

    public void addPagingTable(PagingTable pagingTable) {
        pagingTables.put(pagingTable.getProcessOwner().getId(), pagingTable);
    }

    public void updateProcessPage(Integer frameNumber, String idProcess, int pageNumber, boolean isModified) {
        cacheProcessPages.get(frameNumber); // Actualizar el cache
        pagingTables.get(idProcess).setRowInformation(pageNumber, frameNumber, true, isModified);
    }

    public void removeProcessPageByProcessId(String processId) {
        for (int i = 0; i < storage.size(); i++) {
            ProcessPage processPage = storage.get(i);
            if (processPage != null && processPage.getProcessOwner().getId().equals(processId)) {
                storage.put(i, null);
                cacheProcessPages.remove(i);
                freeFrames++;
            }
        }

        pagingTables.remove(processId);
    }

    public ProcessPage removeLastUsedProcessPage() {
        Integer keyElementLastUsed = cacheProcessPages.getEldestKeyUsed();

        ProcessPage processPage = storage.get(keyElementLastUsed);

        storage.put(keyElementLastUsed, null);
        cacheProcessPages.remove(keyElementLastUsed);

        freeFrames++;

        return processPage;
    }

    public List<String> getProcessesInMemory() {
        List<String> processesInMemory = new ArrayList<>();
        for (Map.Entry<Integer, Pair<String, Integer>> entry : cacheProcessPages.entrySet()) {
            processesInMemory.add(entry.getValue().getFirst());
        }
        return processesInMemory;
    }

    public void showCacheProcessPages() {
        System.out.println("\nCache Process Pages:");
        System.out.println("\t\t┌─────────────────────────┐");
        int eldestKeyUsed = cacheProcessPages.getEldestKeyUsed();

        for (Map.Entry<Integer, Pair<String, Integer>> entry : cacheProcessPages.entrySet()) {
            Pair<String, Integer> frameNumber = entry.getValue();
            System.out.println("\t\t│Frame " + entry.getKey() + ":                 │\n" + (frameNumber == null ? "\t\t│Free                     │" :
                    "\t\t│Process: " + frameNumber.getFirst() + "              │" +
                            "\n\t\t│Page: " + frameNumber.getSecond() + "                  │"));
            if (entry.getKey() == eldestKeyUsed) {
                System.out.println("\t\t│          LRU            │");
            }
            System.out.println("\t\t├─────────────────────────┤");
        }
        System.out.println("\t\t└─────────────────────────┘\n");
    }

    public void showTables() {
        System.out.println("\nPaging Tables:");
        for (PagingTable pagingTable : pagingTables.values()) {
            pagingTable.showTable();
        }
    }
}
