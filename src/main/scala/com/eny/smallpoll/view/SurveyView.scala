package com.eny.smallpoll.view

import android.app.AlertDialog.Builder
import android.content.DialogInterface.OnClickListener
import android.content.{DialogInterface, Intent}
import android.view.View
import android.widget.{AdapterView, ArrayAdapter}
import com.eny.smallpoll.R
import com.eny.smallpoll.model.Question
import com.eny.smallpoll.repository.QuestionRepository
import org.scaloid.common._

class SurveyView extends SActivity with Db {

  lazy val name = new SEditText
  lazy val questions = new SListView
  lazy val add = new SButton
  lazy val repository = new QuestionRepository(instance.getWritableDatabase)

  onCreate {
    name.setText(getIntent.getStringExtra("name"))
    val id = getIntent.getLongExtra("id", -1)
    questions.setAdapter(
      new ArrayAdapter[Question](
        this,
        android.R.layout.simple_list_item_1,
        repository.list(id).toArray
      )
    )
    questions.onItemClick {
      (adapterView: AdapterView[_], view: View, position: Int, id: Long) =>
        val intent = new Intent(SurveyView.this, classOf[QuestionView])
        val question: Question = adapterView.getItemAtPosition(position).asInstanceOf[Question]
        intent.putExtra("id", question.id.getOrElse(-1L))
        intent.putExtra("text", question.text)
        startActivity(intent)
    }
    questions.onItemLongClick {
      (adapterView:AdapterView[_], view:View, position:Int, id:Long) =>
        new Builder(SurveyView.this)
          .setIcon(android.R.attr.alertDialogIcon)
          .setTitle(R.string.remove_question)
          .setPositiveButton(
            R.string.dialog_ok,
            new OnClickListener() {
              override def onClick(dialog: DialogInterface, whichButton: Int) = {
                val question = adapterView.getItemAtPosition(position).asInstanceOf[Question]
                repository.remove(question.id.getOrElse(-1L))
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
          .create
          .show()
        true
    }
    add.setText(R.string.add)
    add.onClick {
      val intent = new Intent(SurveyView.this, classOf[QuestionView])
      intent.putExtra("id", -1L)
      intent.putExtra("txt", "")
      startActivity(intent)
    }
    contentView(new SVerticalLayout += name += questions += add)
  }
}
