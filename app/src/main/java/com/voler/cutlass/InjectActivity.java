package com.voler.cutlass;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.TextView;

import com.voler.cutlass.annotation.InjectField;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 三尺春光驱我寒，一生戎马为长安
 * Created by Han on 17/7/10.
 */

public class InjectActivity extends AppCompatActivity {

    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    private TextView tv4;
    private TextView tv5;

    @InjectField
    String string;
    @InjectField
    int  number;
    @InjectField
    char[]  chars;
    @InjectField
    Serializable obj;
    @InjectField
    ArrayList<Student> studentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inject);
        initView();
        Cutlass.inject(this);

        tv1.setText(Html.fromHtml(string));
        tv2.setText(String.valueOf(number));
        tv3.setText(Arrays.toString(chars));
        tv4.setText(obj.toString());
        tv5.setText(StringUtils.join(studentList,","));
    }

    private void initView() {
        tv1 = (TextView) findViewById(R.id.tv_1);
        tv2 = (TextView) findViewById(R.id.tv_2);
        tv3 = (TextView) findViewById(R.id.tv_3);
        tv4 = (TextView) findViewById(R.id.tv_4);
        tv5 = (TextView) findViewById(R.id.tv_5);
    }
}
