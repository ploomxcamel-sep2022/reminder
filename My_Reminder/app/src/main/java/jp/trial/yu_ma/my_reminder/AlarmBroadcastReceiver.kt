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

        //ブロードキャストの種類を判別
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            //端末の再起動後の処理（アラームの再セット）
            Log.d("ログ","端末の起動を受信")

            //データベースから全てのスケジュールを取得
            val realm: Realm = Realm.getDefaultInstance()
            val schedules = realm.where<Schedule>().findAll()
            //idの個数からループ回数を設定する
            val maxId = schedules.where().max("id")
            val MaxLoop: Int? = maxId?.toInt() //idはLong型なのでInt型にキャスト

            if (MaxLoop != null) { //idの存在チェック
                for (sarch_Id in 1..MaxLoop) { //MaxLoopの個数分繰り返す
                    var schedule_data = schedules.where().equalTo("id", sarch_Id).findFirst() //順にスケジュールを取得
                    if (schedule_data?.dateTime == null) { //時間設定の存在チェック
                        //時間の設定が無ければ何もしない
                    } else { //データベースから時間設定が存在するスケージュールを再設定
                        if (schedule_data.notice_switch == "YET") { //通知前のスケジュールか確認
                            var triggerTime = Calendar.getInstance()
                            triggerTime.time = schedule_data?.dateTime
                            var mAlarm = mAlarmManager()
                            mAlarm.setAlarm(context, triggerTime, sarch_Id)
                        } else { /*通知済（YET以外）なら何もしない*/ }
                    }
                }
            } else { /*データベースが空なら何もしない*/ }

            realm.close()

        } else { //通常のブロードキャストを受信

            //通常のブロードキャストの処理（通知を出す処理）
            val intent_Id: Int = intent.getIntExtra("scheduleId", 0)
            val realm: Realm = Realm.getDefaultInstance()
            var schedule_data: Schedule? = realm.where<Schedule>().equalTo("id", intent_Id).findFirst()
            if(schedule_data?.notice_switch != null) { //nullチェック
                if (schedule_data.notice_switch == "YET") { //未通知か確認
                    val text: String? = schedule_data?.title
                    val datetime: String? =
                        android.text.format.DateFormat.format("yyyy'年'MM'月'dd'日、'(EEE) kk'時'mm'分'", schedule_data?.dateTime)
                            .toString()

                    //通知を出す
                    var mNotice = mNoticeManager()
                    mNotice.notify(context, intent_Id, text, datetime)

                    //通知を行ったらnotice_switchにYET以外を書き込む
                    realm.executeTransaction {
                        schedule_data.notice_switch = "DONE"
                    }

                    realm.close()

                } else { /*通知済（YET以外）なら何もしない*/ }
            } else { /*nullなら何もしない*/ }
        }
    }
}
