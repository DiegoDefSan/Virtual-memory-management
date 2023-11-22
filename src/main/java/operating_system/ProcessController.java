package operating_system;

import lombok.Data;
import process.Process;
import process.ProcessState;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

@Data
public class ProcessController {
    private final Queue<String> readyQueue;
    private final Queue<String> blockedQueue;
    private final Queue<String> blockedSuspendedQueue;
    private final Queue<String> readySuspendedQueue;
    private String runningProcess;
    private final int maxNumberOfProcesses;
    private final List<Process> processList;

    public ProcessController() {
        readyQueue = new LinkedList<>();
        blockedQueue = new LinkedList<>();
        blockedSuspendedQueue = new LinkedList<>();
        readySuspendedQueue = new LinkedList<>();
        runningProcess = "";
        maxNumberOfProcesses = 4;
        processList = new LinkedList<>();
    }

    private void setProcessState(String processId, ProcessState state) {
        processList.stream().filter(process -> process.getId().equals(processId)).findFirst().get().setState(state);
    }

    private void addProcessToReadySuspendedQueue(String processId) {
        readySuspendedQueue.add(processId);
        processList.stream().filter(process -> process.getId().equals(processId)).findFirst().get().setState(ProcessState.READY_SUSPENDED);
        setProcessState(processId, ProcessState.READY_SUSPENDED);
    }

    private void addProcessToBlockedSuspendedQueue(String processId) {
        blockedSuspendedQueue.add(processId);
        processList.stream().filter(process -> process.getId().equals(processId)).findFirst().get().setState(ProcessState.BLOCKED_SUSPENDED);
        setProcessState(processId, ProcessState.BLOCKED_SUSPENDED);
    }

    public void addProcessToReadyQueue(String processId) {
        if (readyQueue.size() < maxNumberOfProcesses) {
            readyQueue.add(processId);
            processList.stream().filter(process -> process.getId().equals(processId)).findFirst().get().setState(ProcessState.READY);
            setProcessState(processId, ProcessState.READY);
        } else {
            addProcessToReadySuspendedQueue(processId);
        }
    }

    public void addProcessToBlockedQueueFromExecuting(List<String> idProcessesInMP) {
        String processId = runningProcess;
        runningProcess = "";

        boolean isProcessInMP = idProcessesInMP.contains(processId);

        if (isProcessInMP) {
            blockedQueue.add(processId);
            setProcessState(processId, ProcessState.BLOCKED);
        } else {
            addProcessToBlockedSuspendedQueue(processId);
            setProcessState(processId, ProcessState.BLOCKED_SUSPENDED);
        }
    }

    private void addProcessFromSuspendedToBlockedQueue() {
        String processId = blockedSuspendedQueue.poll();
        blockedQueue.add(processId);
        setProcessState(processId, ProcessState.BLOCKED);
    }

    public void addProcess(Process process) {
        processList.add(process);
    }

    public void suspendProcess(String processId) {
        if (readyQueue.contains(processId)) {
            readyQueue.remove(processId);
            addProcessToReadySuspendedQueue(processId);
        } else if (blockedQueue.contains(processId)) {
            blockedQueue.remove(processId);
            addProcessToBlockedSuspendedQueue(processId);
        }
    }

    public String getTheSettingRunningProcess() {
        // Si el proceso está en estado running, se debe obtener la siguiente instrucción
        if (!runningProcess.isEmpty()) return runningProcess;

        // Si no hay ningún proceso en estado running, se debe obtener el siguiente proceso de la ready queue
        if (!readyQueue.isEmpty()) {
            runningProcess = readyQueue.poll();

            setProcessState(runningProcess, ProcessState.RUNNING);
            return runningProcess;
        }

        if (!readySuspendedQueue.isEmpty()) {
            runningProcess = readySuspendedQueue.poll();

            setProcessState(runningProcess, ProcessState.RUNNING);
            return runningProcess;
        }

        return "";
    }

    public String getTheBlockedProcess() {
        // Si no hay ningún proceso en estado running, se debe obtener el siguiente proceso de la ready queue
        if (!blockedQueue.isEmpty()) {
            String processId = blockedQueue.poll();

            setProcessState(processId, ProcessState.RUNNING);

            return processId;
        }

        if (!blockedSuspendedQueue.isEmpty()) {
            addProcessFromSuspendedToBlockedQueue();
            return getTheBlockedProcess();
        }

        return "";
    }
    public void removeProcess(String processId) {
        processList.removeIf(process -> process.getId().equals(processId));
        runningProcess = "";
    }

    public void showProcesses() {
        System.out.println("\tProcess list: ");
        processList.forEach(process -> System.out.println("\t- " + process.getId() + " " + process.getState()));
    }
    public void showQueues() {
        System.out.println("\t- Ready queue: " + readyQueue);
        System.out.println("\t- Blocked queue: " + blockedQueue);
        System.out.println("\t- Ready suspended queue: " + readySuspendedQueue);
        System.out.println("\t- Blocked suspended queue: " + blockedSuspendedQueue);
        System.out.println("\t- Running process: " + runningProcess);
    }
}
