package com.example.myapplication.utilities

import android.app.DatePickerDialog
import android.content.Context

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.data.model.TaskModel
import com.example.myapplication.viewModel.TasksViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

   class TasksPressAction (modelProvider: TasksViewModel, taskModel: TaskModel, context: Context) {
    private  var mtaskModel: TaskModel?=null
     private var mcontext:Context?=null
     private var mmodelProvider: TasksViewModel?=null
      val toDay = Calendar.getInstance()
      private var alertDialog: AlertDialog.Builder


      init {

          this.mtaskModel=taskModel
           mmodelProvider=modelProvider
           this.mcontext=context
          alertDialog=  AlertDialog.Builder(mcontext!!, R.style.MyAlertDialogStyle)

      }

    fun testhandelpress() {
        if (mtaskModel?.Done == true){
            Toast.makeText(mcontext?.applicationContext,"Task IS Done ",Toast.LENGTH_SHORT).show()
        } else{
            alertDialog.apply {
                setTitle(mtaskModel?.Header)
                setMessage("Action")
                setCancelable(true)
                setNeutralButton("Early Achieved") { _, _ ->
                    Toast.makeText(context, "early achieved", Toast.LENGTH_SHORT).show()
                    testaddToAchieved(mtaskModel)
                }

                setNegativeButton("Post Dead Line") { _, _ ->
                    Toast.makeText(context, "post dead line", Toast.LENGTH_SHORT).show()
                    testpostDeadLine(mtaskModel, context)

                }



                show()
            }

        }

    }


     private fun testaddToAchieved(taskModel: TaskModel?) {
        taskModel?.Done = true

         mmodelProvider?.viewModelScope?.launch(Dispatchers.IO) {
            mmodelProvider?.setTaskDone(true, taskModel!!.Header)
            // mTasksViewModel.addNewTask(taskModel!!)
        }
    }


     private fun testpostDeadLine(taskModel: TaskModel?, context: Context) {
        val year = toDay.get(Calendar.YEAR)
        val month = toDay.get(Calendar.MONTH)
        val day = toDay.get(Calendar.DAY_OF_MONTH)
        val newDatePicker =
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                var remainTime = "$dayOfMonth/${month + 1}/$year"
                val tempCal = Calendar.getInstance()
                tempCal.clear(Calendar.HOUR_OF_DAY)
                tempCal.clear(Calendar.SECOND)
                tempCal.clear(Calendar.MILLISECOND)
                tempCal.clear(Calendar.MINUTE)
                tempCal.set(Calendar.YEAR, year)
                tempCal.set(Calendar.MONTH, month)
                tempCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                when (tempCal.get(Calendar.DAY_OF_WEEK)) {

                    1 -> remainTime = "Sun $remainTime"
                    2 -> remainTime = "Mon $remainTime"
                    3 -> remainTime = "Tue $remainTime"
                    4 -> remainTime = "Wen $remainTime"
                    5 -> remainTime = "Thr $remainTime"
                    6 -> remainTime = "Fri $remainTime"
                    7 -> remainTime = "Sat $remainTime"
                }
                if (tempCal.before(toDay)) {
                    // cant select passed date
                    Toast.makeText(context, "cant select passed date", Toast.LENGTH_SHORT).show()
                } else {
                    mmodelProvider?.viewModelScope?.launch(Dispatchers.IO) {
                        mmodelProvider?.updateTaskDeadLine(tempCal, remainTime, taskModel!!.Header)
                    }
                }
            }
        DatePickerDialog(context, newDatePicker, year, month, day).show()


    }






  }