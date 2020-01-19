package jp.trial.yu_ma.my_reminder

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Schedule : RealmObject() { //RealmObjectを継承、モデルクラスは継承出来る様にopen修飾子を付ける必要がある
    @PrimaryKey //idが一意である必要がある為、@PrimaryKeyアノテーションを付加
    var id: Long = 0
    var date: Date = Date()
    var time: Date = Date()
    var dateTime: Date? = Date()
    var title: String = ""
    var notice_switch: String = "YET"
}