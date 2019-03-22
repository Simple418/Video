package com.example.video_mobile;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




public class MainActivity extends AppCompatActivity {
    Button btn_add;
    int REQUEST_CODE;
    ListView listView;
    ArrayList names = new ArrayList(); //显示名
    ArrayList fileNames = new ArrayList();//存储文件路径
    int i = 0;

    // 创建一个List集合，List集合的元素是Map
    List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
    /*// 将names集合对象的数据转换到Map集合中
    Map<String, Object> listItem = new HashMap<String, Object>();*/
    // 创建一个SimpleAdapter
    SimpleAdapter simpleAdapter;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    public void verifyStoragePermissions() {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(MainActivity.this,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyStoragePermissions();

        btn_add = findViewById(R.id.btn_add);
        listView = findViewById(R.id.ListView);

        //清空names、descs、fileNames集合里原有的数据。
        names.clear();
        fileNames.clear();


        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_CODE);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && null != data) {
            Uri selectedVideo = data.getData();
            String[] filePathColumn = {MediaStore.Video.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedVideo,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();


            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String videoPath = cursor.getString(columnIndex);
            String name = getFileName(videoPath);
            names.add(name);
            fileNames.add(videoPath);

          /*  // 创建一个List集合，List集合的元素是Map
            List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
            // 将names集合对象的数据转换到Map集合中
            Map<String, Object> listItem = new HashMap<String, Object>();*/
            listItems.clear();
            //simpleAdapter.notifyDataSetChanged();
             for(int a=0; a<=i; a++) {
                 Map<String, Object> listItem = new HashMap<String, Object>();
                 listItem.put("video_name", names.get(a));
                 Log.i("i", names.get(a) + "");
                 listItems.add(listItem);
             }
             i++;
            simpleAdapter = new SimpleAdapter(
                    MainActivity.this, listItems, R.layout.item_layout, new String[]{"video_name"}, new int[]{R.id.video_name});
            // 为listView组件设置Adapter
            listView.setAdapter(simpleAdapter);

            // 为listView的列表项单击事件添加监听器
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View source, int position, long id) {
                    //点击某一个listview的item
                    Intent broadcast = new Intent(MainActivity.this,Broadcast.class);
                    String path = (String)fileNames.get(position);
                    Log.i("path",path);
                    broadcast.putExtra("path",path);
                    startActivity(broadcast);


                }
            });
            cursor.close();
        }
    }

    public String getFileName(String pathandname){

        int start=pathandname.lastIndexOf("/");
        int end=pathandname.lastIndexOf(".");
        if(start!=-1 && end!=-1){
            return pathandname.substring(start+1,end);
        }else{
            return null;
        }
    }
}
