package com.eny.smallpoll.repository

import android.content.Context
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.util.Log

/**
 * Created by eny on 26.04.15.
 */
class SmallpollDatabase(context:Context, name:String, version:Int) extends SQLiteOpenHelper(context, name, null, version) {

  def this(context:Context) = this(context, "smallpoll", 1)

  override def onCreate(db:SQLiteDatabase) = {
    db.execSQL("CREATE TABLE answer (id integer primary key, txt text not null, indx integer not null)")
    db.execSQL("CREATE TABLE question (id integer primary key, txt text not null, indx integer not null)")
    db.execSQL("CREATE TABLE question_answer (question_id integer not null, answer_id integer not null)")
    db.execSQL("CREATE TABLE survey (id integer not null primary key, name text not null)")
    db.execSQL("CREATE TABLE survey_question (survey_id integer not null, question_id integer not null)")
    db.execSQL("CREATE TABLE result (date integer not null, question_id integer not null, answer_id integer not null)")
    db.execSQL("INSERT INTO survey(id, name) VALUES(1, 'hello')")
    db.execSQL("INSERT INTO survey(id, name) VALUES(2, 'world')")
    db.execSQL("INSERT INTO survey(id, name) VALUES(3, '!')")
  }

  override def onUpgrade(db:SQLiteDatabase, oldVersion:Int, newVersion:Int) = {
    Log.w(this.getClass.getName, s"Upgrading database from version $oldVersion to $newVersion")
  }
}
