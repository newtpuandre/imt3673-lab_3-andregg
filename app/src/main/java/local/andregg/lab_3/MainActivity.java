package local.andregg.lab_3;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor sensor;
    private Vibrator v;

    private TextView currentHeight;
    private TextView highscore;
    private TextView throwStatus;
    private MediaPlayer mp;

    private final float EarthGravity = 9.81f;
    private boolean inAir = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,sensor,sensorManager.SENSOR_DELAY_NORMAL);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        currentHeight = findViewById(R.id.currentheight_txt);
        highscore = findViewById(R.id.highscore_txt);
        throwStatus = findViewById(R.id.throwstatus_txt);
        mp = MediaPlayer.create(this, R.raw.popsound);

    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,sensor,sensorManager.SENSOR_DELAY_NORMAL);
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float biggestAcc;

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float ACC = (float) Math.sqrt(x*x + y*y + z*z) - EarthGravity;

            if (ACC >= 40 && !inAir) { //TODO: change to slider in preferences
                heightCalculation(ACC);
            }
        }
    }


    public void heightCalculation(final float acceleration){
        //Set textboxes etc
        inAir = true;
        v.vibrate(500);
        final float t = acceleration / EarthGravity;
        final float s = acceleration * t + 1/2 * -EarthGravity * (float) Math.pow(t,2);
        //Count to time and play sound
        //Calculate height and get high score if it is higher

        updateText( (int) t * 1000, s);

    }

    private void updateText(final int maxtime, final float maxHeight) {
        throwStatus.setText("In the air..");
        new CountDownTimer((long) maxtime * 2, 100) {

            float countNumber = ((maxHeight / maxtime)) * 100 ;
            float curHeight;
            boolean atTop = false;
            public void onTick(long millisUntilFinished) {

                if(!atTop) {
                    curHeight += countNumber;
                    currentHeight.setText(String.valueOf(curHeight));
                    Log.d("app1", "countNumber " + countNumber + " " + maxHeight / maxtime);
                }

                if ((float) millisUntilFinished <= (maxtime) && !atTop) {
                    Log.d("app1", "At the top" + millisUntilFinished);
                    currentHeight.setText(String.valueOf(maxHeight));
                    Log.d("app1", "" + maxHeight);
                    throwStatus.setText("Reached the top!");
                    mp.start();
                    atTop = true;
                }

            }

            public void onFinish() {
                inAir = false;
                throwStatus.setText("Landed... Throw again!");
                Log.d("app1", "Bottom" );
                v.vibrate(200);

                float curHighscore = Float.valueOf(highscore.getText().toString());
                Log.d("app1", "HIGHSCORE" + curHighscore);
                if(maxHeight >= curHighscore) {
                    highscore.setText(String.valueOf(maxHeight));
                }
            }
        }.start();

    }

}
