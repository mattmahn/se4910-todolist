package com.mahnke.todolist;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.mahnke.todolist.contentprovider.TodoContentProvider;

import java.util.Calendar;

public class TodoDetailActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener {

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
        Calendar c = Calendar.getInstance();
        DialogFragment dialogFragment = new DatePickerFragment();
        dialogFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void setTime(View view) {
        Log.v(this.getClass().getName(), "Setting the time this todo item is due");

        // open TimePickerFragment
        Calendar c = Calendar.getInstance();
        DialogFragment dialogFragment = new TimePickerFragment();
        dialogFragment.show(getSupportFragmentManager(), "timePicker");
    }

    private void fillData(Uri uri) {
        String[] projection = {TodoDatabaseHelper.COL_SUMMARY,
                               TodoDatabaseHelper.COL_DESCRIPTION,
                               TodoDatabaseHelper.COL_STATUS,
                               TodoDatabaseHelper.COL_PRIORITY};
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

    /**
     * @param view        The view associated with this listener.
     * @param year        The year that was set.
     * @param monthOfYear The month that was set (0-11) for compatibility
     *                    with {@link Calendar}.
     * @param dayOfMonth  The day of the month that was set.
     */
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Log.v(this.getClass().getName(), "In the callback from date picker");
    }
}
