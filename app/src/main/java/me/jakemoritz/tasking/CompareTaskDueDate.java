package me.jakemoritz.tasking;

import com.google.api.services.tasks.model.Task;

import java.util.Comparator;

public class CompareTaskDueDate implements Comparator<Task> {

    @Override
    public int compare(Task lhs, Task rhs) {
        if (lhs.getDue() == null && rhs.getDue() != null){
            return -1;
        }
        else if (lhs.getDue() != null && rhs.getDue() == null){
            return 1;
        }
        else if (lhs.getDue() == null && rhs.getDue() == null){
            return -1;
        } else {
            return Long.valueOf(lhs.getDue().getValue()).compareTo(Long.valueOf(rhs.getDue().getValue()));
        }
    }

    @Override
    public boolean equals(Object object) {
        return false;
    }
}
