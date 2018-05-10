package sakura.felica.ui

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcF
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import sakura.felica.R

class MainActivity : AppCompatActivity() {

  private var intentFiltersArray: Array<IntentFilter>? = null
  // FelicaはNFC-TypeFなのでNfcFのみ指定
  private val techListsArray = arrayOf(arrayOf(NfcF::class.java.name))
  private val nfcAdapter: NfcAdapter? by lazy {
    NfcAdapter.getDefaultAdapter(this)
  }
  private var pendingIntent: PendingIntent? = null
  private val felicaReader = FelicaReader()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    if (savedInstanceState == null) {
      val transaction = supportFragmentManager.beginTransaction()
      transaction.add(R.id.fragment_container, felicaReader).commit()
    }

    pendingIntent = PendingIntent.getActivity(
        this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
    )

    val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)

    try {
      ndef.addDataType("text/plain")
    } catch (e: IntentFilter.MalformedMimeTypeException) {
      throw RuntimeException("fail", e)
    }

    intentFiltersArray = arrayOf(ndef)

    if (nfcAdapter == null) {
      //NFCが搭載されてない端末
      val builder = AlertDialog.Builder(this@MainActivity, R.style.MyAlertDialogStyle)
      builder.setMessage("サービス対象外です")
      builder.setPositiveButton("キャンセル", null)
    } else if (!nfcAdapter!!.isEnabled) {
      //NFCが無効になっている時
      val builder = AlertDialog.Builder(this@MainActivity, R.style.MyAlertDialogStyle)
      builder.setTitle("NFC無効")
      builder.setMessage("NFCを有効にしてください")
      builder.setPositiveButton("設定") { _, _ -> startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) }
      builder.setNegativeButton("キャンセル", null)

      val myDialog = builder.create()
      //ダイアログ画面外をタッチされても消えないようにする。
      myDialog.setCanceledOnTouchOutside(false)
      //ダイアログ表示
      myDialog.show()
    }
    getTag(intent)
  }

  override fun onResume() {
    super.onResume()

    // NFCの読み込みを有効化
    nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    getTag(intent)
  }

  override fun onPause() {
    if (this.isFinishing) {
      nfcAdapter?.disableForegroundDispatch(this)
    }
    super.onPause()
  }

  private fun getTag(intent: Intent) {
    // IntentにTagの基本データが入ってくるので取得。
    val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
    felicaReader.readTag(tag, applicationContext)
  }
}
