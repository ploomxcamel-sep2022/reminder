package jp.trial.yu_ma.my_reminder

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.kotlin.where
import java.text.ParseException
import java.text.SimpleDateFormat

import java.util.*


/*
* OrderedRealmCollection<Schedule>はRecyclerViewに表示するデータの一覧（データセット）で、autoUpdateをtrueに設定するとデータベースが更新されるとRecyclerViewの表示も自動更新される
* RealmRecyclerViewAdapterは抽象クラスで総称型を２つ取る
* 最初の総称型はRealmModelインターフェイスでRealmModelはRealmObjectのインターフェイス、ここにはRecyclerViewに表示したい項目を指定する
*２番目はRecyclerView.ViewHolderクラス、ViewHolderはアダプター内部に用意するクラスで、セルに表示するビューを保持する為のもの
* */

class ScheduleAdapter(data: OrderedRealmCollection<Schedule>) : RealmRecyclerViewAdapter<Schedule, ScheduleAdapter.ViewHolder>(data, true) {

    //引数がLong?型で戻り値がない関数型の変数listenerを宣言
    //関数の定義では戻り値が無いことを意味するUnitは省略できるが、関数型の宣言では省略できない
    //変数listenerは後から値を設定したいのでNull許容型として定義
    //関数型の定義全体を丸括弧で囲み、Null許容型を示す疑問符（?）を丸括弧の後に置く
    private var listener: ((Long?)-> Unit)? = null

    //関数型の変数、listenerに登録を行う為のメソッド
    //このメソッドは引数として関数型を受け取る、つまり関数が別の関数を受け取る
    fun setOnItemClickListener(listener:(Long?)-> Unit) {
        this.listener = listener
    }

    init {
        //RecyclerViewを高速に描画する為の設定（更新時の画面のチラつきの軽減）
        setHasStableIds(true)

    }

    /*ViewHolderクラスはRecyclerView.ViewHolderを継承したクラスで、セルに使用するビューを保持する
    *セルとは表示したい項目をグループ化した単位で、１つのスケジュールの日付とタイトルをセットにしたもの
    **/
    class ViewHolder(cell: View) : RecyclerView.ViewHolder(cell) {
        val title: TextView = cell.findViewById(android.R.id.text1)
        val dateTime: TextView = cell.findViewById(android.R.id.text2)

    }

    override fun getItemViewType(position: Int): Int {

        //スケージュールの時間によってViewTypeを設定
        var sarch_id = getItem(position)?.id
        val realm: Realm = Realm.getDefaultInstance()
        var schedulus = realm.where<Schedule>().findAll()
        val date_now = Date()
        var schedulu_data = schedulus.where().equalTo("id", sarch_id).findFirst()
        when(schedulu_data?.dateTime!!.compareTo(date_now)){
            -1 -> { //過去の日時
                return -1
            }
            0,1 -> { //現在以降
                //現在の１日後の日付を作成
                val sdf = SimpleDateFormat("yyyy-MM-dd")
                var cal1 = Calendar.getInstance()
                var str_day1: String = sdf.format(date_now)
                var dt_next: Date?
                dt_next = str_day1.toDate()
                cal1.time = dt_next
                cal1.add(Calendar.DAY_OF_MONTH, 1)
                dt_next = cal1.time
                //セルのセット時間を年月日だけに変える
                var str_day2: String = sdf.format(schedulu_data.dateTime)
                var dt_setTime: Date? = str_day2.toDate()
                if(dt_setTime!!.before(dt_next)){
                    return 0
                } else {
                    return 1
                }
            }
        }

        return super.getItemViewType(2)
    }

    /*
    *onCreateViewHolderはセルが必要になる度に呼び出され、内部でViewHolderのインスタンスを生成して返すように実装する
    * */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(android.R.layout.simple_list_item_2, parent, false)
        //ViewTypeでセルの色を分ける
        when(viewType){
            -1 -> { //過去の予定
                view.setBackgroundResource(R.color.colorGray)
            }
            0 -> {
                //何もしない（本日の予定）
            }
            1 -> { //明日以降の予定
                view.setBackgroundResource(R.color.colorOrange)
            }
            2 -> {
                view.setBackgroundResource(R.color.colorError)
            }
        }

        return ViewHolder(view)

    }

    /*
    *データを取り出して表示するための処理
    * onBindViewHolderは指定した位置にデータを表示する必要がある時、RecyclerViewによって呼び出される
    * このメソッドではViewHolderで保持しているビューに対して、実際に表示する画像や文字などのコンテンツの設定を行う
    * */
    override fun onBindViewHolder(holder: ScheduleAdapter.ViewHolder, position: Int) {
        val schedule: Schedule? = getItem(position)

        //テキストビューに値をセット
        //日付はDate型で保持しているのでテキストビューにセットする前に文字列に変換
        holder.dateTime.text = android.text.format.DateFormat.format("yyyy'年'MM'月'dd'日、'(EEE) kk'時'mm'分'", schedule?.dateTime)
        holder.title.text = schedule?.title
        holder.title.setTextSize(24.0f)


        //セルに使用しているビューがタップされた時のイベント
        //invokeはKotlin特有の機能で、関数型の変数を実行する為の特殊なメソッド
        holder.itemView.setOnClickListener {
            listener?.invoke(schedule?.id)
        }
    }

    /*
    * データ更新時に画面がチラつくのを軽減する為の実装
    * */
    override fun getItemId(position: Int): Long {
        return getItem(position)?.id ?: 0
    }
}

private fun String.toDate(pattern: String = "yyyy-MM-dd"): Date? {
    return try {
        SimpleDateFormat(pattern).parse(this)
    } catch (e: IllegalArgumentException) {
        return null
    } catch (e: ParseException) {
        return null
    }
}