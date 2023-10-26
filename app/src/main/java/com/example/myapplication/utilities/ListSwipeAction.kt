package com.example.myapplication.utilities

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Canvas
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.TaskModel
import com.example.myapplication.systemReceiver.AlarmReceiver
import com.example.myapplication.ui.adaptors.TaskAdapter
import com.example.myapplication.viewModel.TasksViewModel
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.*


abstract class ListSwipeAction() {

    companion object : DatePickerDialog.OnDateSetListener {
        private var alertDialog: AlertDialog.Builder? = null
        private lateinit var alarmManager: AlarmManager
        private var hint: String? = null


        private var toDay = Calendar.getInstance()

        // private var myContext: Context? = null
        private var myContext: WeakReference<Context>? = null
        private var myModelProvider: TasksViewModel? = null

        private var simpleCallBack: ItemTouchHelper.SimpleCallback =
            object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    if (direction == ItemTouchHelper.LEFT) {
                        val holder = viewHolder as TaskAdapter.TaskHolder
                        // holder.setAlarm()
                        if (holder.layoutitem().LastDate.before(toDay)) {
                            Toast.makeText(
                                myContext?.get(),
                                "cant create alarm  for passed Tasks",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            testAddAlarm(holder.layoutitem())

                        }
                        holder.bindingAdapter?.notifyDataSetChanged()

                    } else {
                        val holder = viewHolder as TaskAdapter.TaskHolder
                        testHandleDeleteTask(holder.layoutitem())
                        // holder.deleteTask()
                        holder.bindingAdapter?.notifyDataSetChanged()


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
                    RecyclerViewSwipeDecorator.Builder(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                        .addSwipeRightActionIcon(R.drawable.ic_baseline_delete_24)
                        .addSwipeRightLabel("Delete")
                        .addSwipeRightBackgroundColor(Color.Red.toArgb())
                        .addSwipeLeftActionIcon(R.drawable.ic_baseline_alarm_24)
                        .addSwipeLeftBackgroundColor(Color.Magenta.toArgb())
                        .addSwipeLeftLabel("Alarm")

                        .create()
                        .decorate()
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )

                }

            }

        var itemTouchHelper: ItemTouchHelper? = null


        fun getItemTouchHelper(context: Context, modelProvider: TasksViewModel): ItemTouchHelper {
            if (itemTouchHelper == null) {
                itemTouchHelper = ItemTouchHelper(simpleCallBack)
            }
            myModelProvider = modelProvider
            myContext = WeakReference(context)

            alertDialog = AlertDialog.Builder((myContext?.get()!!), R.style.MyAlertDialogStyle)

            alarmManager =
                myContext?.get()?.getSystemService(Context.ALARM_SERVICE) as AlarmManager // now

            return itemTouchHelper as ItemTouchHelper
        }


        fun testHandleDeleteTask(taskModel: TaskModel) {
            // just delete from current list and ROOM
            alertDialog?.apply {
                setTitle("Alert")
                setMessage("which to delete")
                setCancelable(true)
                setNeutralButton("Single Task") { _, _ ->
                    myModelProvider?.viewModelScope?.launch(Dispatchers.Main) {
                        myModelProvider?.deleteSingleHeader(taskModel!!.Header)
                    }
                }

                setNegativeButton("All List") { _, _ ->
                    myModelProvider?.viewModelScope?.launch(Dispatchers.Main) {
                        myModelProvider?.deleteAGroup(taskModel!!.GroupName)
                    }
                }
                show()
            }
        }

        fun testAddAlarm(taskModel: TaskModel) {

            hint = taskModel.Header

            var year = toDay.get(Calendar.YEAR)
            var month = toDay.get(Calendar.MONTH)
            var day = toDay.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(
                myContext?.get()!!,
                R.style.MyAlertDialogStyle,
                this,
                year,
                month,
                day
            ).apply {
                datePicker.maxDate = taskModel?.LastDate?.timeInMillis!!
                show()
            }
        }

        override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
            var alarmInfo: Calendar = Calendar.getInstance()
            alarmInfo.set(/* year = */ year, /* month = */ month, /* date = */ dayOfMonth)

            var materialTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Set Time")
                .build()
            try {
                var fragmentManager = (myContext?.get() as FragmentActivity).supportFragmentManager
                materialTimePicker.show(fragmentManager, "Test")

            } catch (e: ClassCastException) {
                Log.e("fragManagerException", "onDateSet:${e.localizedMessage}")
            }

            materialTimePicker.addOnPositiveButtonClickListener(View.OnClickListener {
                alarmInfo.apply {
                    set(Calendar.HOUR_OF_DAY, materialTimePicker.hour)
                    set(Calendar.MINUTE, materialTimePicker.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                showConfirmAlert(alarmInfo)

            })
            materialTimePicker.addOnCancelListener(DialogInterface.OnCancelListener {
                Toast.makeText(myContext?.get(), "Alarm Cancelled", Toast.LENGTH_SHORT).show()
            })
        }


        private fun showConfirmAlert(alarmInfo: Calendar) {

            var text = ""
            text = if (alarmInfo.get(Calendar.HOUR_OF_DAY) >= 12) {
                " At ${alarmInfo.get(Calendar.HOUR)} : ${alarmInfo.get(Calendar.MINUTE)} PM"
            } else " At ${alarmInfo.get(Calendar.HOUR)} : ${alarmInfo.get(Calendar.MINUTE)} AM"

            var atd =
                "${alarmInfo.get(Calendar.DAY_OF_MONTH)}/ ${alarmInfo.get(Calendar.MONTH) + 1} /${
                    alarmInfo.get(Calendar.YEAR)
                }\n"
            when (alarmInfo.get(Calendar.DAY_OF_WEEK)) {
                1 -> atd = ("Sun $atd")
                2 -> atd = ("Mon $atd")
                3 -> atd = ("Tue $atd")
                4 -> atd = ("Wen $atd")
                5 -> atd = ("Thr $atd")
                6 -> atd = ("Fri $atd")
                7 -> atd = ("Sat $atd")
            }

            alertDialog?.apply {
                setTitle("Confirm Create Alarm")
                setMessage(" $atd $text")
                setCancelable(true)
                setNeutralButton("confirm") { _, _ ->
                    testCreateAlarm(alarmInfo)
                }
                setOnCancelListener(DialogInterface.OnCancelListener {
                    Toast.makeText(myContext?.get(), "Alarm Cancelled", Toast.LENGTH_SHORT).show()

                })

                show()
            }

        }

        private fun testCreateAlarm(alarmInfo: Calendar) {
            var intent = Intent(myContext?.get(), AlarmReceiver::class.java)

            intent.putExtra("Hint", hint)
            var pendingIntent =
                PendingIntent.getBroadcast(
                    myContext?.get(),
                    UUID.randomUUID().hashCode(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )

            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                alarmInfo.timeInMillis,
                pendingIntent
            )

//        alarmManager.setInexactRepeating(
//            AlarmManager.RTC_WAKEUP,
//            alarmInfo.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent,
//        )
        }


    }


}