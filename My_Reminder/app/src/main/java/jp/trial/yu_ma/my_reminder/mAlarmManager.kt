package jp.trial.yu_ma.my_reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

class mAlarmManager {

    //トリガー時刻のセット
    fun setAlarm(context: Context, triggerTime: Calendar, id: Int) {

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmBroadcastReceiver::class.java)
        intent.putExtra("scheduleId", id)
        val pending = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime.timeInMillis, pending)
        Log.d("ログ","setAlarm_ID" + id)
    }

    //トリガー時刻のキャンセル
    fun cancelAlarm(context: Context, id: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmBroadcastReceiver::class.java)
        intent.putExtra("scheduleId", id)
        val pending = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        am.cancel(pending)
        Log.d("ログ","cancelAlarm_ID" + id)
    }
}