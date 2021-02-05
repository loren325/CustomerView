package com.loren.CustomerView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;


import com.loren.CustomerView.bubble.MessageEntity;
import com.loren.CustomerView.ship.WaveView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        RecyclerView rv = findViewById(R.id.rv);
//        rv.setLayoutManager(new LinearLayoutManager(this));
//        rv.setAdapter(new RvAdapter(initData()));

        setContentView(new WaveView(this));
    }

    private ArrayList<MessageEntity> initData(){
        ArrayList<MessageEntity> msgList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            MessageEntity messageEntity = new MessageEntity();
            messageEntity.title = "标题" + i;
            messageEntity.content = "这是个测试的消息" + i;
            messageEntity.msgCount = "2" + i;
            msgList.add(messageEntity);
        }
        return msgList;
    }
}