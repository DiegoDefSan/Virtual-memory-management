package process;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Process {
    private String id;
    private int size;
    private ProcessState state;
    private List<ProcessPage> pages;

    public Process(String id, int size) {
        this.id = id;
        this.size = size;
        this.state = ProcessState.NEW;
        this.pages = new ArrayList<>();
    }
}
