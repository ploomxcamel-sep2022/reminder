package jp.trial.yu_ma.my_reminder

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    //Realmクラスのプロパティ、onCreateで初期化するのでlateinit修飾子を付ける（遅延初期化）
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //action.BOOT_COMPLETEDの有効化
        val receiver = ComponentName(applicationContext, AlarmBroadcastReceiver::class.java)

        this.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        realm = Realm.getDefaultInstance() //Realmクラスのインスタンスを取得、データベース使用準備完了

        //LinearLayoutManagerのインスタンスを作成してRecyclerViewのレイアウトマネージャーとして登録
        list.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager?
        //一覧画面に区切り線を設定
        val separateLine = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        list.addItemDecoration(separateLine)

        //Realmインスタンスからデータを取得するクエリを発行、クエリの発行にはwhereメソッドの型引数でモデルの型を指定
        //その後、findAllメソッドですべてのスケジュールを取得し変数に格納
        //sortメソッドでデータベースの並べ替え
        var realmResult: RealmResults<Schedule> = realm.where(Schedule::class.java).findAll()
        realmResult = realmResult.sort("dateTime")

        //ScheduleAdapterクラスのインスタンスを生成してRecyclerViewに設定
        //ScheduleAdapterのコンストラクタには上の「val schedules = realm.where<Schedule>().findAll()」で取得したすべてのスケジュールを渡す
        val adapter = ScheduleAdapter(realmResult)
        list.adapter = adapter

        //インフォメーション
        var maxid = realmResult.where().max("id")
        val maxId: Int? = maxid?.toInt()
        var count_old = 0
        var count_after_today = 0
        var date_now = Date()
        if(maxId != null) {
            for(sarch_Id in 1..maxId) {
                var schedule_data = realmResult.where().equalTo("id", sarch_Id).findFirst()
                if(schedule_data?.dateTime != null) {
                    when(schedule_data.dateTime!!.compareTo(date_now)) {
                        -1 -> {
                            count_old += 1
                        }
                        0,1 -> {
                            count_after_today += 1
                        }
                    }
                } else { /*何もしない*/ }
            }
        } else { /*何もしない*/ }
        
        var info: Button = findViewById(R.id.info)
        //setTextで文字の連結するのは行儀の悪いコードと見なされるので変数に詰めてからinfo.textに渡す
        var str: String = "過去の予定 " + count_old.toString() + " 件\n" + "以後の予定 " + count_after_today.toString() + " 件"
        info.text = str

        fab.setOnClickListener { view ->
            val intent = Intent(this, ScheduleEditActivity::class.java)
            startActivity(intent)
        }

        //RecyclerViewの項目がタップされた時の処理をアダプターに用意したsetOnItemClickListenerメソッドに登録
        //Scheduleのidを受け取りインテントに「schedule_id」として格納することでidをスケジュール編集用アクティビティ、ScheduleEditActivityに渡す
        adapter.setOnItemClickListener { id ->
            val intent = Intent(this, ScheduleEditActivity::class.java)
                .putExtra("schedule_id", id)
            startActivity(intent)
        }

        //スワイプに対応
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(this))
        itemTouchHelper.attachToRecyclerView(this.list)

    }

    //ライフサイクルメソッドであるonDestroyメソッドをオーバーライドし、closeメソッドでRealmのインスタンスを破棄しリソースを開放
    override fun onDestroy() {
        super.onDestroy()
      realm.close()
        Log.d("ログ","main_close")
    }

/*
      //右上のメニュー関連（今は使用しない）
      override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
     }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
**/

}
