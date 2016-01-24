package me.jakemoritz.tasking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.api.services.tasks.model.Task;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String dbName = "taskDB";
    private static final int DATABASE_VERSION = 1;
    public static final String TASK_TABLE_NAME = "tasks";
    public static final String TASK_COLUMN_ID = "_id";
    public static final String TASK_COLUMN_TITLE = "title";
    public static final String TASK_COLUMN_NOTES = "notes";
    public static final String TASK_COLUMN_STATUS = "status";
    public static final String TASK_COLUMN_DUE_DATE = "duedate";
    public static final String TASK_COLUMN_COMP_DATE = "completeddate";


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

    public Cursor getTask(String id){
        SQLiteDatabase db = getReadableDatabase();
         return db.rawQuery("SELECT * FROM " + TASK_TABLE_NAME + " WHERE " + TASK_COLUMN_ID + " = ?", new String[]{id});
    }

    public Cursor getAllTasks(){
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TASK_TABLE_NAME, null);
    }

    public Integer deleteTask(String id){
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TASK_TABLE_NAME,
                TASK_COLUMN_ID + " = ? ",
                new String[]{id});
    }

}
