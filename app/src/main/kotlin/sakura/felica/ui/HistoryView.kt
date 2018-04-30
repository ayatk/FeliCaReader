package sakura.felica.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import sakura.felica.R
import timber.log.Timber

class HistoryView : Fragment() {

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.historyview_fragment, container, false)

    Timber.v(arguments!!.getString("card"))
    Timber.v(arguments!!.getString("data"))

    // カード名表示
    val cardText = view.findViewById<TextView>(R.id.card)
    cardText.text = arguments!!.getString("card")
    // 残高表示
    val balanceText = view.findViewById<TextView>(R.id.balance)
    balanceText.text = arguments!!.getString("data")

    return view
  }
}
