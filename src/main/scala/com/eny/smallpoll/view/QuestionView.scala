package com.eny.smallpoll.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.{AdapterView, ArrayAdapter}
import com.eny.smallpoll.R
import com.eny.smallpoll.model.Answer
import com.eny.smallpoll.repository.AnswerRepository
import org.scaloid.common._

class QuestionView extends SActivity with Db {

  lazy val text = new SEditText
  lazy val answers = new SListView
  lazy val multi = new SCheckBox
  lazy val add = new SButton
  lazy val repository = new AnswerRepository(instance.getWritableDatabase)
  var id:Long = -1
  onCreate {
    text.setText(getIntent.getStringExtra("text"))
    id = getIntent.getLongExtra("id", -1)
    multi.setChecked(getIntent.getBooleanExtra("multi", false))
    multi.setText(R.string.multi_select)
    answers.onItemClick {
      (adapterView: AdapterView[_], view: View, position: Int, id: Long) =>
        edit(adapterView.getItemAtPosition(position).asInstanceOf[Answer])
    }
    answers.onItemLongClick {
      (adapterView:AdapterView[_], view:View, position:Int, id:Long) =>
        Alert(R.string.remove_survey).run(() => remove(adapterView.getItemAtPosition(position).asInstanceOf[Answer]))
        true
    }
    add.setText(R.string.add)
    add.onClick {
      edit(Answer(None, "", answers.getAdapter.getCount, id))
    }
    contentView(new SVerticalLayout += new STextView(R.string.question_text) += text += new STextView(R.string.question_answers) += answers += multi += add)
  }
  def edit(answer:Answer): Unit = {
    val intent = new Intent(QuestionView.this, classOf[AnswerView])
    intent.putExtra("id", answer.id.getOrElse(-1L))
    intent.putExtra("text", answer.text)
    intent.putExtra("questionId", answer.questionId)
    intent.putExtra("indx", answer.index)
    startActivity(intent)
  }
  def remove(answer:Answer): Unit = {
    repository.remove(answer.id.getOrElse(-1L))
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
  override def onResume() {
    super.onResume()
    update()
  }
  override def onSaveInstanceState(bundle:Bundle) = {
    super.onSaveInstanceState(bundle)
    bundle.putLong("id", id)
  }
  override def onRestoreInstanceState(bundle:Bundle) = {
    id = bundle.getLong("id")
  }
}
