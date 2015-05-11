package com.eny.smallpoll.view
import android.content.Intent
import android.view.View
import android.widget.{AdapterView, ArrayAdapter}
import com.eny.smallpoll.model.Survey
import com.eny.smallpoll.repository.{SmallpollDatabase, SurveyRepository, SurveyRepositoryImpl}
import org.scaloid.common.{SVerticalLayout, SListView, SActivity}

class SurveyList extends SActivity with Db {

  lazy val list = new SListView()
  lazy val repository = new SurveyRepositoryImpl(instance.getWritableDatabase)

  onCreate {
    val adapter: ArrayAdapter[Survey] = new ArrayAdapter[Survey](
    this,
        android.R.layout.simple_list_item_1,
        repository.list().map(survey => survey).toArray
    )
    list.setAdapter(adapter)
    list.onItemClick{
      (adapterView:AdapterView[_], view:View, position:Int, id:Long) =>
        val intent = new Intent(SurveyList.this, classOf[SurveyView])
        val survey: Survey = adapterView.getItemAtPosition(position).asInstanceOf[Survey]
        intent.putExtra("id", survey.id)
        intent.putExtra("name", survey.name)
        startActivity(intent)
    }
    setContentView(new SVerticalLayout += list)
  }
}