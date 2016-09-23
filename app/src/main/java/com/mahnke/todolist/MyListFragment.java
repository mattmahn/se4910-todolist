package com.mahnke.todolist;


import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyListFragment extends ListFragment
        implements AdapterView.OnItemClickListener {

    private final List<String> todoItems;

    public MyListFragment() {
        // Required empty public constructor
        todoItems = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p/>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), this.todoItems.get(position), Toast.LENGTH_SHORT).show();
    }

    /**
     * Attach to list view once the view hierarchy has been created.
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // get refs to UI elements
        final EditText myEditText = (EditText) view.findViewById(R.id.myEditText);

        // connect adapter for displaying todo list items in ListView
        final ArrayAdapter<String> aa = new ArrayAdapter<>(getActivity(),
                                                           android.R.layout.simple_list_item_1,
                                                           todoItems);
        setListAdapter(aa);
        getListView().setOnItemClickListener(this);

        // let user add items to todo list
        myEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_ENTER) {
                    // add items to the end because older items are probably more important
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
