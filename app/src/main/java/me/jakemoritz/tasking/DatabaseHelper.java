package me.jakemoritz.tasking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.api.services.tasks.model.Task;

public class DatabaseHelper extends SQLiteOpenHelper {

    static final String dbName="taskDB";
    static final String taskTable = "Tasks";
    static final String taskID ="_id";
    static final String colTitle="Title";
    static final String colNotes="Notes";
    static final String colStatus="Status";
    static final String colDueDate="DueDate";
    static final String colCompletedDate="CompletedDate";


    public DatabaseHelper(Context context){
        super(context, dbName, null , 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE tasks ( _id TEXT PRIMARY KEY "
                + ", title TEXT, notes TEXT, status TEXT, duedate TEXT,"
                + "completeddate TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tasks");
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
        contentValues.put("_id", taskId);
        contentValues.put("title", taskTitle);
        contentValues.put("notes", taskNotes);
        contentValues.put("status", taskStatus);
        if (taskDueDate != null){
            contentValues.put("duedate", taskDueDate);
        }
        if (taskCompletedDate != null){
            contentValues.put("completeddate", taskCompletedDate);
        }
        db.insert("tasks", null, contentValues);
        return true;
    }

    public boolean updateTask(String id, Task task){
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
        contentValues.put("_id", taskId);
        contentValues.put("title", taskTitle);
        contentValues.put("notes", taskNotes);
        contentValues.put("status", taskStatus);
        if (taskDueDate != null){
            contentValues.put("duedate", taskDueDate);
        }
        if (taskCompletedDate != null){
            contentValues.put("completeddate", taskCompletedDate);
        }

        db.update("tasks", contentValues, "_id = ? ", new String[]{
                id
        });
        return true;
    }

    public Cursor getTask(String id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM tasks WHERE _id = ?", new String[]{
                id
        });
        return res;
    }

    public Cursor getAllTasks(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM tasks", null);
        return res;
    }

    public Integer deleteTask(String id){
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("tasks",
                "_id = ? ",
                new String[]{
                        id
                });
    }
}
