package jp.trial.yu_ma.my_reminder

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import io.realm.Realm
import io.realm.kotlin.where
import java.util.*

class AlarmBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        /*
        //NotificationManagerのインスタンスの取得
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // カテゴリー名（通知設定画面に表示される情報）
        val name = "通知設定"
        // システムに登録するChannelのID
        val id = "Notification_Channel"
        // 通知の詳細情報（通知設定画面に表示される情報）
        val notifyDescription = "この通知の詳細情報を設定します"

        // Channelの取得と生成
        if (notificationManager.getNotificationChannel(id) == null) {
            //第１引数はアプリで固有のID
            //第２引数はユーザーが設定アプリで見えるチャンネル
            val mChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
            mChannel.apply {
                description = notifyDescription
            }
            //チャンネルを登録し設定アプリで見える様にする
            notificationManager.createNotificationChannel(mChannel)
        }*/

        //端末の再起動後の処理（アラームの再セット）
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {

            //データベースから全てのスケジュールを取得
            val realm: Realm = Realm.getDefaultInstance()
            val schedules = realm.where<Schedule>().findAll()
            val maxId = schedules.where().max("id")
            
            val MaxLoop: Int? = maxId?.toInt()

            if (MaxLoop != null) {
                for (sarch_Id in 1..MaxLoop) {
                    var schedule_data = schedules.where().equalTo("id", sarch_Id).findFirst()
                    if (schedule_data?.dateTime == null) {
                        //何もしない
                    } else { //データベースから存在するスケージュールを再セット
                        //println("ID: " + schedule_data?.id + " のスイッチは " + schedule_data?.notice_switch + " です")
                        if (schedule_data.notice_switch == "YET") {
                            /*var am = context.getSystemService(ALARM_SERVICE) as AlarmManager
                            var intent = Intent(context, AlarmBroadcastReceiver::class.java)
                            intent.putExtra("scheduleId", sarch_Id)
                            val pending =
                                PendingIntent.getBroadcast(context, sarch_Id, intent, PendingIntent.FLAG_UPDATE_CURRENT)*/
                            var triggerTime = Calendar.getInstance()
                            triggerTime.time = schedule_data?.dateTime
                            var mAlarm = mAlarmManager()
                            mAlarm.setAlarm(context, triggerTime, sarch_Id)
                            //am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime.timeInMillis, pending)
                        } else { /*何もしない*/ }
                    }
                }
            } else {
                //何もしない
            }

            realm.close()

        } else {

            //通常のブロードキャストの処理（通知を出す処理）
            val intent_Id: Int = intent.getIntExtra("scheduleId", 0)
            val realm: Realm = Realm.getDefaultInstance() //Realmクラスのインスタンスを取得、データベース使用準備完了
            var schedule_data: Schedule? = realm.where<Schedule>().equalTo("id", intent_Id).findFirst()
            if(schedule_data?.notice_switch != null) {
                if (schedule_data.notice_switch == "YET") {
                    val text: String? = schedule_data?.title
                    val datetime: String? =
                        android.text.format.DateFormat.format("yyyy'年'MM'月'dd'日、'(EEE) kk'時'mm'分'", schedule_data?.dateTime)
                            .toString()

                    //通知を出す
                    var mNotice = mNoticeManager()
                    mNotice.notify(context, intent_Id, text, datetime)
                    /*val intent = Intent(context, MainActivity::class.java).apply {
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
                        }.build()*/
                    realm.executeTransaction {
                        schedule_data.notice_switch = "DONE"
                    }
                    realm.close()
                    Log.d("ログ","Broadcast_close")
                    //notificationManager.notify(intent_Id, notification)
                } else { /*何もしない*/ }
            } else { /*何もしない*/ }

        }

    }

}