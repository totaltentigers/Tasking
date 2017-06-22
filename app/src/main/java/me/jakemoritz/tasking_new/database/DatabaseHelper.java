package me.jakemoritz.tasking_new.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String dbName = "taskDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TASK_TABLE_NAME = "tasks";
    private static final String TASK_COLUMN_ID = "_id";
    private static final String TASK_COLUMN_TITLE = "title";
    private static final String TASK_COLUMN_NOTES = "notes";
    private static final String TASK_COLUMN_STATUS = "status";
    private static final String TASK_COLUMN_DUE_DATE = "duedate";
    private static final String TASK_COLUMN_COMP_DATE = "completeddate";

    public DatabaseHelper(Context context){
        super(context, dbName, null , DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TASK_TABLE_NAME + " ( " +
                TASK_COLUMN_ID + " TEXT PRIMARY KEY NOT NULL, " +
                TASK_COLUMN_TITLE + " TEXT, " +
                TASK_COLUMN_NOTES + " TEXT, " +
                TASK_COLUMN_STATUS + " TEXT, " +
                TASK_COLUMN_DUE_DATE + " TEXT," +
                TASK_COLUMN_COMP_DATE + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TASK_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertTask(Task task){
        String taskId = task.getId();
        String taskTitle = task.getTitle();
        String taskNotes = task.getNotes();
        String taskStatus = task.getStatus();
        String taskDueDate = null;
        String taskCompletedDate = null;
        if (task.getDue() != null){
            taskDueDate = String.valueOf(task.getDue().getValue());
        }
        if (task.getCompleted() != null){
            taskCompletedDate = String.valueOf(task.getCompleted().getValue());
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TASK_COLUMN_ID, taskId);
        contentValues.put(TASK_COLUMN_TITLE, taskTitle);
        contentValues.put(TASK_COLUMN_NOTES, taskNotes);
        contentValues.put(TASK_COLUMN_STATUS, taskStatus);
        if (taskDueDate != null){
            contentValues.put(TASK_COLUMN_DUE_DATE, taskDueDate);
        }
        if (taskCompletedDate != null){
            contentValues.put(TASK_COLUMN_COMP_DATE, taskCompletedDate);
        }
        db.insert(TASK_TABLE_NAME , null, contentValues);
        return true;
    }

    public boolean updateTaskInDb(String id, Task task){
        String taskId = task.getId();
        String taskTitle = task.getTitle();
        String taskNotes = task.getNotes();
        String taskStatus = task.getStatus();
        String taskDueDate = null;
        String taskCompletedDate = null;
        if (task.getDue() != null){
            taskDueDate = String.valueOf(task.getDue().getValue());
        }
        if (task.getCompleted() != null){
            taskCompletedDate = String.valueOf(task.getCompleted().getValue());
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TASK_COLUMN_ID, taskId);
        contentValues.put(TASK_COLUMN_TITLE, taskTitle);
        contentValues.put(TASK_COLUMN_NOTES, taskNotes);
        contentValues.put(TASK_COLUMN_STATUS, taskStatus);
        if (taskDueDate != null){
            contentValues.put(TASK_COLUMN_DUE_DATE, taskDueDate);
        }
        if (taskCompletedDate != null){
            contentValues.put(TASK_COLUMN_COMP_DATE, taskCompletedDate);
        }

        db.update(TASK_TABLE_NAME, contentValues, TASK_COLUMN_ID + " = ? ", new String[]{id});
        return true;
    }

    private Cursor getTask(String id){
        SQLiteDatabase db = getReadableDatabase();
         return db.rawQuery("SELECT * FROM " + TASK_TABLE_NAME + " WHERE " + TASK_COLUMN_ID + " = ?", new String[]{id});
    }

    private Cursor getAllTasks(){
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TASK_TABLE_NAME, null);
    }

    public Integer deleteTask(String id){
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TASK_TABLE_NAME,
                TASK_COLUMN_ID + " = ? ",
                new String[]{id});
    }

    public void saveTasksToDb(List<Task> tasks) {
        SQLiteDatabase db = getWritableDatabase();
        onUpgrade(db, 1, 1);

        if (tasks != null) {
            for (Task task : tasks) {
                if (getTask(task.getId()).getCount() != 0) {
                    updateTaskInDb(task.getId(), task);
                } else {
                    insertTask(task);
                }
            }
        }
    }

    public List<Task> getTasksFromDb() {
        Cursor res = getAllTasks();
        res.moveToFirst();

        List<Task> taskList = new ArrayList<>();

        for (int i = res.getCount() - 1; i >= 0; i--) {
            String taskId = res.getString(res.getColumnIndex(DatabaseHelper.TASK_COLUMN_ID));
            String taskTitle = res.getString(res.getColumnIndex(DatabaseHelper.TASK_COLUMN_TITLE));
            String taskNotes = res.getString(res.getColumnIndex(DatabaseHelper.TASK_COLUMN_NOTES));
            String taskStatus = res.getString(res.getColumnIndex(DatabaseHelper.TASK_COLUMN_STATUS));
            String taskDueDate = res.getString(res.getColumnIndex(DatabaseHelper.TASK_COLUMN_DUE_DATE));
            String taskCompletedDate = res.getString(res.getColumnIndex(DatabaseHelper.TASK_COLUMN_COMP_DATE));

            Task task = new Task();
            task.setId(taskId);
            if (taskTitle != null) {
                task.setTitle(taskTitle);
            }
            if (taskNotes != null) {
                task.setNotes(taskNotes);
            }
            if (taskStatus != null) {
                task.setStatus(taskStatus);
            }
            if (taskDueDate != null) {
                task.setDue(new DateTime(Long.valueOf(taskDueDate)));
            }
            if (taskCompletedDate != null) {
                task.setCompleted(new DateTime(Long.valueOf(taskCompletedDate)));
            }
            taskList.add(task);
            res.moveToNext();
        }

        res.close();
        return taskList;
    }
}
