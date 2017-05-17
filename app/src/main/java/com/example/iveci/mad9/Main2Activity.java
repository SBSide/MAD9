package com.example.iveci.mad9;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.icu.text.NumberFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class Main2Activity extends AppCompatActivity {
    LinearLayout l1,l2;
    ListView listView;
    String editfile;
    ArrayList<String> list = new ArrayList<>();
    ArrayAdapter adapter;
    DatePicker datePicker;
    TextView tvc;
    EditText editText;
    Button bsave, bcancel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Memo");
        setContentView(R.layout.activity_main2);
        int permissioninfo = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissioninfo == PackageManager.PERMISSION_GRANTED) {
            File file = new File(getExternalPath() + "diary");
            if (!file.exists()) {
                file.mkdir();
                String msg = "디렉터리 생성";
                if (file.isDirectory() == false) msg = "디렉터리 생성 오류";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        }
        else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Toast.makeText(getApplicationContext(),
                        "SDCard 쓰기 권한이 필요합니다. \n" + "설정에서 수동으로 활성화해주세요.",Toast.LENGTH_SHORT).show();
            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
        init();
    }

    public void init(){
        l1 = (LinearLayout) findViewById(R.id.linear1);
        l2 = (LinearLayout) findViewById(R.id.linear2);
        tvc = (TextView) findViewById(R.id.tvCount);
        datePicker = (DatePicker) findViewById(R.id.date);
        editText = (EditText) findViewById(R.id.etdesc);
        bsave = (Button) findViewById(R.id.btnsave);
        bsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bsave.getText().toString().equals("수정")){ //수정버튼
                    String path = getExternalPath();
                    NumberFormat numberFormat = NumberFormat.getNumberInstance();
                    numberFormat.setMinimumIntegerDigits(2);
                    int year = datePicker.getYear() >= 100 ?
                            datePicker.getYear() % 100 : datePicker.getYear();
                    int month = datePicker.getMonth()+1;
                    int day = datePicker.getDayOfMonth();
                    File file = new File(path + "diary/" +
                            year + "-" +  numberFormat.format(month) + "-" + numberFormat.format(day) + ".memo");
                    if (editfile != file.getName()){ //날짜가 바뀐경우
                        File dfile = new File(getExternalPath() + "diary/" + editfile);
                        dfile.delete();
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).equals(editfile)) list.remove(i);
                        }
                        editfile = year + "-" +  numberFormat.format(month) + "-" + numberFormat.format(day) + ".memo";
                        edit();
                    }
                    else edit();
                }
                else{ //저장버튼
                    try {
                        String path = getExternalPath();
                        NumberFormat numberFormat = NumberFormat.getNumberInstance();
                        numberFormat.setMinimumIntegerDigits(2);
                        int year = datePicker.getYear() >= 100 ?
                                datePicker.getYear() % 100 : datePicker.getYear();
                        int month = datePicker.getMonth()+1;
                        int day = datePicker.getDayOfMonth();
                        File file = new File(path + "diary/" +
                                year + "-" +  numberFormat.format(month) + "-" + numberFormat.format(day) + ".memo");
                        if (file.exists()){ //저장할 파일명이 이미 존재하는경우
                            editfile = year + "-" +  numberFormat.format(month) + "-" + numberFormat.format(day) + ".memo";
                            load();
                        }
                        else{  //없는경우 새로저장
                            BufferedWriter bw = new BufferedWriter(new FileWriter(path + "diary/" +
                                    year + "-" +  numberFormat.format(month) + "-" + numberFormat.format(day) + ".memo"));
                            bw.write(editText.getText().toString());
                            bw.close();
                            Toast.makeText(getApplicationContext(),"저장완료",Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        bcancel = (Button) findViewById(R.id.btncancel);
        bcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  //취소
                bsave.setText("저장");
                editText.setText("");
                l1.setVisibility(View.VISIBLE);
                l2.setVisibility(View.INVISIBLE);
                listdir(list);
                adapter.notifyDataSetChanged();
            }
        });
        listView = (ListView) findViewById(R.id.listview);
        listdir(list);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //항목 선택시 수정모드
                editfile = list.get(position);
                load();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                //항목 길게 선택시 삭제
                AlertDialog.Builder dlg = new AlertDialog.Builder(Main2Activity.this);
                dlg.setTitle("항목삭제")
                        .setMessage("항목을 삭제하시겠습니까?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File file = new File(getExternalPath() + "diary/" + list.get(position));
                                file.delete();//실제 삭제
                                list.remove(position);
                                listdir(list);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(),
                                        "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("NO", null)
                        .show();
                return true;
            }
        });
    }

    public String getExternalPath(){
        String sdPath ="";
        String ext = Environment.getExternalStorageState();
        if(ext.equals(Environment.MEDIA_MOUNTED)){
            sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        }
        else sdPath = getFilesDir() + "";
        return sdPath;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        String str = null;
        if (requestCode == 100) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                str = "SD Card 쓰기권한 승인.";
            else str = "SD Card 쓰기권한 거부.";
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        }
    }

    public void load(){ //수정모드진입
        try {
            l1.setVisibility(View.INVISIBLE);
            l2.setVisibility(View.VISIBLE);
            int y = Integer.parseInt("20" + editfile.substring(0,2));
            int m = Integer.parseInt(editfile.substring(3,5))-1;
            int d = Integer.parseInt(editfile.substring(6,8));
            datePicker.init(y,m,d,null);
            BufferedReader br  = new BufferedReader(new FileReader(getExternalPath() + "diary/" + editfile));
            String readStr = "";
            String str = null;
            while((str = br.readLine()) != null) readStr += str + "\n";
            br.close();
            bsave.setText("수정");
            editText.setText(readStr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"File not found", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void edit(){ //수정작업을 진행(덮어쓰기)
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(getExternalPath() + "diary/" + editfile));
            bw.write(editText.getText().toString());
            bw.close();
            Toast.makeText(getApplicationContext(),"수정완료",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listdir(ArrayList<String> str){
        String path = getExternalPath();
        File[] files = new File(path + "diary").listFiles();
        for (File f : files) {
            if (!str.contains(f.getName())) str.add(f.getName());
        }
        tvc.setText("등록된 메모 개수: " + list.size());
        Collections.sort(str,cmpAsc);
    }

    Comparator<String> cmpAsc = new Comparator<String>(){
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    };

    public void onClick(View v){
        l1.setVisibility(View.INVISIBLE);
        l2.setVisibility(View.VISIBLE);
    }
}
