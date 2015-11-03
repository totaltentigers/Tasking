package me.jakemoritz.tasking;

import com.google.api.services.tasks.model.Task;

import java.util.List;

/**
 * Created by jakem on 11/1/2015.
 */
public interface LoadTasksResponse {
    void loadTasksFinish(List<Task> taskList);
}
