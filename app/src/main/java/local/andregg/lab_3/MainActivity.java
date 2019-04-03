package local.andregg.lab_3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    //Shared pref variable
    public static final String PREFS_NAME = "BallThrowGame";

    //Sensor Variables
    private SensorManager sensorManager;
    private Sensor sensor;

    //Vibration variables
    private Vibrator v;

    //UI element variables
    private TextView currentHeight;
    private TextView highscore;
    private TextView throwStatus;
    private TextView highscoreStatus;
    private Button preferencebtn;

    //Media player for playing sound at top
    private MediaPlayer mp;

    //Constants for calculations
    private int MIN_ACC = 20;
    private int SLIDINGWINDOW_SIZE = 4;
    private final float EarthGravity = 9.81f;

    //Return code for preference activity
    public static final int REQUEST_CODE = 1;

    //Holding last SLIDINGWINDOWS_SIZE amount of sensor reading
    ArrayList<Float> SlidingWindow;

    //Bool for keeping track of ball state
    private boolean inAir = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup sensormanager and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,sensor,sensorManager.SENSOR_DELAY_NORMAL);

        //Setup the vibration
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Setup the UI elements
        currentHeight = findViewById(R.id.currentheight_txt);
        highscore = findViewById(R.id.highscore_txt);
        throwStatus = findViewById(R.id.throwstatus_txt);
        highscoreStatus = findViewById(R.id.highscorestatus_txt);
        preferencebtn = findViewById(R.id.preference_button);

        //Setup the MediaPlayer
        mp = MediaPlayer.create(this, R.raw.popsound);

        //Initialize the Array List
        SlidingWindow = new ArrayList<>();

        //Setup and get shared preference
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        MIN_ACC = prefs.getInt("MIN_ACC", 20);

        //Handle button press
        preferencebtn.setOnClickListener(v -> {
            Intent I = new Intent(MainActivity.this, PreferenceActivity.class);
            I.putExtra("MIN_ACC", MIN_ACC);
            startActivityForResult(I, REQUEST_CODE);
        });
    }

    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,sensor,sensorManager.SENSOR_DELAY_NORMAL);
    }

    // This method is invoked when target activity return result data back.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        switch (requestCode)
        {
            case REQUEST_CODE:
                if(resultCode == RESULT_OK)
                {
                    MIN_ACC = dataIntent.getIntExtra("MIN_ACC", 0);
                }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Only do stuff for the accelerometer
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            //accelerometer readings
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            //Calculate acceleration
            float ACC = (float) Math.sqrt(x*x + y*y + z*z) - EarthGravity;

            //Is larger that threshold and not in air.
            if (ACC >= MIN_ACC && !inAir) {
                SlidingWindow.add(ACC); //Add Acceleration to ArrayList
                if(SlidingWindow.size() >= SLIDINGWINDOW_SIZE) { //Is list larger than SlidingWindowSize?
                    inAir = true; //We are in the air
                    findHighestAcc(); //Find highest acceleration
                }
            }
        }
    }

    public void findHighestAcc(){
        float highestAcc = 0; //Temp value
        for (int i = 0; i < SlidingWindow.size(); i++) { //Loop over array
            float temp = SlidingWindow.get(i); //Get element I
            if ( temp > highestAcc) { //Save if higher than the one stored.
                highestAcc = temp;
            }
        }
        heightCalculation(highestAcc); //Calculate height
        SlidingWindow.clear(); //Clear SlidingWindows arraylist
    }

    public void heightCalculation(final float acceleration){
        highscoreStatus.setText(""); //Clear text
        v.vibrate(500);  //Vibrate indicating ball leaving hand.
        final float t = acceleration / EarthGravity; //Get time til top
        final float s = acceleration * t + 1/2 * -EarthGravity * (float) Math.pow(t,2); //Get max height.

        updateText( (int) t * 1000, s);

    }

    private void updateText(final int maxtime, final float maxHeight) {
        throwStatus.setText("In the air.."); //Set text
        new CountDownTimer((long) maxtime * 2, 100) {

            float countNumber = ((maxHeight / maxtime)) * 100 ; //Find amount of ticks to do
            float curHeight; //Variable holding current height
            boolean atTop = false; //Keeping track of state
            public void onTick(long millisUntilFinished) {

                if(!atTop) { //Not at top
                    curHeight += countNumber; //Keep counting height
                    currentHeight.setText(String.valueOf(curHeight));
                }

                if ((float) millisUntilFinished <= (maxtime) && !atTop) { //We reached the top
                    currentHeight.setText(String.valueOf(maxHeight));
                    throwStatus.setText("Reached the top!");
                    mp.start(); //Play sound
                    atTop = true;

                    float curHighscore = Float.valueOf(highscore.getText().toString());
                    if(maxHeight >= curHighscore) { //Set new highscore
                        highscoreStatus.setText("New Highscore!");
                        highscore.setText(String.valueOf(maxHeight));
                    }
                }
            }

            public void onFinish() { //Reset variables.
                inAir = false;
                throwStatus.setText("Landed... Throw again!");
                v.vibrate(200);
            }
        }.start(); //Start countdown

    }

}
