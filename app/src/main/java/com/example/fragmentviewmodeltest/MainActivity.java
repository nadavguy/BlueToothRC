package com.example.fragmentviewmodeltest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.view.WindowManager;

import java.util.*;

import com.example.fragmentviewmodeltest.ui.main.MainFragment;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static MainActivity outInstance = new MainActivity();
    public static MainActivity getInstance() { return  outInstance;}

    private SensorManager sensorManager;
    private Sensor mLight;
    private Sensor mAcc;
    private Sensor mMag;
    private Sensor mRotation;
    private boolean IsStarted = false;
    private  float lux;
    private float Roll;
    private float Pitch;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private float RotationMatrix[] = new float[9];
    private float InclinationMatrix[] = new float[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_GAME);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        MainActivity.getInstance().setLux(event.values[0]); //lux =
        // Do something with this sensor value.

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
            int a = 0;
        }
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
            int b = 0;
        }

        if (magnetometerReading != null && accelerometerReading != null)
        {
//            float R[] = new float[9];
//            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(RotationMatrix, InclinationMatrix, accelerometerReading , magnetometerReading);
            if (success)
            {
                float orientation[] = new float[3];
                SensorManager.getOrientation(RotationMatrix, orientation);
//                System.out.println(orientation[0] + " " + orientation[1] + " " + orientation[2]);
                MainActivity.getInstance().setPitch(orientation[1]);
                MainActivity.getInstance().setRoll(orientation[2]);
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, mMag, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
