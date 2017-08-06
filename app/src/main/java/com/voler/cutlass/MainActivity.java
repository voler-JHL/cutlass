package com.voler.cutlass;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, InjectActivity.class);
        intent.putExtra("string","这是由MainActivity传来的<font color=\"#00A0E9\">C</font><font color=\"#E4007F\">M</font><font color=\"#FFF100\">Y</font><font color=\"#000000\">K</font>");
        intent.putExtra("number",52054843);
        intent.putExtra("chars",new char[]{'v','o','l','e','r',' ','m','e','a','n','s',' ','l','o','v','e','r'});
        intent.putExtra("obj",new Person("Voler",false));
        ArrayList<Student> students = new ArrayList<>();
        students.add(new Student("Loof",69));
        students.add(new Student("Fool",25));
        intent.putParcelableArrayListExtra("studentList",students);
        startActivity(intent);
    }
}
