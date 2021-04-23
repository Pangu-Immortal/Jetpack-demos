package in.dotworld.workmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    TextView oneTime, periodic, chaining;
    WorkManager mWorkmanager;
    Button onetime, periodic_request, chaining_request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        oneTime = findViewById(R.id.oneTime);
        periodic = findViewById(R.id.periodic);
        chaining = findViewById(R.id.chaining);


        onetime = findViewById(R.id.onetime);
        periodic_request = findViewById(R.id.periodic_request);
        chaining_request = findViewById(R.id.chaining_request);
    }

    @Override
    public void onStart() {
        super.onStart();

        onetime.setOnClickListener(this);
        periodic_request.setOnClickListener(this);
        chaining_request.setOnClickListener(this);
    }


    public void excuteOneTimeRequest() {
        // 设置任务触发条件。例如，可以设置在设备处于充电，网络已连接，且电池电量充足的状态下，才触发设置的任务。
        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresDeviceIdle(false)
                .build();
        // 一次性 WorkRequest
        final OneTimeWorkRequest oneTimeWorkRequest =
                new OneTimeWorkRequest.Builder(Myworker.class)
                        .setInitialDelay(10, TimeUnit.SECONDS) // 符合触发条件后，延迟10秒执行
                        .setBackoffCriteria(BackoffPolicy.LINEAR, OneTimeWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS) // 设置指数退避算法
                        .addTag("OneTimeWorkRequestTag")
                        .build();
        // 最后，需要使用 enqueue() 方法将 WorkRequest 提交到 WorkManager。
        WorkManager.getInstance(getApplicationContext()).enqueue(oneTimeWorkRequest).getResult();
        // 任务在提交给系统后，通过WorkInfo获知任务的状态，WorkInfo包含了任务的id，tag，以及Worker对象传递过来的outputData，以及任务当前的状态。
        // 如果你希望能够实时获知任务的状态。这三个get方法还有对应的LiveData方法。
        WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(oneTimeWorkRequest.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        String status = workInfo.getState().toString();
                        oneTime.append("\n" + status);
                        Data data = workInfo.getOutputData();
                        Log.i("MainActivity", status);
                        if (workInfo.getState().isFinished()) {
                            oneTime.append("\n" + data.getString("value"));
                        }
                        Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
                    }
                });
        // 取消任务。与观察任务类似的，我们也可以根据Id或者Tag取消某个任务，或者取消所有任务。
        findViewById(R.id.onetime_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WorkManager.getInstance(getApplicationContext()).cancelWorkById(oneTimeWorkRequest.getId());
            }
        });
    }


    public void excutePeriodicRequest() {

        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresDeviceIdle(false)
                .build();
        // 周期任务
        final PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest
                        .Builder(NewWorker.class, 15, TimeUnit.MINUTES, 5, TimeUnit.MINUTES)
                        .setConstraints(constraints).build();

        WorkManager.getInstance(getApplicationContext()).enqueue(periodicWorkRequest).getResult();

        WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(periodicWorkRequest.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        String status = workInfo.getState().toString();
                        periodic.append("\n" + status);
                    }
                });
        findViewById(R.id.periodic_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WorkManager.getInstance(getApplicationContext()).cancelWorkById(periodicWorkRequest.getId());
            }
        });

    }

    public void excuteChainRequest() {

        OneTimeWorkRequest oneTimeWorkRequest1 = new OneTimeWorkRequest.Builder(Myworker.class).build();
        OneTimeWorkRequest oneTimeWorkRequest2 = new OneTimeWorkRequest.Builder(NewWorker.class).build();
        OneTimeWorkRequest oneTimeWorkRequest3 = new OneTimeWorkRequest.Builder(Myworker.class).build();

        // 任务链。如果你有一系列的任务需要顺序执行，那么可以利用如下调用链。
        WorkManager.getInstance(getApplicationContext())
                .beginWith(oneTimeWorkRequest1)
                .then(oneTimeWorkRequest2)
                .then(oneTimeWorkRequest3)
                .enqueue().getResult();

        WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(oneTimeWorkRequest1.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        String staus = workInfo.getState().toString();
                        chaining.append("\n" + staus);
                    }
                });

        findViewById(R.id.chaining_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WorkManager.getInstance(getApplicationContext()).cancelAllWork();
            }
        });

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.onetime:
                excuteOneTimeRequest();       //OneTimeRequest
                break;

            case R.id.periodic_request:
                excutePeriodicRequest();     //PeriodicRequest
                break;

            case R.id.chaining_request:
                excuteChainRequest();        //ChainingRequest
                break;
        }
    }
}