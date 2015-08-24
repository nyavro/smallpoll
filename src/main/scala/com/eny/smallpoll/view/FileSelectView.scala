package com.eny.smallpoll.view

import java.io.{FileFilter, File}

import android.app.Activity
import android.content.Intent
import android.os.{Bundle, Environment}
import android.view.View
import android.widget.{ArrayAdapter, AdapterView, AbsListView}
import android.widget.AdapterView.OnItemClickListener
import com.eny.smallpoll.R
import com.eny.smallpoll.view.components.Wrap
import org.scaloid.common._

/**
 * Created by eny on 10.07.15.
 */
class FileSelectView extends SActivity {
  lazy val filePath = new STextView(sdRoot.getPath)
  lazy val files = new SListView
  lazy val ok = new SButton(getString(R.string.dialog_ok))
  lazy val cancel = new SButton(getString(R.string.dialog_cancel))

  lazy val sdRoot = Environment.getExternalStorageDirectory

  var extensions = Array[String]()

  def adapter(item: Wrap) = {
    def children(directory:File) =
      directory
        .listFiles(
          new FileFilter() {
            override def accept(file: File) = {
              !file.isHidden && (file.isDirectory || extensions.exists(file.getName.endsWith))
            }
          }
        )
        .map(item => Wrap(item))
    val file = if(item.file.isFile) item.file.getParentFile else item.file
    new ArrayAdapter(
      this,
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

  onCreate {
    initArguments()
    filePath.disable()
    ok.setEnabled(false)
    files.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE)
    files.setAdapter(adapter(Wrap(sdRoot)))
    files.setOnItemClickListener(
      new OnItemClickListener {
        override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = {
          val item = files.getAdapter.getItem(position).asInstanceOf[Wrap]
          filePath.setText(item.file.getPath)
          if(item.file.isDirectory) {
            files.setAdapter(adapter(item))
          }
          ok.setEnabled(extensions.isEmpty || item.file.isFile)
        }
      }
    )
    files.setSelector(R.drawable.selector)
    ok.onClick {
      val intent = new Intent()
      intent.putExtra(FileSelectView.SelectedPath, filePath.getText.toString)
      setResult(Activity.RESULT_OK, intent)
      finish()
    }
    cancel.onClick {
      setResult(Activity.RESULT_CANCELED)
      finish()
    }
    val buttonsArea = new SLinearLayout() {
      cancel.<<.fill.Weight(1.0f).>>
      ok.<<.fill.Weight(1.0f).>>
    } += cancel += ok
    val empty = new SLinearLayout()

    val runArea = new SLinearLayout() {
      files.<<.fill.Weight(1.0f)
      buttonsArea.<<(MATCH_PARENT, WRAP_CONTENT)
    } += filePath += files += buttonsArea
//    runArea.setBackground(R.drawable.listitem_background)

//    files.setBackgroundResource(android.R.attr.activatedBackgroundIndicator)
    runArea.setOrientation(runArea.VERTICAL)
    setContentView(runArea)
  }

  def initArguments() = {
    extensions = getIntent.getStringArrayExtra(FileSelectView.Extensions)
  }

  override def onSaveInstanceState(bundle:Bundle) = {
    super.onSaveInstanceState(bundle)
    bundle.putStringArray(FileSelectView.Extensions, extensions)
  }
  override def onRestoreInstanceState(bundle:Bundle) = {
    super.onRestoreInstanceState(bundle)
    extensions= bundle.getStringArray(FileSelectView.Extensions)
  }
}

object FileSelectView {
  val Extensions = "extensions"
  val SelectedPath = "selectedPath"
}
