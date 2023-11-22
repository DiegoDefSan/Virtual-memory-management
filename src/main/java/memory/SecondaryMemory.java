package memory;

import process.ProcessPage;


public class SecondaryMemory extends Memory {
    public SecondaryMemory(int size, int frames) {
        super(size, frames);
    }

    @Override
    public int addPage(ProcessPage page) {
        searchForFreeFrame();

        storage.put(pointer, page);

        freeFrames--;

        return pointer;
    }

    public void removeProcessPageByProcessId(String processId) {
        for (int i = 0; i < frames; i++) {
            if (storage.get(i) == null) continue;
            ProcessPage processPage = storage.get(i);
            if (processPage != null && processPage.getProcessOwner().getId().equals(processId)) {
                storage.put(i, null);
                freeFrames++;
            }
        }
    }

    public ProcessPage freeFrameByProcessPageId(int processPageId, String processId) {
        for (int i = 0; i < frames; i++) {
            if (storage.get(i) == null) continue;
            ProcessPage processPage = storage.get(i);
            if (processPage != null && processPage.getId() == processPageId && processPage.getProcessOwner().getId().equals(processId)) {
                storage.put(i, null);
                freeFrames++;
                return processPage;
            }
        }
        return null;
    }
}
