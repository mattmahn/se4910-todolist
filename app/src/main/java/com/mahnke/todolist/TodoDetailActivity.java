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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
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
    private Uri todoUri;
    private SimpleCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_edit);

        summaryText = (AutoCompleteTextView) findViewById(R.id.todo_edit_summary);
        descriptionText = (EditText) findViewById(R.id.todo_edit_description);
        btnDate = (Button) findViewById(R.id.pick_date);
        btnTime = (Button) findViewById(R.id.pick_time);
        btnConfirm = (Button) findViewById(R.id.todo_edit_confirm);
        btnDelete = (Button) findViewById(R.id.todo_edit_delete);

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
                return getCursor(constraint);
            }

            private Cursor getCursor(CharSequence constraint) {
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
        if (extras != null && !extras.isEmpty()) { // TODO check
            todoUri = extras.getParcelable(TodoContentProvider.CONTENT_ITEM_TYPE);
            fillData(todoUri);
        }
    }

    public void confirmEdit(View view) {
        Log.v(this.getClass().getName(), "Saving this todo item");
        if (TextUtils.isEmpty(summaryText.getText().toString())) {
            makeToast();
        } else {
            setResult(RESULT_OK);
            finish();
        }
    }

    public void delete(View view) {
        Log.v(this.getClass().getName(), "Deleting this todo item");
//        getContentResolver().delete(todoUri, where, selection);

        setResult(RESULT_OK);
        finish();
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
        String[] projection = {TodoDatabaseHelper.COL_SUMMARY, TodoDatabaseHelper.COL_DESCRIPTION};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();

            summaryText.setText(cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COL_SUMMARY)));
            descriptionText.setText(cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COL_DESCRIPTION)));

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

        // only save if either summary or description is available
        if (!description.isEmpty() && !summary.isEmpty()) {
            ContentValues values = new ContentValues();
            values.put(TodoDatabaseHelper.COL_SUMMARY, summary);
            values.put(TodoDatabaseHelper.COL_DESCRIPTION, description);

            if (todoUri == null) {
                todoUri = getContentResolver().insert(TodoContentProvider.CONTENT_URI, values);
            } else {
                getContentResolver().update(todoUri, values, null, null);
            }
        }
    }

    private void makeToast() {
        Toast.makeText(TodoDetailActivity.this, "Please add a summary", Toast.LENGTH_LONG).show();
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
