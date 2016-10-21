package com.mahnke.todolist.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.mahnke.todolist.TodoDatabaseHelper;

import java.util.Arrays;
import java.util.HashSet;

import static android.app.DownloadManager.COLUMN_ID;
import static com.mahnke.todolist.TodoDatabaseHelper.TABLE_TODO;

public class TodoContentProvider extends ContentProvider {
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/todos";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/todo";
    private static final int TODOS = 167;
    private static final int TODO_ID = 963;
    private static final String AUTHORITY = "com.mahnke.todolist.contentprovider";
    private static final String BASE_PATH = "todos";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, TODOS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", TODO_ID);
    }

    private TodoDatabaseHelper dbHelper;

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        Log.v(this.getClass().getName(), "Deleting an item");
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        int rowsDeleted;
        switch (uriType) {
            case TODOS:
                rowsDeleted = sqlDB.delete(TABLE_TODO, selection,
                                           selectionArgs);
                break;
            case TODO_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(TABLE_TODO, COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(TABLE_TODO,
                                               COLUMN_ID + "=" + id + " AND " + selection,
                                               selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        Log.w(this.getClass().getName(),
              "Getting type of data, but not implemented",
              new UnsupportedOperationException());
        // TODO: Implement this to handle requests for the MIME type of the data at the given URI.
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Log.v(this.getClass().getName(),
              "Inserting an item: uri=" + uri.toString() + "; values={" + values.toString() + "}");
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        long id;
        switch (uriType) {
            case TODOS:
                id = sqlDB.insert(TABLE_TODO, null, values);
                Log.v(this.getClass().getName(), "added new item to DB");
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public boolean onCreate() {
        this.dbHelper = new TodoDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.v(this.getClass().getName(), "Querying for an item");
        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(TABLE_TODO);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case TODOS:
                break;
            case TODO_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(COLUMN_ID + "="
                                         + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                                           selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;

    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.v(this.getClass().getName(), "Updating an item");
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = dbHelper.getWritableDatabase();
        int rowsUpdated;
        switch (uriType) {
            case TODOS:
                rowsUpdated = sqlDB.update(TABLE_TODO, values, selection, selectionArgs);
                break;
            case TODO_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(TABLE_TODO, values, COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = sqlDB.update(TABLE_TODO,
                                               values,
                                               COLUMN_ID + "=" + id + " AND " + selection,
                                               selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        final String[] available = {TodoDatabaseHelper.COL_ID,
                                    TodoDatabaseHelper.COL_SUMMARY,
                                    TodoDatabaseHelper.COL_DESCRIPTION,
                                    TodoDatabaseHelper.COL_DATETIME,
                                    TodoDatabaseHelper.COL_STATUS};
        if (projection != null) {
            final HashSet<String> requestedColumns = new HashSet<>(Arrays.asList(projection));
            final HashSet<String> availableColumns = new HashSet<>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
