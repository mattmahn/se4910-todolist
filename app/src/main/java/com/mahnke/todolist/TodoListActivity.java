package com.mahnke.todolist;

import android.app.ListFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class TodoListActivity extends AppCompatActivity {
    static final int SPEECH_REQUEST_CODE = 677;
    static final int NOTIF_TASK_DUE = 797;
    final List<TodoItem> todoItems;
    ArrayAdapter<TodoItem> aa;

    public TodoListActivity() {
        todoItems = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);
        // connect adapter for displaying todo list items in ListView
//        ListFragment myListFragment = (ListFragment) findViewById(R.id.myListFragment);
        aa = new ArrayAdapter<>(getApplicationContext(),
                                android.R.layout.simple_list_item_1,
                                todoItems);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Log.v(MyListFragment.class.getName(), results.toString());
            String spokenText = results.get(0);

            TodoItem todoItem = new TodoItem(spokenText, true);
            addTodoItem(todoItem);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showTimePickerDialog(View view) {
        DialogFragment dialogFragment = new TimePickerFragment();
        dialogFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog(View view) {
        DialogFragment dialogFragment = new DatePickerFragment();
        dialogFragment.show(getSupportFragmentManager(), "datePicker");
    }

    /**
     * Create an intent that starts the Speech Recognizer activity
     */
    public void displaySpeechRecognizer(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // start the activity, the intent will be populated with text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    public void addTodoItem(TodoItem todoItem) {
        todoItems.add(todoItem);
        aa.notifyDataSetChanged();
    }

    public void makeNotification(TodoItem todoItem) {
        Intent intent = new Intent(this, MyListFragment.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        // build notification
        Notification n = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(todoItem.getTitle())
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIF_TASK_DUE, n);
    }
}
