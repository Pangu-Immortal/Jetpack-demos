package in.dotworld.workmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NewWorker extends Worker {
    public NewWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
            int a=10, b=10;
            shownotificaiton();
            Log.i("MyworkerNew",String.valueOf(a+b));
            return Result.success();
    }

    public void shownotificaiton()
    {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "task_channel";
        String channelName = "task_name";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new
                    NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        Notification builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setContentTitle("Repeat")
                .setContentText("Running")
                .setSmallIcon(R.drawable.ic_launcher_foreground).build();
        manager.notify(1, builder);
    }
}
