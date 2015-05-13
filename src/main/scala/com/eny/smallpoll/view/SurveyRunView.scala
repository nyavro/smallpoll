package com.eny.smallpoll.view

import android.widget.{AbsListView, ListView}
import com.eny.smallpoll.R
import com.eny.smallpoll.repository.{AnswerRepository, ResultRepository, QuestionRepository}
import com.eny.smallpoll.view.Db
import org.scaloid.common._

/**
 * Created by Nyavro on 13.05.15
 */
class SurveyRunView extends SActivity with Db {
  lazy val questionRepository = new QuestionRepository(instance.getReadableDatabase)
  lazy val answerRepository = new AnswerRepository(instance.getReadableDatabase)
  lazy val resultRepository = new ResultRepository(instance.getWritableDatabase)

  lazy val text = new STextView

  onCreate {
    val surveyId = getIntent.getLongExtra("surveyId", -1)
    val questionIds = questionRepository.list(surveyId).map(_.id.get).toArray
    if(questionIds.isEmpty) {

    } else {
      val question = questionRepository.load(questionIds(0))
      val answers = answerRepository.list(question.id.get)
      text.setText(question.text)
      if(question.multi) {
        val list = new SListView
        val next = new SButton
        next.setText(R.string.next)
        next.onClick {
          val ids = list.getCheckedItemPositions
          val k = ids.keyAt(0)
          val vl = ids.valueAt(0)
          ids.size

        }
        list.setAdapter(new SArrayAdapter(answers.toArray, android.R.layout.simple_list_item_multiple_choice))
        list.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE)
        contentView(
          new SVerticalLayout += text += list += next
        )
      } else {
        val list = new SListView
        list.setAdapter(new SArrayAdapter(answers.toArray))
        contentView(
          new SVerticalLayout += text += list
        )
      }
    }
  }
}
