package com.mahnke.todolist;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mahnke.todolist.contentprovider.TodoContentProvider;

import java.io.UTFDataFormatException;
import java.util.Calendar;

public class TodoDetailActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private AutoCompleteTextView summaryText;
    private EditText descriptionText;
    private Button btnDate;
    private Button btnTime;
    private Button btnConfirm;
    private Button btnDelete;
    private CheckBox chkStatus;
    private Spinner spnrPriority;
    private Uri todoUri;
    private SimpleCursorAdapter cursorAdapter;

    private Calendar dueDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_edit);

        // set view components
        summaryText = (AutoCompleteTextView) findViewById(R.id.todo_edit_summary);
        descriptionText = (EditText) findViewById(R.id.todo_edit_description);
        btnDate = (Button) findViewById(R.id.pick_date);
        btnTime = (Button) findViewById(R.id.pick_time);
        btnConfirm = (Button) findViewById(R.id.todo_edit_confirm);
        btnDelete = (Button) findViewById(R.id.todo_edit_delete);
        chkStatus = (CheckBox) findViewById(R.id.todo_edit_status);
        spnrPriority = (Spinner) findViewById(R.id.todo_edit_priority);

        // setup autocomplete for summary
        cursorAdapter = new SimpleCursorAdapter(this,
                                                android.R.layout.simple_list_item_1,
                                                null,
                                                new String[]{TodoDatabaseHelper.COL_SUMMARY},
                                                new int[]{android.R.id.text1},
                                                0);
        cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String select = TodoDatabaseHelper.COL_SUMMARY + " LIKE ? ";
                String[] selectArgs = {"%" + constraint + "%"};
                String[] summaryProjection =
                        {TodoDatabaseHelper.COL_ID, TodoDatabaseHelper.COL_SUMMARY};
                return getContentResolver().query(TodoContentProvider.CONTENT_URI,
                                                  summaryProjection,
                                                  select,
                                                  selectArgs,
                                                  null);
            }
        });
        cursorAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor cursor) {
                int index = cursor.getColumnIndex(TodoDatabaseHelper.COL_SUMMARY);
                return cursor.getString(index);
            }
        });
        summaryText.setAdapter(cursorAdapter);
        // end setup autocomplete for summary

        // setup date & time pickers
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerFragment().show(getSupportFragmentManager(), "datePicker");
            }
        });
        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerFragment().show(getSupportFragmentManager(), "timePicker");
            }
        });

        Bundle extras = getIntent().getExtras();
        // check from saved instance
        todoUri = (savedInstanceState == null) ?
                  null :
                  (Uri) savedInstanceState.getParcelable(TodoContentProvider.CONTENT_ITEM_TYPE);
        // or passed from other activity
        if (extras != null && !extras.isEmpty()) {
            Log.v(this.getClass().getName(), "Loading the item from the database");
            todoUri = extras.getParcelable(TodoContentProvider.CONTENT_ITEM_TYPE);
            fillData(todoUri);
        }
    }

    public void confirmEdit(View view) {
        Log.v(this.getClass().getName(), "Saving this todo item");
        if (TextUtils.isEmpty(summaryText.getText().toString()) ||
            TextUtils.isEmpty(descriptionText.getText().toString())) {
            makeToast();
        } else {
            setResult(RESULT_OK);
            finish();
            scheduleTaskNotification();
            Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
        }
    }

    public void delete(View view) {
        Log.v(this.getClass().getName(), "Deleting this todo item");
        String summaryText = this.summaryText.getText().toString();
        String descriptionText = this.descriptionText.getText().toString();

        // TODO delete item where _id matches a (hidden) View in the layout, so the user can
        // delete an item that they have edited
        String where = TodoDatabaseHelper.COL_SUMMARY + " = ? AND " +
                       TodoDatabaseHelper.COL_DESCRIPTION + " = ?";
        getContentResolver().delete(todoUri, where, new String[]{summaryText, descriptionText});

        setResult(RESULT_OK);
        finish();
        Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
    }

    public void setDate(View view) {
        Log.v(this.getClass().getName(), "Setting the date this todo item is due");

        // open DatePickerFragment
        DialogFragment dialogFragment = new DatePickerFragment();
        dialogFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void setTime(View view) {
        Log.v(this.getClass().getName(), "Setting the time this todo item is due");

        // open TimePickerFragment
        DialogFragment dialogFragment = new TimePickerFragment();
        dialogFragment.show(getSupportFragmentManager(), "timePicker");
    }

    private void fillData(Uri uri) {
        String[] projection = TodoDatabaseHelper.ALL_COLS;
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();

            summaryText.setText(cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COL_SUMMARY)));
            descriptionText.setText(cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COL_DESCRIPTION)));
            chkStatus.setChecked(
                    cursor.getInt(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COL_STATUS)) ==
                    1);
            int priorityIdx =
                    cursor.getInt(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COL_PRIORITY));
            spnrPriority.setSelection(priorityIdx);
            dueDate =
                    Utils.getCalendarFromMillis(cursor.getLong(cursor.getColumnIndexOrThrow(
                            TodoDatabaseHelper.COL_DATETIME)));
            btnDate.setText(Utils.getPrettyDate(dueDate));
            btnTime.setText(Utils.getPrettyTime(dueDate));

            // always close the cursor
            cursor.close();
        }
    }

    /**
     * @param outState Bundle in which to place your saved state.
     * @see #onCreate
     * @see #onRestoreInstanceState
     * @see #onPause
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putParcelable(TodoContentProvider.CONTENT_ITEM_TYPE, todoUri);
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into
     * the background, but has not (yet) been killed.  The counterpart to
     * {@link #onResume}.
     *
     * @see #onResume
     * @see #onSaveInstanceState
     * @see #onStop
     */
    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    private void saveState() {
        String summary = summaryText.getText().toString();
        String description = descriptionText.getText().toString();
        boolean isComplete = chkStatus.isChecked();

        // only save if either summary or description is available
        if (!description.isEmpty() && !summary.isEmpty()) {
            ContentValues values = new ContentValues();
            values.put(TodoDatabaseHelper.COL_SUMMARY, summary);
            values.put(TodoDatabaseHelper.COL_DESCRIPTION, description);
            values.put(TodoDatabaseHelper.COL_STATUS, isComplete ? 1 : 0);
            values.put(TodoDatabaseHelper.COL_PRIORITY, spnrPriority.getSelectedItemPosition());
            values.put(TodoDatabaseHelper.COL_DATETIME, dueDate.getTimeInMillis());
            
            if (todoUri == null) {
                todoUri = getContentResolver().insert(TodoContentProvider.CONTENT_URI, values);
            } else {
                getContentResolver().update(todoUri, values, null, null);
            }
        }
    }

    private void makeToast() {
        Toast.makeText(TodoDetailActivity.this,
                       "Please add a summary and description",
                       Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Log.v(this.getClass().getName(), "In the callback from date picker");
        this.dueDate.set(Calendar.YEAR, year);
        this.dueDate.set(Calendar.MONTH, monthOfYear);
        this.dueDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        btnDate.setText(Utils.getPrettyDate(this.dueDate));

        // start time picker, for convenience
        new TimePickerFragment().show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Log.v(this.getClass().getName(), "In the callback from time picker");
        this.dueDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
        this.dueDate.set(Calendar.MINUTE, minute);
        this.dueDate.set(Calendar.SECOND, 0);
        btnTime.setText(Utils.getPrettyTime(this.dueDate));
    }

    private void scheduleTaskNotification() {
        Log.v(this.getClass().getName(), "Scheduling a notification");
        Intent i = new Intent(this, AlarmReceiver.class);
        i.putExtra(TodoDatabaseHelper.COL_SUMMARY, summaryText.getText().toString());
        i.putExtra(TodoDatabaseHelper.COL_DESCRIPTION, descriptionText.getText().toString());
        i.setAction("com.mahnke.todolist.AlarmReceiver");

        PendingIntent pIntent =
                PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, dueDate.getTimeInMillis(), pIntent);
    }
}
