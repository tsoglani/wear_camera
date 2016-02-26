package camera.nikos.tsoglani.camera;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {
private RelativeLayout start_layout;
    static boolean isMainOnFront;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start_layout=(RelativeLayout)findViewById(R.id.start_layout);
        start_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CameraActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        isMainOnFront=false;
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        isMainOnFront=true;
//    }
}
