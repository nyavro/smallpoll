package com.eny.smallpoll.view

import android.content.Intent
import android.os.Bundle
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
  lazy val start = new SButton
  lazy val repository = new QuestionRepository(instance.getWritableDatabase)
  var surveyId = -1L
  def edit(question:Question) = {
    val intent = new Intent(SurveyView.this, classOf[QuestionView])
    intent.putExtra("id", question.id.getOrElse(-1L))
    intent.putExtra("text", question.text)
    intent.putExtra("multi", question.multi)
    intent.putExtra("survey", question.surveyId)
    startActivity(intent)
  }
  onCreate {
    name.setText(getIntent.getStringExtra("name"))
    name.setActivated(false)
    surveyId = getIntent.getLongExtra("id", -1)
    questions.onItemClick {
      (adapterView: AdapterView[_], view: View, position: Int, id: Long) =>
        edit(adapterView.getItemAtPosition(position).asInstanceOf[Question])
    }
    questions.onItemLongClick {
      (adapterView:AdapterView[_], view:View, position:Int, id:Long) =>
        Alert(R.string.remove_question).run(() => remove(adapterView.getItemAtPosition(position).asInstanceOf[Question]))
        true
    }
    add.setText(R.string.add)
    add.onClick {
      edit(Question(None, "", multi = false, surveyId))
    }
    start.setText(R.string.start)
    start.onClick {
      val intent = new Intent(SurveyView.this, classOf[SurveyRunView])
      intent.putExtra("surveyId", surveyId)
      startActivity(intent)
    }
    contentView(
      new SVerticalLayout
        += new STextView(R.string.survey_name)
        += name
        += new STextView(R.string.survey_questions)
        += questions
        += add
        += start
    )
  }
  def update() = {
    questions.setAdapter(
      new ArrayAdapter[Question](
        this,
        android.R.layout.simple_list_item_1,
        repository.list(surveyId).toArray
      )
    )
  }
  def remove(question:Question) = {
    repository.remove(question.id.getOrElse(-1))
    update()
  }
  override def onResume() {
    super.onResume()
    update()
  }
  override def onSaveInstanceState(bundle:Bundle) = {
    super.onSaveInstanceState(bundle)
    bundle.putLong("surveyId", surveyId)
  }
  override def onRestoreInstanceState(bundle:Bundle) = {
    surveyId = bundle.getLong("surveyId")
  }
}
