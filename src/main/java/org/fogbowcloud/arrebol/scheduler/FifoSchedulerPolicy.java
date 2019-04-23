package org.fogbowcloud.arrebol.scheduler;

import org.apache.log4j.Logger;
import org.fogbowcloud.arrebol.core.models.Job;
import org.fogbowcloud.arrebol.core.models.task.Task;
import org.fogbowcloud.arrebol.core.models.task.TaskState;
import org.fogbowcloud.arrebol.resource.Resource;
import org.fogbowcloud.arrebol.resource.ResourceState;
import org.fogbowcloud.arrebol.queue.JobQueue;
import org.fogbowcloud.arrebol.resource.ResourcePool;

import java.util.Collection;
import java.util.LinkedList;

public class FifoSchedulerPolicy implements SchedulerPolicy {

    private final Logger logger = Logger.getLogger(SchedulerPolicy.class);

    @Override
    public Collection<AllocationPlan> schedule(JobQueue queue, ResourcePool pool) {

        //logger.info("queueId={} resourcePool={}", queue.getId(), pool.getId());

        Collection<Resource> availableResources = filterAvailable(pool);

        Collection<AllocationPlan> queueAllocation = new LinkedList<AllocationPlan>();
        for(Job job: queue.queue()) {
            Collection<AllocationPlan> allocationPlans = scheduleJob(job, availableResources);
            for (AllocationPlan plan : allocationPlans) {
                queueAllocation.add(plan);
                availableResources.remove(plan.getResource());
            }
        }

        return queueAllocation;
    }

    private Collection<AllocationPlan> scheduleJob(Job job, Collection<Resource> availableResources) {
        //obviously, not optimised

        Collection<AllocationPlan> jobAllocation = new LinkedList<AllocationPlan>();

        //we use a working copy to not modify the receive list
        Collection<Resource> copyOfAvailableResources = new LinkedList<Resource>(availableResources);
        for(Task pendingTask: filterPending(job)) {
            AllocationPlan taskAllocation = scheduleTask(pendingTask, copyOfAvailableResources);
            if (taskAllocation != null) {
                copyOfAvailableResources.remove(taskAllocation.getResource());
                jobAllocation.add(taskAllocation);
            }
        }
        return jobAllocation;
    }

    private AllocationPlan scheduleTask(Task task, Collection<Resource> availableResources) {

        for (Resource resource: availableResources) {
            if (resource.match(task.getSpecification())) {
                //logger.info("allocation made for task={} using resource={}", task, resource);
                return new AllocationPlan(task, resource, AllocationPlan.Type.RUN);
            }
        }

        return null;
    }

    private Collection<Task> filterPending(Job job) {
        Collection<Task> pending = new LinkedList<Task>();
        for(Task task: job.tasks()) {
            if (TaskState.PENDING.equals(task.getState())) {
                pending.add(task);
            }
        }
        return pending;
    }

    private Collection<Resource> filterAvailable(ResourcePool toFilter) {
        Collection<Resource> availableResources = new LinkedList<Resource>();
        for(Resource resource : toFilter.getResources()) {
            if (resource.getState().equals(ResourceState.IDLE)) {
                availableResources.add(resource);
            }
        }
        return availableResources;
    }
}