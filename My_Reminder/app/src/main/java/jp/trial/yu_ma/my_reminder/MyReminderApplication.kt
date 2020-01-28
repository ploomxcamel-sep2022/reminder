package jp.trial.yu_ma.my_reminder

import android.app.Application
import io.realm.Realm

class MyReminderApplication : Application() {  //アプリケーションクラスの継承

    //アプリケーションクラスのonCreateメソッドはアプリ実行時にアクティビティより先に呼ばれる
    override fun onCreate() {
        super.onCreate()
        Realm.init(this) //Realmの初期化
    }
}