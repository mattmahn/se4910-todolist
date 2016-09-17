package com.mahnke.todolist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class TodoListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);

        // get refs to UI elements
        final EditText myEditText = (EditText) findViewById(R.id.myEditText);
        final ListView myListView = (ListView) findViewById(R.id.myListView);

        // connect adapter for displaying todo list items in ListView
        final List<String> todoItems = new ArrayList<>();
        final ArrayAdapter<String> aa = new ArrayAdapter<>(this,
                                                           android.R.layout.simple_list_item_1,
                                                           todoItems);
        myListView.setAdapter(aa);

        // let user add items to todo list
        myEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_ENTER) {
                    todoItems.add(myEditText.getText().toString());
                    aa.notifyDataSetChanged();
                    myEditText.setText("");
                    return true;
                }
                return false;
            }
        });
    }
}
