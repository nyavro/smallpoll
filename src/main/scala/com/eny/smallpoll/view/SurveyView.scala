package com.eny.smallpoll.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.{AdapterView, ArrayAdapter}
import com.eny.smallpoll.R
import com.eny.smallpoll.model.{Survey, Question}
import com.eny.smallpoll.repository.{SurveyRepository, QuestionRepository}
import org.scaloid.common._

class SurveyView extends SActivity with Db {

  lazy val nameEdit = new SEditText
  lazy val questions = new SListView
  lazy val add = new SButton
  lazy val saveBtn = new SButton
  lazy val questionRepository = new QuestionRepository(instance.getReadableDatabase)
  lazy val surveyRepository = new SurveyRepository(instance.getWritableDatabase)
  var surveyId = -1L
  var name = ""
  def edit(question:Question) = {
    val intent = new Intent(SurveyView.this, classOf[QuestionView])
    intent.putExtra("questionId", question.id.getOrElse(-1L))
    intent.putExtra("text", question.text)
    intent.putExtra("multi", question.multi)
    intent.putExtra("survey", question.surveyId)
    intent.putExtra("index", question.index)
    startActivity(intent)
  }
  def save() = {
    surveyId = surveyRepository.save(Survey(asOption(surveyId), nameEdit.text.toString))
  }
  onCreate {
    initParams()
    nameEdit.setActivated(false)
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
      save()
      edit(Question(None, "", multi = false, surveyId, questions.count))
    }
    saveBtn.setText(R.string.save)
    saveBtn.onClick {
      save()
      finish()
    }
    contentView(
      new SVerticalLayout
        += new STextView(R.string.survey_name)
        += nameEdit
        += new STextView(R.string.survey_questions)
        += questions
        += add
        += saveBtn
    )
  }
  def initParams() = {
    surveyId = getIntent.getLongExtra("surveyId", -1)
    name = getIntent.getStringExtra("name")
  }
  def update() = {
    nameEdit.setText(name)
    val questions1: List[Question] = questionRepository.list(surveyId)
    questions.setAdapter(
      new ArrayAdapter[Question](
        this,
        android.R.layout.simple_list_item_1,
        questions1.toArray
      )
    )
  }
  def remove(question:Question) = {
    questionRepository.remove(question.id.getOrElse(-1))
    update()
  }
  override def onResume() {
    super.onResume()
    update()
  }
  override def onSaveInstanceState(bundle:Bundle) = {
    super.onSaveInstanceState(bundle)
    name = nameEdit.text.toString
    bundle.putLong("surveyId", surveyId)
    bundle.putString("name", name)
  }
  override def onRestoreInstanceState(bundle:Bundle) = {
    super.onRestoreInstanceState(bundle)
    surveyId = bundle.getLong("surveyId")
    name = bundle.getString("name")
  }
}
