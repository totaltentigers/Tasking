package me.jakemoritz.tasking;

import com.google.api.services.tasks.model.Task;

import java.util.List;

public interface LoadTasksResponse {
    void loadTasksFinish(List<Task> taskList);
}
