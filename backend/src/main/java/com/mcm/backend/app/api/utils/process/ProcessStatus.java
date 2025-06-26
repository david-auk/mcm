package com.mcm.backend.app.api.utils.process;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProcessStatus {
    private ProcessState state;
    private List<LogEntry> logs;

    public ProcessStatus() {
        this.state = ProcessState.RUNNING;
        this.logs  = new CopyOnWriteArrayList<>();
    }

    public ProcessState getState() { return state; }
    public List<LogEntry> getLogs() { return logs; }

    public void setState(ProcessState state) { this.state = state; }
    public void setLogs(List<LogEntry> logs)  { this.logs  = logs;  }
}

