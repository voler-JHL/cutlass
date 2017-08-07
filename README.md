# cutlass
利用@InjectField注解，自动解析上一个界面传递的值。

接收值的Activity
```
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
```
传值的Activity
```
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent injectActivity = IntentFactory.createInjectActivity("string", 123, new char[]{'1', '2'}, new Person("voler", false), new ArrayList<Student>());
        startActivity(injectActivity);
    }
}
```
# 引入
```
    compile 'com.voler:cutlass-annotation:1.0.0'
    annotationProcessor 'com.voler:cutlass-compiler:1.0.0'
```
@InjectField是一个编译时注解。在build时检测哪些字段是带有@InjectField的，会在编译时为每一个Fragment或Activity生成一个辅助解析类，
就是这个类在 Cutlass.inject(this)时，完成字段赋值操作；与butterknife原理一样，被修饰的字段不能为private；
编译时还会额外生成两个类，为Activity生成的IntentFactory和为Fragment生成的FragmentFactory，这两个类中包含的是能生成相应Intent或Fragment
的静态方法，按参数填入对应的值即可。
