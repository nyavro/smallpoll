package com.eny.smallpoll.view.components

import java.io.{FilenameFilter, File}

import android.app.AlertDialog.Builder
import android.content.{Context, DialogInterface}
import android.graphics.Typeface
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{BaseAdapter, CheckedTextView}
import org.scaloid.common.Preferences
/**
 * Created by eny on 15.05.15.
 */
class FontPreference(ctx:Context, attrs:AttributeSet) extends DialogPreference(ctx,attrs) {

  var fonts = List[(String, String)]()

  class FontAdapter extends BaseAdapter {

    override def getCount = fonts.size

    override def getItem(position: Int) = fonts(position) match {
      case (path, name) => name
    }

    override def getItemId(position: Int) = position

    def ensureView(view:View, group:ViewGroup) =
      if (view == null) {
        val inflater = getContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
        inflater.inflate(android.R.layout.select_dialog_singlechoice, group, false)
      } else {
        view
      }

    override def getView(position: Int, convertView:View, parent:ViewGroup) = {
      val view = ensureView(convertView, parent)
      if(view!=null) {
        val tv = view.findViewById(android.R.id.text1).asInstanceOf[CheckedTextView]
        val (path, name) = fonts(position)
        tv.setTypeface(Typeface.createFromFile(path))
        tv.setText(name)
      }
      view
    }
  }

  override def onPrepareDialogBuilder(builder:Builder) = {
    super.onPrepareDialogBuilder(builder)
    fonts = enumerateFonts()
    val selectedFontPath = getSharedPreferences.getString(getKey, "")
    val adapter = new FontAdapter()
    builder.setSingleChoiceItems(
      adapter,
      fonts.indexWhere {
        case (path, name) => selectedFontPath==path
      },
      this
    )
    builder.setPositiveButton(null, null)
  }

  override def onClick(dialog:DialogInterface, which:Int) = {
    if (which>=0 && which<fonts.size) {
      val (path, name) = fonts(which)
      new Preferences(getSharedPreferences).font_path = path
      dialog.dismiss()
    }
  }

  def enumerateFonts() =
    List("/system/fonts", "/system/font", "/data/fonts")
      .map(new File(_))
      .withFilter(_.exists)
      .map(
        _.listFiles(
          new FilenameFilter() {
            override def accept(dir: File, filename: String): Boolean = List(".ttf").exists(filename.endsWith)
          }
        )
      )
      .withFilter(_ != null)
      .map(_.toList)
      .foldLeft(List[File]()) {
        case (acc, item) => acc ++ item
      }
      .map {
        file => (file.getPath, file.getName.replaceFirst("[.][^.]+$", ""))
      }
}
