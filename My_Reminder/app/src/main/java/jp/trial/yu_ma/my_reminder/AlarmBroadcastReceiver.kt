package jp.trial.yu_ma.my_reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.realm.Realm
import io.realm.kotlin.where
import java.util.*

class AlarmBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        //端末の再起動後の処理（アラームの再セット）
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            Log.d("ログ","端末の起動を受信")

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
                        if (schedule_data.notice_switch == "YET") {
                            var triggerTime = Calendar.getInstance()
                            triggerTime.time = schedule_data?.dateTime
                            var mAlarm = mAlarmManager()
                            mAlarm.setAlarm(context, triggerTime, sarch_Id)
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
                    realm.executeTransaction {
                        schedule_data.notice_switch = "DONE"
                    }
                    realm.close()
                    Log.d("ログ","Broadcast_close")
                } else { /*何もしない*/ }
            } else { /*何もしない*/ }
        }
    }
}
