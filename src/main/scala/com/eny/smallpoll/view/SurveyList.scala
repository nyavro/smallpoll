package com.eny.smallpoll.view

import android.app.ListActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.eny.smallpoll.repository.{SmallpollDatabase, SurveyRepositoryImpl}
import samples.employeedirectory.R

class SurveyList extends ListActivity {

  override def onCreate(bundle:Bundle) {
    super.onCreate(bundle)
    val db = new SmallpollDatabase(this.getApplicationContext)
    val repository = new SurveyRepositoryImpl(db.getWritableDatabase)
//    repository.names.map(item => Log.w("NAMES", item))
    setContentView(R.layout.main)


    // use the SimpleCursorAdapter to show the
    // elements in a ListView

    val adapter: ArrayAdapter[String] = new ArrayAdapter[String](this, android.R.layout.simple_list_item_1, repository.names().toArray)
    setListAdapter(adapter)
  }
}