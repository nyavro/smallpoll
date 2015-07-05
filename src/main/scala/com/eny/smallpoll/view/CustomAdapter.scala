package com.eny.smallpoll.view

import android.content.Context
import android.graphics.{Color, Typeface}
import android.view._
import android.widget._
import com.eny.smallpoll.R
import com.eny.smallpoll.model.Answer
import org.scaloid.common._


class CustomAdapter(items:Array[Answer], res:Int, typeface:Option[Typeface], color:Option[Int], textSize:Option[Float])(implicit context: android.content.Context) extends SArrayAdapter[Nothing, Answer](items, res) {

  def ensureView(view:View, id:Int, group:ViewGroup) =
    if(view==null) {
      getContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater].inflate(id, group, false)
    }
    else {
      view
    }
  
  override def getView(position:Int, v:View, parent:ViewGroup):View = {
    val mView = ensureView(v, res, parent)
    val text = mView.findViewById(android.R.id.text1).asInstanceOf[TextView]
    if(position < items.length) {
      text.setText(items(position).toString)
      color.map(text.setTextColor)
      textSize.map(text.setTextSize)
      typeface.map(text.setTypeface)
    }
    mView
  }

}
