package com.eny.smallpoll.view

import android.os.Bundle
import android.preference.PreferenceFragment
import com.eny.smallpoll.R
import org.scaloid.common.SActivity

/**
 * Created by eny on 15.05.15.
 */
class PreferencesView extends SActivity {

  onCreate {
    getFragmentManager
      .beginTransaction
      .replace(
        android.R.id.content,
        new PreferenceFragment {
          override def onCreate(bundle:Bundle) = {
            super.onCreate(bundle)
            addPreferencesFromResource(R.xml.preferences)
          }
        }
      )
      .commit()
  }

}
