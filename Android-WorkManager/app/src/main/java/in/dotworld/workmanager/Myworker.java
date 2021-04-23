package in.dotworld.workmanager;


import android.content.Context;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


public class Myworker extends Worker {
    public static int a=10, b=20;
    public Myworker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        a++;
        b++;
        Data data= new Data.Builder().putString("value",String.valueOf(a+b)).build();
        try {
            Thread.sleep(15000);
            Log.i("Myworker", String.valueOf(a+b));
            return Result.success(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.failure();
        }


    }


}
