package com.example.noteapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.LauncherActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
    private static SQLiteDatabase DB;
    private static ArrayList<Note> list;
    private ArrayList<Note> high;
    private ArrayList<Note> low;
    private ArrayList<Note> mid;
    private ArrayList<String> titlelist;
    private ArrayAdapter listAdapter;
    private ListView listView;
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

    }

    private void init() {

        // initialize list
        list = new ArrayList<>();
        high = new ArrayList<>();
        mid = new ArrayList<>();
        low = new ArrayList<>();
        titlelist = new ArrayList<>();

        // initialize Offline DB
        DB = this.openOrCreateDatabase("Note",MODE_PRIVATE,null);
//        DB.execSQL("Drop table Note");
        DB.execSQL("create table if not exists Note(Title varchar , Description varchar, Priority varchar)");

//        DB.execSQL("delete from Note");

        // retrieve data
        Cursor c = DB.rawQuery("select * from Note",null);
        c.moveToFirst();

        int title = c.getColumnIndex("Title");
        int description = c.getColumnIndex("Description");
        int priority = c.getColumnIndex("Priority");
        String noteTitle = "";
        String noteDescription = "";
        String notePriority = "";

        if(c.getCount()>= 1) {
            noteTitle = c.getString(title);
            noteDescription = c.getString(description);
            notePriority = c.getString(priority);
            Note note = new Note(noteTitle,noteDescription,notePriority);
            switchNote(notePriority,note);
            while (c.moveToNext()) {
                noteTitle = c.getString(title);
                noteDescription = c.getString(description);
                notePriority = c.getString(priority);
                note = new Note(noteTitle, noteDescription, notePriority);
                switchNote(notePriority, note);
//            list.add(note);
//            titlelist.add(noteTitle);

                //  c.moveToNext();

//            c.moveToNext();

            }
        }


        list.addAll(high);
        list.addAll(mid);
        list.addAll(low);

        //get items by priority

        for (int i = 0; i<list.size();i++) titlelist.add(list.get(i).getTitle());

        // initialize list Adapter

        listAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,titlelist){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                if(list.get(position).getPriority().equals("HIGH")){
                    view.setBackgroundColor(getColor(android.R.color.holo_red_light));
                }else if(list.get(position).getPriority().equals("MID")){
                    view.setBackgroundColor(getColor(android.R.color.holo_blue_light));
                }else if(list.get(position).getPriority().equals("LOW")){
                    view.setBackgroundColor(getColor(android.R.color.holo_green_light));
                }
                return view;
            }
        };
        listView = findViewById(R.id.listview);
        listView.setAdapter(listAdapter);

        listView.setOnItemLongClickListener(this);
        listView.setOnItemClickListener(this);
        Log.d(TAG, "init: Success");

    }

    private void switchNote(String notePriority, Note note) {
        switch (notePriority) {
            case "HIGH":
                high.add(note);
                break;
            case "MID":
                mid.add(note);
                break;
            case "LOW":
                low.add(note);
                break;
                default:
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.note_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        switch (item.getItemId()) {
            case R.id.addNote:
                //goto Add Note activity
                startActivity(new Intent(this, AddNoteActivity.class));
                break;
            case R.id.refreshButtonMenu:
                init();
                Toast.makeText(this, "List Refreshed", Toast.LENGTH_SHORT).show();
                break;
            case R.id.clearAll:
                DB.execSQL("delete from Note");
                init();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public static SQLiteDatabase getDB() {
        return DB;
    }

    public static ArrayList<Note> getList() {
        return list;
    }

    public ArrayList getHigh() {
        return high;
    }

    public ArrayList getLow() {
        return low;
    }

    public ArrayList getMid() {
        return mid;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Alert")
                .setMessage("You are trying to delete this note, is that right?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       DB.execSQL("delete from Note where Title=? and Description=? and priority=?",new String[]{titlelist.get(position),list.get(position).getDescription(),list.get(position).getPriority()});
                       list.remove(position);
                       titlelist.remove(position);
                        Toast.makeText(MainActivity.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                        init();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this,Show_Update_Note.class);
        intent.putExtra("position",position);
        startActivity(intent);
    }
}
