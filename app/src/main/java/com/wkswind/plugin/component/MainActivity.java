package com.wkswind.plugin.component;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.wkswind.plugin.demo.test2.Test2Helper;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Test2Helper.INSTANCE.test2();
  }
}
