package com.eny.smallpoll.view

import android.content.Intent
import android.view.ContextMenu.ContextMenuInfo
import android.view.{ContextMenu, MenuItem, View}
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.{AdapterView, ArrayAdapter}
import com.eny.smallpoll.R
import com.eny.smallpoll.model.Survey
import com.eny.smallpoll.repository.SurveyRepository
import org.scaloid.common._

class SurveyListView extends SActivity with Db {

  lazy val list = new SListView()
  lazy val add = new SButton()
  lazy val repository = new SurveyRepository(instance.getWritableDatabase)

  onCreate {
    list.onItemClick {
      (adapterView: AdapterView[_], view: View, position: Int, id: Long) =>
        edit(adapterView.getItemAtPosition(position).asInstanceOf[Survey])
    }
    add.setText(R.string.add)
    add.onClick {
      edit(Survey(None, ""))
    }
    setContentView(new SVerticalLayout += list += add)
    registerForContextMenu(list)
  }
  def edit(survey: Survey) = {
    val intent = new Intent(SurveyListView.this, classOf[SurveyView])
    intent.putExtra("surveyId", survey.id.getOrElse(-1L))
    intent.putExtra("name", survey.name)
    startActivity(intent)
  }
  def update() = {
    list.setAdapter(
      new ArrayAdapter[Survey](
        this,
        android.R.layout.simple_list_item_1,
        repository.list().toArray
      )
    )
  }
  def remove(survey:Survey) = {
    repository.remove(survey.id.getOrElse(-1))
    update()
  }
  def run(survey:Survey) = {
    val intent = new Intent(SurveyListView.this, classOf[SurveyRunView])
    intent.putExtra("surveyId", survey.id.getOrElse(-1L))
    startActivity(intent)
  }
  override def onResume() = {
    super.onResume()
    update()
  }
  override def onCreateContextMenu(menu:ContextMenu, view:View, info:ContextMenuInfo) = {
    super.onCreateContextMenu(menu, view, info)
    getMenuInflater.inflate(R.menu.survey_context, menu)
  }
  override def onContextItemSelected(item:MenuItem) = {
    val info = item.getMenuInfo.asInstanceOf[AdapterContextMenuInfo]
    val survey = list.getAdapter.getItem(info.position).asInstanceOf[Survey]
    item.getItemId match {
      case R.id.delete =>
        Alert(R.string.remove_survey).run(
          () => remove(survey)
        )
        true
      case R.id.run =>
        run(survey)
        true
      case _ =>
        super.onContextItemSelected(item)
    }
  }
}