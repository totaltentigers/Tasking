package me.jakemoritz.tasking;

import com.google.api.services.tasks.model.TaskList;

/**
 * Created by jakem on 11/1/2015.
 */
public interface DeleteTasksResponse {
    void deleteTasksFinish(TaskList deletedTasks);
}
