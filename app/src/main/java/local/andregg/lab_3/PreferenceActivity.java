package local.andregg.lab_3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class PreferenceActivity extends AppCompatActivity {

    //UI Element variables
    private SeekBar sensitivitySlider;
    private TextView sensitivty_text;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        //Setup UI elements
        sensitivitySlider = findViewById(R.id.sensitivity_seek);
        sensitivty_text = findViewById(R.id.sensitivity_txt);
        saveBtn = findViewById(R.id.save_btn);

        //Set max value
        sensitivitySlider.setMax(30);

        //Get intent passed when activity started
        Intent Intent = getIntent();
        int temp_min_acc = Intent.getIntExtra("MIN_ACC", 10) - 10;

        //Set slider progress
        sensitivitySlider.setProgress(temp_min_acc);

        sensitivty_text.setText(String.valueOf(sensitivitySlider.getProgress() + 10));

        sensitivitySlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress + 10;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}

            public void onStopTrackingTouch(SeekBar seekBar) {
                sensitivty_text.setText(String.valueOf(progressChangedValue));
            }
        });

        saveBtn.setOnClickListener(v -> {
            //Create a new intent
            Intent I = new Intent();
            //Put extras
            I.putExtra("MIN_ACC", sensitivitySlider.getProgress() + 10);

            //Save preference
            SharedPreferences.Editor editor = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE).edit();
            editor.putInt("MIN_ACC", sensitivitySlider.getProgress() + 10);
            editor.apply();
            //Set result
            setResult(RESULT_OK, I);
            //Finish activity
            finish();
        });
    }
}
