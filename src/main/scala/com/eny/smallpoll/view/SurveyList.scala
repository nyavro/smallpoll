package com.eny.smallpoll.view

import android.app.ListActivity
import android.content.{Context, Intent}
import android.os.Bundle
import android.view.View
import android.widget.{ListView, ArrayAdapter}
import com.eny.smallpoll.R
import com.eny.smallpoll.model.Survey
import com.eny.smallpoll.repository.{SurveyRepository, SmallpollDatabase, SurveyRepositoryImpl}

class SurveyList extends ListActivity {

   var repository:SurveyRepository = _

  override def onCreate(bundle:Bundle) = {
    super.onCreate(bundle)
    val db = new SmallpollDatabase(this.getApplicationContext)
    repository = new SurveyRepositoryImpl(db.getWritableDatabase)
    //    repository.names.map(item => Log.w("NAMES", item))
    setContentView(R.layout.main)


    // use the SimpleCursorAdapter to show the
    // elements in a ListView

    val adapter: ArrayAdapter[Survey] = new ArrayAdapter[Survey](
      this,
      android.R.layout.simple_list_item_1,
      repository.list().map(survey => survey).toArray
    )
    setListAdapter(adapter)
  }

  override def onListItemClick(listview:ListView, view:View, position:Int, id:Long) = {
    val item = listview.getAdapter.getItem(position).asInstanceOf[Survey]
    val intent = new Intent(SurveyList.this, classOf[SurveyView])
    intent.putExtra("id", item)
    startActivity(intent)
  }

  class CustomArrayAdapter(context:Context, resource:Int, values:Array[Survey]) extends ArrayAdapter[Survey](context, resource, values) {

  }
}