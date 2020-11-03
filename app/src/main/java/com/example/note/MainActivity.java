package com.example.note;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Base implements AdapterView.OnItemClickListener {


    private NoteDatabase dbHelper;

    private Context context = this;
    final String TAG = "tag";
    FloatingActionButton btn;
    private ListView lv;
    private NoteAdapter adapter;
    private List<Note> noteList = new ArrayList<Note>();
    private Toolbar myToolbar;//状态栏



    @Override
    protected void onCreate(Bundle savedInstanceState){//主页面
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (FloatingActionButton) findViewById(R.id.fab);
        lv = findViewById(R.id.lv);

        myToolbar = findViewById(R.id.myToolbar);
        adapter = new NoteAdapter(getApplicationContext(), noteList);
        refreshListView();
        lv.setAdapter(adapter);

        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //设置toolbar
        myToolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);

        lv.setOnItemClickListener(this);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Edit.class);
                intent.putExtra("mode", 4);
                startActivityForResult(intent, 0);
            }
        });

    }

    // 接受startActivityForResult的结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        int returnMode;
        long note_Id;
        returnMode = data.getExtras().getInt("mode", -1);
        note_Id = data.getExtras().getLong("id", 0);


        if (returnMode == 1) {  //更新笔记
            String content = data.getExtras().getString("content");
            String time = data.getExtras().getString("time");
            int tag = data.getExtras().getInt("tag", 1);

            Note newNote = new Note(content, time, tag);
            newNote.setId(note_Id);//保持原有ID
            Operate op = new Operate(context);
            op.open();
            op.updateNote(newNote);//更新笔记
            op.close();
        } else if (returnMode == 0) {  //新建笔记
            String content = data.getExtras().getString("content");
            String time = data.getExtras().getString("time");
            int tag = data.getExtras().getInt("tag", 1);

            Note newNote = new Note(content, time, tag);
            Operate op = new Operate(context);
            op.open();
            op.addNote(newNote);
            op.close();
        } else if (returnMode == 2) { // 删除
            Note curNote = new Note();
            curNote.setId(note_Id);
            Operate op = new Operate(context);
            op.open();
            op.removeNote(curNote);
            op.close();
        }
        else{

        }
        refreshListView();
        super.onActivityResult(requestCode, resultCode, data);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {//搜索
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem mSearch = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) mSearch.getActionView();

        mSearchView.setQueryHint("Search");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){//全删
            case R.id.menu_clear:
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("删除所有笔记吗？")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbHelper = new NoteDatabase(context);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                db.delete("notes", null, null);
                                db.execSQL("update sqlite_sequence set seq=0 where name='notes'");
                                refreshListView();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshListView(){

        Operate op = new Operate(context);
        op.open();
        if (noteList.size() > 0) noteList.clear();
        noteList.addAll(op.getAllNotes());
        op.close();
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//点击查看每个笔记
        switch (parent.getId()) {
            case R.id.lv:
                Note curNote = (Note) parent.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, Edit.class);//跳转进入编辑页面
                intent.putExtra("content", curNote.getContent());
                intent.putExtra("id", curNote.getId());
                intent.putExtra("time", curNote.getTime());
                intent.putExtra("mode", 3);
                intent.putExtra("tag", curNote.getTag());
                startActivityForResult(intent, 1);      //取得编辑信息
                Log.d(TAG, "onItemClick: " + position);
                break;
        }
    }
}
