package paging_table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import process.Process;

import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagingTable {
    private HashMap<Integer, PagingTableRow> table;
    private Process processOwner;

    public PagingTable(Process processOwner, int pagesNeeded) {
        this.processOwner = processOwner;
        this.table = new HashMap<>();
        for (int i = 0; i < pagesNeeded; i++) {
            this.table.put(i, new PagingTableRow(-1, false, false));
        }
    }

    public void setRowInformation(int pageAddress, int frameAddress, boolean isPresentInPrimaryMemory, boolean isModified) {
        PagingTableRow row = table.get(pageAddress);
        row.setFrameId(frameAddress);
        row.setPresentInPrimaryMemory(isPresentInPrimaryMemory);
        row.setModified(isModified);
    }

    public void showTable() {
        System.out.println("\t┌───────────────────────────────────────────────────────────┐");
        System.out.println("\t│\t\t\tPaging Table for Process " + processOwner.getId() + "\t\t\t\t\t    │");
        System.out.println("\t├───────────────────────────────────────────────────────────┤");
        System.out.println("\t│\tPage\t│\tIn Main Memory\t│\tIs Modified\t│\tFrame\t│");
        for (int i = 0; i < table.size(); i++) {
            PagingTableRow row = table.get(i);
            System.out.println("\t│\t" + i + "\t\t│\t" + row.isPresentInPrimaryMemory() + "\t\t\t│\t" + row.isModified() + "\t\t│\t" + row.getFrameId() + "\t\t│");
        }
        System.out.println("\t└───────────────────────────────────────────────────────────┘\n");
    }
}
