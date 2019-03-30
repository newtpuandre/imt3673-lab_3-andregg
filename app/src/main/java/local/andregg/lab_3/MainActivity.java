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
    private TextView highscoreStatus;
    private MediaPlayer mp;

    ArrayList<Float> test;

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
        highscoreStatus = findViewById(R.id.highscorestatus_txt);
        mp = MediaPlayer.create(this, R.raw.popsound);

        test = new ArrayList<>();
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

            if (ACC >= 15 && !inAir) { //TODO: change to slider in preferences
                test.add(ACC);
                if(test.size() >= 4) {
                    inAir = true;
                    findHighestAcc();
                }
                //heightCalculation(ACC);
                Log.d("app1", "" + ACC);
            }
        }
    }

    public void findHighestAcc(){
        float highestAcc = 0;
        for (int i = 0; i < test.size(); i++) {
            float temp = test.get(i);
            if ( temp > highestAcc) {
                highestAcc = temp;
            }
        }
        Log.d("app1", "Highest Acc" + highestAcc);
        heightCalculation(highestAcc);
        test.clear();
    }

    public void heightCalculation(final float acceleration){
        //Set textboxes etc
        highscoreStatus.setText("");
        v.vibrate(500);
        final float t = acceleration / EarthGravity;
        final float s = acceleration * t + 1/2 * -EarthGravity * (float) Math.pow(t,2);

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
                }

                if ((float) millisUntilFinished <= (maxtime) && !atTop) {
                    currentHeight.setText(String.valueOf(maxHeight));
                    throwStatus.setText("Reached the top!");
                    mp.start();
                    atTop = true;
                }

            }

            public void onFinish() {
                inAir = false;
                throwStatus.setText("Landed... Throw again!");
                v.vibrate(200);

                float curHighscore = Float.valueOf(highscore.getText().toString());
                if(maxHeight >= curHighscore) {
                    highscoreStatus.setText("New Highscore!");
                    highscore.setText(String.valueOf(maxHeight));
                }
            }
        }.start();

    }

}
