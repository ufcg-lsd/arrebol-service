package org.fogbowcloud.arrebol.core.processors;

import org.fogbowcloud.arrebol.core.models.Command;
import org.fogbowcloud.arrebol.core.models.Resource;
import org.fogbowcloud.arrebol.core.models.Specification;
import org.fogbowcloud.arrebol.core.models.TaskState;

import java.util.List;

public interface TaskProcessor {
    String getProcessId();

    String getTaskId();

    List<Command> getCommands();

    void executeTask(Resource resource);

    TaskState getStatus();

    Specification getSpecification();

    Resource getResource();

    void setStatus(TaskState taskState);
}
