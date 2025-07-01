package com.mcm.backend.app.api.utils.process;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProcessRegistry {
    private final Map<UUID, ProcessStatus> jobs = new ConcurrentHashMap<>();

    public UUID create() {
        UUID id = UUID.randomUUID();
        jobs.put(id, new ProcessStatus());
        return id;
    }

    public ProcessStatus get(UUID id) {
        return jobs.get(id);
    }
}