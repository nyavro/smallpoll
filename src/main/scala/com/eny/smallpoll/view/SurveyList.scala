package com.eny.smallpoll.view

import android.app.AlertDialog.Builder
import android.content.DialogInterface.OnClickListener
import android.content.{DialogInterface, Intent}
import android.view.View
import android.widget.{AdapterView, ArrayAdapter}
import com.eny.smallpoll.R
import com.eny.smallpoll.model.Survey
import com.eny.smallpoll.repository.SurveyRepository
import org.scaloid.common._

class SurveyList extends SActivity with Db {

  lazy val list = new SListView()
  lazy val add = new SButton()
  lazy val repository = new SurveyRepository(instance.getWritableDatabase)

  onCreate {
    list.setAdapter(
      new ArrayAdapter[Survey](
        this,
        android.R.layout.simple_list_item_1,
        repository.list().toArray
      )
    )
    list.onItemClick {
      (adapterView:AdapterView[_], view:View, position:Int, id:Long) =>
        val intent = new Intent(SurveyList.this, classOf[SurveyView])
        val survey: Survey = adapterView.getItemAtPosition(position).asInstanceOf[Survey]
        intent.putExtra("id", survey.id.getOrElse(-1L))
        intent.putExtra("name", survey.name)
        startActivity(intent)
    }
    list.onItemLongClick {
      (adapterView:AdapterView[_], view:View, position:Int, id:Long) =>
        new Builder(SurveyList.this)
          .setIconAttribute(android.R.attr.alertDialogIcon)
          .setTitle(R.string.remove_survey)
          .setPositiveButton(
            R.string.dialog_ok,
            new OnClickListener() {
              override def onClick(dialog: DialogInterface, whichButton: Int) = {
                val survey: Survey = adapterView.getItemAtPosition(position).asInstanceOf[Survey]
                repository.remove(survey.id.getOrElse(-1L))
                view.invalidate()
              }
            }
          )
          .setNegativeButton(
            R.string.dialog_cancel,
            new OnClickListener {
              override def onClick(dialog: DialogInterface, whichButton: Int) = {}
            }
          )
        true
    }
    add.setText(R.string.add)
    add.onClick {
      val intent = new Intent(SurveyList.this, classOf[SurveyView])
      intent.putExtra("id", -1L)
      intent.putExtra("name", "")
      startActivity(intent)
    }
    setContentView(new SVerticalLayout += list += add)
  }
}