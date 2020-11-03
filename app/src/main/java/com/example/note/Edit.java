package com.example.note;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Edit extends Base {

    EditText et;

    private Toolbar myToolbar;
    private String old_content = "";
    private String old_time = "";
    private long id = 0;
    private int openMode = 0;

    public Intent intent = new Intent(); // 传递的内容



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);

        myToolbar = findViewById(R.id.my_Toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //设置toolbar取代actionbar

        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoSetMessage();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        et = findViewById(R.id.et);
        Intent getIntent = getIntent();
        openMode = getIntent.getIntExtra("mode", 0);

        if (openMode == 3) {//打开已存在的note
            id = getIntent.getLongExtra("id", 0);
            old_content = getIntent.getStringExtra("content");
            old_time = getIntent.getStringExtra("time");//获取原信息
            et.setText(old_content);
            et.setSelection(old_content.length());//设置光标位置

        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.delete:
                new AlertDialog.Builder(Edit.this)
                        .setMessage("是否删除该笔记？")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (openMode == 4){ // 新文件
                                    intent.putExtra("mode", -1);
                                    setResult(RESULT_OK, intent);
                                }
                                else { // 已存在文件
                                    intent.putExtra("mode", 2);
                                    intent.putExtra("id", id);
                                    setResult(RESULT_OK, intent);
                                }
                                finish();
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





    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_HOME){
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_BACK){//返回
            autoSetMessage();
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void autoSetMessage(){
        if(openMode == 4){
            if(et.getText().toString().length() == 0){//未作修改
                intent.putExtra("mode", -1);
            }
            else{//新笔记
                intent.putExtra("mode", 0);
                intent.putExtra("content", et.getText().toString());
                intent.putExtra("time", dateToStr());
               //传入新信息
            }
        }
        else {//打开已有笔记
            if (et.getText().toString().equals(old_content) )
                intent.putExtra("mode", -1); // 未作修改
            else {
                intent.putExtra("mode", 1); //作出修改
                intent.putExtra("content", et.getText().toString());
                intent.putExtra("time", dateToStr());
                intent.putExtra("id", id);

            }
        }
    }

    public String dateToStr(){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }


}
