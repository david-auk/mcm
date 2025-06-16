package com.mcm.backend.database.core.components;

import java.util.List;

public interface TimestampedDAOInterface<T, K> extends DAOInterface<T, K> {
    List<T> getOrdered(Integer maxRecords, boolean ascending);
    List<T> getLatest(Integer maxRecords);
    List<T> getOldest(Integer maxRecords);
    T getLatest();
    T getOldest();
}
