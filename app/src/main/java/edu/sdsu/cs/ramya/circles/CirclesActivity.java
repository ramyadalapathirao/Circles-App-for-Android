package edu.sdsu.cs.ramya.circles;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;


public class CirclesActivity extends ActionBarActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private CirclesDrawingView view;
    private long lastUpdateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circles);
        view=(CirclesDrawingView)findViewById(R.id.circleView);
        sensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (lastUpdateTime == 0) {
            lastUpdateTime = event.timestamp;
            return;
        }
        long timeDelta = event.timestamp - lastUpdateTime;
        lastUpdateTime = event.timestamp;
        float xAcceleration = round(event.values[0]);
        float yAcceleration = round(event.values[1]);
        view.accelerate(xAcceleration, yAcceleration, timeDelta/1000000000.0f);
    }

    private float round(float value)
    {
        return Math.round(value*100)/100.0f;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_circles, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_clear)
        {
            view.clearView();
        }
        if(id == R.id.action_accelerometer)
        {
            if(item.getTitle().equals(getResources().getString(R.string.action_accelerometer)))
            {
                item.setTitle(R.string.action_disable_accelerometer);
                view.enableAccelerometer(true);
            }
            else
            {
                item.setTitle(R.string.action_accelerometer);
                view.enableAccelerometer(false);

            }

        }

        return super.onOptionsItemSelected(item);
    }

}
