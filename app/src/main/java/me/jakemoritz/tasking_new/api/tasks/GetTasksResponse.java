package me.jakemoritz.tasking_new.api.tasks;

import com.google.api.services.tasks.model.Task;

import java.util.List;

public interface GetTasksResponse {
    void tasksReceived(List<Task> taskList);
}
