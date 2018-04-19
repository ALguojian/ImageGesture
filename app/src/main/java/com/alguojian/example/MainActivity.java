package com.alguojian.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.alguojian.imagegesture.ImageGesture;

import java.util.ArrayList;

/**
 * @author alguojian
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void open(View view){
        String url = "https://xdimg.meiyezhushou.com/xd/share/img/8128b16277044f2f.jpg!/watermark/url/L3hkL3FyL215emh1c2hvdV81YWMxZWU1MTIwMTQ2MjA3ODljOWIyYTAuanBn/margin/100x750/percent/20";
        String gif = "https://xmimg.meiyezhushou.com/xd/share/img/d72d5162ae807242.jpg!/watermark/url/L3hkL3FyL215emh1c2hvdV81YTdlZjZmOGE3Y2E2ZDA0ZWE3NTFkMWQuanBn/margin/308x9262/percent/23";


        ArrayList<String> strings = new ArrayList<>();
        strings.add(gif);
        strings.add(url);

        ImageGesture.setDate(this,1,strings);
    }
}
