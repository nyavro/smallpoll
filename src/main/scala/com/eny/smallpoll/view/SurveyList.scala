package com.eny.smallpoll.view
import org.scaloid.common._
import android.app.ListActivity
import android.content.{Context, Intent}
import android.os.Bundle
import android.view.View
import android.widget.{AdapterView, ListView, ArrayAdapter}
import com.eny.smallpoll.R
import com.eny.smallpoll.model.Survey
import com.eny.smallpoll.repository.{SurveyRepository, SmallpollDatabase, SurveyRepositoryImpl}

class SurveyList extends SActivity {

  lazy val list = new SListView()
   var repository:SurveyRepository = _

  def onClick (adapterView:AdapterView[_], view:View, position:Int, id:Long):Unit = {
    val intent = new Intent(SurveyList.this, classOf[SurveyView])
    val survey: Survey = adapterView.getItemAtPosition(position).asInstanceOf[Survey]
    intent.putExtra("survey", survey)
    startActivity(intent)
  }

  onCreate {
    val db = new SmallpollDatabase(this.getApplicationContext)
    repository = new SurveyRepositoryImpl(db.getWritableDatabase)
    //    repository.names.map(item => Log.w("NAMES", item))
//    setContentView(R.layout.main)


    // use the SimpleCursorAdapter to show the
    // elements in a ListView
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
        intent.putExtra("survey", survey)
        startActivity(intent)
    }
    setContentView(new SVerticalLayout += list)
//    setListAdapter(adapter)
  }
//
//  override def onListItemClick(listview:ListView, view:View, position:Int, id:Long) = {
//    val item = listview.getAdapter.getItem(position).asInstanceOf[Survey]
//    val intent = new Intent(SurveyList.this, classOf[SurveyView])
//    intent.putExtra("id", item)
//    startActivity(intent)
//  }
//
//  class CustomArrayAdapter(context:Context, resource:Int, values:Array[Survey]) extends ArrayAdapter[Survey](context, resource, values) {
//
//  }
}