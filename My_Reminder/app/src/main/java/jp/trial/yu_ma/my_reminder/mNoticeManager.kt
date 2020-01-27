package jp.trial.yu_ma.my_reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class mNoticeManager {

    private lateinit var notificationManager: NotificationManager
    private lateinit var name: String
    private lateinit var id: String

    fun notify(context: Context, intent_Id: Int, text: String?, datetime: String?) {

        //NotificationManagerのインスタンスの取得
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // カテゴリー名（通知設定画面に表示される情報）
        name = "通知設定"
        // システムに登録するChannelのID
        id = "Re:minder_Notification_Channel_Id"

        // Channelの取得と生成
        if (notificationManager.getNotificationChannel(id) == null) {
            //第１引数はアプリで固有のID
            //第２引数はユーザーが設定アプリで見えるチャンネル
            val mChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
            //チャンネルを登録し設定アプリで見える様にする
            notificationManager.createNotificationChannel(mChannel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val notification = NotificationCompat
            .Builder(context, id) //第２引数のidで通知にチャンネルを紐づける
            .apply {
                setSmallIcon(R.drawable.ic_stat_name)
                setContentTitle(text)
                setContentText(datetime)
                setContentIntent(pendingIntent)
                setAutoCancel(true)
            }.build()

        notificationManager.notify(intent_Id, notification)

    }

}