package com.eny.smallpoll.view

import android.app.ListActivity
import android.content.{Context, Intent}
import android.os.Bundle
import android.view.View
import android.widget.{ListView, ArrayAdapter}
import com.eny.smallpoll.R
import com.eny.smallpoll.model.Survey
import com.eny.smallpoll.repository.{SmallpollDatabase, SurveyRepositoryImpl}

class SurveyList extends ListActivity {

  val db = new SmallpollDatabase(this.getApplicationContext)
  val repository = new SurveyRepositoryImpl(db.getWritableDatabase)

  override def onCreate(bundle:Bundle) = {
    super.onCreate(bundle)
//    repository.names.map(item => Log.w("NAMES", item))
    setContentView(R.layout.main)


    // use the SimpleCursorAdapter to show the
    // elements in a ListView

    val adapter: ArrayAdapter[String] = new ArrayAdapter[String](
      this,
      android.R.layout.simple_list_item_1,
      repository.list().map(survey => survey.name).toArray
    )
    setListAdapter(adapter)
  }

  override def onListItemClick(listview:ListView, view:View, position:Int, id:Long) = {
    val item = listview.getAdapter.getItem(position).asInstanceOf[Survey]
    val intent = new Intent(SurveyList.this, Class[SurveyView])
//    intent.putExtra("id", )
  }

  class CustomArrayAdapter(context:Context, resource:Int, values:Array[Survey]) extends ArrayAdapter[Survey](context, resource, values) {

  }
}