package paging_table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagingTableRow {
    private int frameId;
    private boolean isPresentInPrimaryMemory;
    private boolean isModified;
}
