package jp.trial.yu_ma.my_reminder

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.kotlin.where

class SwipeToDeleteCallback(context: Context) : ItemTouchHelper.SimpleCallback(0, (ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT)) {

    val context = context.applicationContext
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    //今はドラッグ＆ドロップは使わない
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    //スワイプで削除
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        var realm: Realm = Realm.getDefaultInstance()
        var scheduleId = viewHolder.itemId
        //println("スワイプされたID：" + scheduleId)  //状態確認用
        realm.executeTransaction { db: Realm ->
            db.where<Schedule>().equalTo("id", scheduleId)
                ?.findFirst()
                ?.deleteFromRealm()
        }

        //アラームのキャンセル
        var scheduleId_int: Int? = scheduleId?.toInt()
        //スマートキャストでInt?をIntとして扱える（Int?とIntは別物である）
        if (scheduleId_int is Int) {

            var mAlarm = mAlarmManager()
            mAlarm.cancelAlarm(context, scheduleId_int)
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView

        val isCanceled = dX == 0f && !isCurrentlyActive
        if (isCanceled) {
            clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        //スワイプした時の背景色とその後の動作を想起させる為の処理
        val background = ColorDrawable()
        background.color = Color.RED
        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 70f
        val isLeftDirection = dX < 0
        if (isLeftDirection) {
            background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        } else {
            background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
        }
        background.draw(c)
        c.drawText("削除",itemView.right + dX + 50, itemView.bottom.toFloat() - 60, paint)
        c.drawText("削除",itemView.left + dX - 180, itemView.bottom.toFloat() - 60, paint)

    }

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }

}
