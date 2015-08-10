package com.example.vanteo89.stormy;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends ActionBarActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private CurrentWeather mCurrentWeather;
    @Bind(R.id.tv_humidity)
    TextView tvHumidity;
    @Bind(R.id.tv_humidity_value)
    TextView tvHumdityValue;
    @Bind(R.id.tvLocation)
    TextView tvLocation;
    @Bind(R.id.tv_precip)
    TextView tvPrecip;
    @Bind(R.id.tv_precip_value)
    TextView tvPrecipValue;
    @Bind(R.id.tv_temperate)
    TextView tvTemperate;
    @Bind(R.id.tv_time)
    TextView tvTime;
    @Bind(R.id.tvSummary)
    TextView tvSummary;
    @Bind(R.id.img_clound_night)
    ImageView imgClound;
    @Bind(R.id.imgRefesh)
    ImageView imgRefesh;
    @Bind(R.id.progressBar)
    ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mProgressBar.setVisibility(View.INVISIBLE);
        final double lattitude = 16.035621; //37.8267;
        final double longitude = 108.244557;//-122.423;
        imgRefesh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),"fkjdsfkjdsh",Toast.LENGTH_LONG).show();
                getForeCast(lattitude,longitude);
            }
        });
        getForeCast(lattitude, longitude);
    }

    private void getForeCast(double latitude,double longitude) {
        String apiKey = "9b030f2c8e522e496e64930931e36233";

        String foreCastUrl = "https://api.forecast.io/forecast/" + apiKey + "/" + latitude + "," + longitude;
        if (isNetworkValiable()) {
           toogleRefesh();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(foreCastUrl).build();
            Call call = client.newCall(request);
            //Schedules the request to be executed at some point in the future
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toogleRefesh();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toogleRefesh();
                        }
                    });
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        //Returns true if the code is in [200..300), which means the request was successfully received, understood, and accepted.
                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });

                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception Caught :", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception Caught :", e);
                    }

                }
            });
        } else {
            Toast.makeText(this, "Network is unvaliable", Toast.LENGTH_LONG).show();
        }
        Log.v(TAG, "Main UI Thread Runing");
    }

    private void toogleRefesh() {
        if(mProgressBar.getVisibility()==View.INVISIBLE){
            mProgressBar.setVisibility(View.VISIBLE);
            imgRefesh.setVisibility(View.INVISIBLE);
        }else{
            mProgressBar.setVisibility(View.INVISIBLE);
            imgRefesh.setVisibility(View.VISIBLE);
        }

    }

    private void updateDisplay() {
        tvTemperate.setText(mCurrentWeather.getmTemperature() + "");
        tvTime.setText("At " + mCurrentWeather.getFormattedTime() + " will be at");
        tvHumdityValue.setText(mCurrentWeather.getmHumidity() + "");
        tvPrecipValue.setText(mCurrentWeather.getmPrecipChance() + " %");
        tvSummary.setText(mCurrentWeather.getmSummary());
        Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId());
        imgClound.setImageDrawable(drawable);
        tvLocation.setText(mCurrentWeather.getTimeZone());

    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject foreCast = new JSONObject(jsonData);
        String timeZone = foreCast.getString("timezone");
        Log.d(TAG, timeZone);
        Log.i(TAG, "From Json :" + timeZone);
        JSONObject currently = foreCast.getJSONObject("currently");

        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setmHumidity(currently.getDouble("humidity"));
        currentWeather.setmTime(currently.getLong("time"));
        currentWeather.setmIcon(currently.getString("icon"));
        currentWeather.setmPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setmSummary(currently.getString("summary"));
        currentWeather.setmTemperature(currently.getDouble("temperature"));
        currentWeather.setTimeZone(timeZone);
        Log.d(TAG, currentWeather.getFormattedTime());
        return currentWeather;
    }

    private boolean isNetworkValiable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlterDialogFragment dialogFragment = new AlterDialogFragment();
        dialogFragment.show(getFragmentManager(), "error dialog");
    }


}
