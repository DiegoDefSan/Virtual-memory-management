package memory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import process.ProcessPage;

import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Memory {
    protected int size;
    protected int frames;
    protected int freeFrames;
    protected int pointer;
    protected HashMap<Integer, ProcessPage> storage;

    public Memory(int size, int frames) {
        this.size = size;
        this.frames = frames;
        this.freeFrames = frames;
        this.pointer = 0;

        this.storage = new HashMap<>();
        for (int i = 0; i < this.frames; i++) {
            this.storage.put(i, null);
        }
    }

    protected abstract int addPage(ProcessPage processPage);
    public void showMemory() {
        System.out.println("\t\t┌─────────────────────────┐");
        for (int i = 0; i < frames; i++) {
            ProcessPage processPage = storage.get(i);
            System.out.println("\t\t│Frame " + i + ":                 │\n" + (processPage == null ? "\t\t│Free                     │" :
                    "\t\t│\tProcess ID: " + processPage.getProcessOwner().getId() + "        │\n" +
                    "\t\t│\tId process page: " + processPage.getId() + "    │"));
            System.out.println("\t\t├─────────────────────────┤");
        }
        System.out.println("\t\t└─────────────────────────┘\n");
    }

    public void searchForFreeFrame() {
        if (freeFrames == 0) {
            System.out.println("No free frames");
            return;
        }

        while (storage.get(pointer) != null) {
            pointer = (pointer + 1) % frames;
        }
    }
}
