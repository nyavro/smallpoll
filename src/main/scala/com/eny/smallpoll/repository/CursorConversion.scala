package com.eny.smallpoll.repository

import android.database.Cursor

/**
 * Created by eny on 27.04.15.
 */
trait CursorConversion {

  def toList[A](cursor:Cursor, convert:Cursor => A): List[A] =
    if(cursor.isAfterLast) Nil
    else {
      val item = convert(cursor)
      cursor.moveToNext
      item :: toList(cursor, convert)
    }
}
