package com.eny.smallpoll.view

import android.app.AlertDialog.Builder
import android.content.DialogInterface.OnClickListener
import android.content.{DialogInterface, Intent}
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.{AdapterView, ArrayAdapter}
import com.eny.smallpoll.R
import com.eny.smallpoll.model.{Question, Answer}
import com.eny.smallpoll.repository.AnswerRepository
import org.scaloid.common._

class QuestionView extends SActivity with Db {

  lazy val text = new SEditText
  lazy val answers = new SListView
  lazy val add = new SButton
  lazy val repository = new AnswerRepository(instance.getWritableDatabase)
  var id:Long = -1

  onCreate {
    text.setText(getIntent.getStringExtra("text"))
    id = getIntent.getLongExtra("id", -1)
    Log.d("smallpoll", s"Answer long clicked")
    answers.onItemClick {
      (adapterView: AdapterView[_], view: View, position: Int, id: Long) =>
        val intent = new Intent(QuestionView.this, classOf[AnswerView])
        val answer = adapterView.getItemAtPosition(position).asInstanceOf[Answer]
        intent.putExtra("id", answer.id.getOrElse(-1L))
        intent.putExtra("text", answer.text)
        intent.putExtra("questionId", answer.questionId)
        intent.putExtra("indx", position)
        startActivity(intent)
    }
    answers.onItemLongClick {
      (adapterView:AdapterView[_], view:View, position:Int, id:Long) =>
        new Builder(QuestionView.this)
          .setIcon(android.R.drawable.alert_light_frame)
          .setTitle(R.string.remove_survey)
          .setPositiveButton(
            R.string.dialog_ok,
            new OnClickListener() {
              override def onClick(dialog: DialogInterface, whichButton: Int) = {
                val answer = adapterView.getItemAtPosition(position).asInstanceOf[Answer]
                repository.remove(answer.id.getOrElse(-1L))
                update()
              }
            }
          )
          .setNegativeButton(
            R.string.dialog_cancel,
            new OnClickListener {
              override def onClick(dialog: DialogInterface, whichButton: Int) = {}
            }
          )
          .create()
          .show()
        true
    }
    add.setText(R.string.add)
    add.onClick {
      val intent = new Intent(QuestionView.this, classOf[AnswerView])
      intent.putExtra("id", -1L)
      intent.putExtra("text", "")
      intent.putExtra("questionId", id)
      intent.putExtra("indx", answers.getAdapter.getCount)
      startActivity(intent)
    }
    contentView(new SVerticalLayout += text += answers += add)
  }
  override def onResume() {
    super.onResume()
    update()
  }
  def update() = {
    Log.d("smallpoll", s"Updating question $id")
    answers.setAdapter(
      new ArrayAdapter[Answer](
        this,
        android.R.layout.simple_list_item_1,
        repository.list(id).toArray
      )
    )
  }
  override def onSaveInstanceState(bundle:Bundle) = {
    super.onSaveInstanceState(bundle)
    bundle.putLong("id", id)
  }
  override def onRestoreInstanceState(bundle:Bundle) = {
    id = bundle.getLong("id")
  }
}
