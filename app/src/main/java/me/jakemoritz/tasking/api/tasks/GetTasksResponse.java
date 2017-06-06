package me.jakemoritz.tasking.api.tasks;

import com.google.api.services.tasks.model.Task;

import java.util.List;

public interface GetTasksResponse {
    void getTasksFinish(List<Task> taskList);
}
