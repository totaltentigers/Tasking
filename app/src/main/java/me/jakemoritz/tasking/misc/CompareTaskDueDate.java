package me.jakemoritz.tasking.misc;

import com.google.api.services.tasks.model.Task;

import java.util.Comparator;

import static java.lang.Long.valueOf;

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
            return Integer.valueOf(lhs.getPosition()).compareTo(Integer.valueOf(rhs.getPosition()));
        } else {
            return valueOf(lhs.getDue().getValue()).compareTo(rhs.getDue().getValue());
        }
    }

    @Override
    public boolean equals(Object object) {
        return false;
    }
}
