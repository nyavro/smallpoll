package com.eny.smallpoll.view.components

import java.io.{File, FileFilter}

import android.app.AlertDialog
import android.content.{Context, DialogInterface, Intent}
import android.os.Environment
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget._
import com.eny.smallpoll.R
import org.scaloid.common.Preferences

/**
 * Created by eny on 15.05.15.
 */

class EditPathPreference3(ctx:Context, attrs:AttributeSet) extends DialogPreference(ctx,attrs) {

  setDialogLayoutResource(R.layout.file_choose)

  val sdRoot = Environment.getExternalStorageDirectory

  var activePath = sdRoot.getPath

  val imageExtensions = Set(".mp4", ".3gp")

  def children(directory:File) =
    directory
      .listFiles(
        new FileFilter() {
          override def accept(file: File) = {
            !file.isHidden && (file.isDirectory || imageExtensions.exists(file.getName.endsWith))
          }
        }
      )
      .map(item => Wrap(item))


  def indexOf(file: File, adapter: ListAdapter) = (1 to adapter.getCount).find(index => adapter.getItem(index).toString.equals(file.getName))

  override def onBindDialogView(view:View) = {
    val path = new Preferences(getSharedPreferences).backgroundPath(activePath)
    if(new File(path).exists()) {
      activePath = path
    }
    val files = view.findViewById(R.id.files).asInstanceOf[ListView]
    files.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE)
    val activeFile = new File(activePath)
    files.setAdapter(adapter(Wrap(activeFile)))
    files.setOnItemClickListener(
      new OnItemClickListener {
        override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = {
          val item = files.getAdapter.getItem(position).asInstanceOf[Wrap]
          activePath = item.file.getPath
          val okButton = getDialog.asInstanceOf[AlertDialog].getButton(DialogInterface.BUTTON_POSITIVE)
          if(item.file.isDirectory) {
            files.setAdapter(adapter(item))
//            crop.setEnabled(false)
          }
          else {
//            crop.setEnabled(true)
//            val intent = new Intent(getContext, classOf[ImageCropView])
//            intent.putExtra(ImageCropView.SourcePath, activePath)
//            intent.putExtra(ImageCropView.TargetPath, new File(getContext.getFilesDir, "portrait.png").getPath)
//            getContext.startActivity(intent)

          }
        }
      }
    )
    super.onBindDialogView(view)
  }

  def adapter(item: Wrap) = {
    val file = if(item.file.isFile) item.file.getParentFile else item.file
    new ArrayAdapter(
      getContext,
      R.layout.custom_singlechoice_layout,
      if (file.getPath.equals(sdRoot.getPath)) {
        children(file)
      }
      else {
        val parent = file.getParentFile
        val displayName = parent.toString.replace(sdRoot.getPath, "")
        (Wrap(parent, Some(if (displayName.isEmpty) "/" else displayName)) :: children(file).toList).toArray
      }
    )
  }

  override def onDialogClosed(positive:Boolean) = {
    super.onDialogClosed(positive)
    if(positive && new File(activePath).isFile) {
      persistString(activePath)
    }
  }
}
