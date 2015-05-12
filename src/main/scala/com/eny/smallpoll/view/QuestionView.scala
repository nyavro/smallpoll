package com.eny.smallpoll.view

import android.app.AlertDialog.Builder
import android.content.{Intent, DialogInterface}
import android.content.DialogInterface.OnClickListener
import android.view.View
import android.widget.{AdapterView, ArrayAdapter}
import com.eny.smallpoll.R
import com.eny.smallpoll.model.{Survey, Answer}
import com.eny.smallpoll.repository.AnswerRepository
import org.scaloid.common._

class QuestionView extends SActivity with Db {

  lazy val text = new STextView("test")
  lazy val answers = new SListView()
  lazy val add = new SButton()
  lazy val repository = new AnswerRepository(instance.getWritableDatabase)

  onCreate {
    text.setText(getIntent.getStringExtra("text"))
    val id = getIntent.getLongExtra("id", -1)
    answers.setAdapter(
      new ArrayAdapter[Answer](
        this,
        android.R.layout.simple_list_item_1,
        repository.list(id).toArray
      )
    )
    answers.onItemLongClick {
      (adapterView:AdapterView[_], view:View, position:Int, id:Long) =>
        new Builder(QuestionView.this)
          .setIconAttribute(android.R.attr.alertDialogIcon)
          .setTitle(R.string.remove_survey)
          .setPositiveButton(
            R.string.dialog_ok,
            new OnClickListener() {
              override def onClick(dialog: DialogInterface, whichButton: Int) = {
                val answer = adapterView.getItemAtPosition(position).asInstanceOf[Answer]
                repository.remove(answer.id.getOrElse(-1L))
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
      val intent = new Intent(QuestionView.this, classOf[AnswerView])
      intent.putExtra("id", -1L)
      intent.putExtra("txt", "")
      startActivity(intent)
    }
    contentView(new SVerticalLayout += text += answers += add)
  }
}
