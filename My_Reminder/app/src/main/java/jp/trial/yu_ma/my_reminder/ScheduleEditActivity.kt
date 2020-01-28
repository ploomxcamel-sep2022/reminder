package jp.trial.yu_ma.my_reminder

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_schedule_edit.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ScheduleEditActivity : AppCompatActivity()
    , DatePickerFragment.OnDateSelectedListener
    , TimePickerFragment.OnTimeSelectedListener {

    private lateinit var realm: Realm

    //日付をテキストビューにセット
    override fun onSelected(year: Int, month: Int, date: Int) {
        val c = Calendar.getInstance()
        c.set(year, month, date)
        dateText.text = DateFormat.format("yyyy'年'MM'月'dd'日、'(EEE)", c)

        //時刻選択ダイアログを呼び出す
        val dialog = TimePickerFragment()
        dialog.show(supportFragmentManager, "time_dialog")
    }

    //時刻をテキストビューにセット
    override fun onSelected(hourOfDay: Int, minute: Int) {
        timeText.text = "%1$02d時%2$02d分".format(hourOfDay, minute)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_edit)

        //Realmインスタンスの取得
        realm = Realm.getDefaultInstance()

        //インテントのgetLongExtraメソッドを使いインテントに格納した「schedule_id」の値を取得して、変数scheduleIdに格納する
        val scheduleId = intent?.getLongExtra("schedule_id", -1L)
        //取得出来なかった時にscheduleIdは「-1」となる為、「-1」の場合は新規登録、それ以外の場合は更新となる
        if (scheduleId != -1L) {
            //更新の場合Realmのインスタンスを生成した後、idフィールドがscheduleIdと同じレコードを取得して変数scheduleに格納する
            val schedule = realm.where<Schedule>()
                .equalTo("id", scheduleId).findFirst()
            //取得したデータを画面上の日付、タイトルの各ビューに表示する
            dateText.setText(DateFormat.format("yyyy'年'MM'月'dd'日、'(EEE)", schedule?.date))
            timeText.setText(DateFormat.format("kk'時'mm'分'", schedule?.time))
            titleEdit.setText(schedule?.title)
            //データの更新時のみ削除ボタンを表示
            delete.visibility = View.VISIBLE
        } else {
            delete.visibility = View.INVISIBLE
        }

 /*     ※保存ボタンを押した時の処理。
        ※SAMインターフェイスが入れ子になっているので、暗黙の引数itが指すものが分かりにくくなっている為
       　「view」と「db」という引数名を設定。
       （※）ラムダ式の引数が１つの場合は、引数を省略して暗黙の引数itが使える
       （※）SAM(Single Abstract Method)、一つだけ抽象メソッドを持つインターフェイス
        ※データベースの書き込み時はトランザクションを使わなければならない、トランザクションはデータベースへの
        　書き込みの一貫性、またスレッドセーフを保証する為に必要。
        ※executeTransactionメソッドを使っておけばトランザクションの開始、終了、キャンセルを自動で行ってくれる。
*/
        save.setOnClickListener { view: View ->
            val date_now = Date()
            val date_user: Date? = "${dateText.text} ${timeText.text}".toDate()
            if(date_user != null) {
                if (date_user.after(date_now)) {
                    when (scheduleId) {
                        -1L -> {
                            realm.executeTransaction { db: Realm ->
                                val maxId = db.where<Schedule>().max("id")
                                val nextId = (maxId?.toLong() ?: 0L) + 1

                                //RealmインスタンスのcreateObjectメソッドを使いデータを１行追加、この時１増やした後
                                // ＩＤを持つScheduleクラスのインスタンスを受け取るので各フィールドに値を設定するとデータの追加が完了する
                                val schedule = db.createObject<Schedule>(nextId)

                                //上の行で作成したScheduleオブジェクトに値を設定、これによりScheduleオブジェクトがデータベースに書き込まれる
                                //最初の行の日付を設定する処理は、toDateという拡張関数（下の方で定義してある）を定義してdateTextに入力された日付を表す文字列をDate型の値に変換してから設定
                                val date = dateText.text.toString().toDate("yyyy'年'MM'月'dd'日、'(EEE)")
                                val time = timeText.text.toString().toDate("kk'時'mm'分'")
                                if (date != null) schedule.date = date
                                if (time != null) schedule.time = time
                                schedule.notice_switch = "YET"
                                schedule.dateTime = "${dateText.text} ${timeText.text}".toDate()
                                schedule.title = titleEdit.text.toString()
                                var triggerTime = Calendar.getInstance()
                                triggerTime.time = schedule.dateTime
                                var scheduleId_int: Int = schedule.id.toInt()
                                var mAlarm = mAlarmManager()
                                mAlarm.setAlarm(applicationContext, triggerTime, scheduleId_int)

                            }

                            finish()

                        }
                        else -> {
                            realm.executeTransaction { db: Realm ->
                                val schedule = db.where<Schedule>()
                                    .equalTo("id", scheduleId).findFirst()
                                val date = dateText.text.toString()
                                    .toDate("yyyy'年'MM'月'dd'日、'(EEE)")
                                val time = timeText.text.toString()
                                    .toDate("kk'時'mm'分'")
                                if (date != null) schedule?.date = date
                                if (time != null) schedule?.time = time
                                schedule?.notice_switch = "YET"
                                schedule?.dateTime = "${dateText.text} ${timeText.text}".toDate()
                                schedule?.title = titleEdit.text.toString()
                                var triggerTime = Calendar.getInstance()
                                triggerTime.time = schedule?.dateTime
                                var scheduleId_int: Int? = schedule?.id?.toInt()
                                //スマートキャストでInt?をIntとして扱える（Int?とIntは別物である）
                                if (scheduleId_int is Int) {
                                    var mAlarm = mAlarmManager()
                                    mAlarm.setAlarm(applicationContext, triggerTime, scheduleId_int)
                                }
                            }

                            finish()

                        }
                    }
                } else { Toast.makeText(this, R.string.old_time_msg, Toast.LENGTH_SHORT).show() }
            } else { Toast.makeText(this, R.string.caution_msg, Toast.LENGTH_SHORT).show() }
        }

        //スケジュールを削除する処理
        delete.setOnClickListener { view: View ->
            realm.executeTransaction { db: Realm ->
                db.where<Schedule>().equalTo("id", scheduleId)
                    ?.findFirst()
                    ?.deleteFromRealm()

                var scheduleId_int: Int? = scheduleId?.toInt()
                //スマートキャストでInt?をIntとして扱える（Int?とIntは別物である）
                if (scheduleId_int is Int) {
                    var mAlarm = mAlarmManager()
                    mAlarm.cancelAlarm(applicationContext, scheduleId_int)
                }
            }

            finish()

        }

        //日付選択ボタン
        dateTime.setOnClickListener {
            val dialog = DatePickerFragment()
            dialog.show(supportFragmentManager, "date_dialog")
        }

        //編集ボタン
        edit.setOnClickListener {
            //エディットテキストにフォーカスを当てる
            val editText: EditText = findViewById(R.id.titleEdit)
            editText.requestFocus()
            //すでに入力がある場合、最後の文字の後ろにカーソルを置く
            editText.setSelection(editText.text.length)
            //キーボードの表示
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }

    //データベースを閉じる処理
    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    //拡張関数
    private fun String.toDate(pattern: String = "yyyy'年'MM'月'dd'日、'(EEE) kk'時'mm'分'"): Date? {
        return try {
            SimpleDateFormat(pattern).parse(this)
        } catch (e: IllegalArgumentException) {
            return null
        } catch (e: ParseException) {
            return null
        }
    }
}
