package sakura.felica.ui

import android.content.Context
import android.nfc.Tag
import android.nfc.tech.NfcF
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import sakura.felica.R
import sakura.felica.felicahistory.EdyHistory
import sakura.felica.felicahistory.ICaHistory
import sakura.felica.felicahistory.IcocaPitapaHistory
import sakura.felica.felicahistory.NanacoHistory
import sakura.felica.felicahistory.SuicaPasmoHistory
import sakura.felica.felicahistory.WaonHistory
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Arrays

class FelicaReader : Fragment() {

  private val suicapasmopmm = byteArrayOf(
      0x10.toByte(),
      0x0B.toByte(),
      0x4B.toByte(),
      0x42.toByte(),
      0x84.toByte(),
      0x85.toByte(),
      0xD0.toByte(),
      0xFF.toByte()
  ) //Suica,PASMO PMm
  private val icocapitapappm1 = byteArrayOf(
      0x04.toByte(),
      0x01.toByte(),
      0x4B.toByte(),
      0x02.toByte(),
      0x4F.toByte(),
      0x49.toByte(),
      0x93.toByte(),
      0xFF.toByte()
  ) //ICOCA,PiTaPa PMm
  private val icocapitapappm2 = byteArrayOf(
      0x01.toByte(),
      0x36.toByte(),
      0x42.toByte(),
      0x82.toByte(),
      0x47.toByte(),
      0x45.toByte(),
      0x9a.toByte(),
      0xFF.toByte()
  ) //ICOCA,PiTaPa PMm
  private val icawaonpmm = byteArrayOf(
      0x03.toByte(),
      0x01.toByte(),
      0x4B.toByte(),
      0x02.toByte(),
      0x4F.toByte(),
      0x49.toByte(),
      0x93.toByte(),
      0xFF.toByte()
  ) //ICa,WAON(JAL),Edy PMm
  private val edywaonpmm = byteArrayOf(
      0x01.toByte(),
      0x20.toByte(),
      0x22.toByte(),
      0x04.toByte(),
      0x27.toByte(),
      0x67.toByte(),
      0x4E.toByte(),
      0xFF.toByte()
  ) //Edy,WAON(credit) PMm
  private val edynanacopmm = byteArrayOf(
      0x03.toByte(),
      0x32.toByte(),
      0x42.toByte(),
      0x82.toByte(),
      0x82.toByte(),
      0x47.toByte(),
      0xAA.toByte(),
      0xFF.toByte()
  ) //nanaco,Edy PMm

  private val suicapmm = byteArrayOf(
      0x05.toByte(),
      0x31.toByte(),
      0x43.toByte(),
      0x45.toByte(),
      0x46.toByte(),
      0x82.toByte(),
      0xB7.toByte(),
      0xFF.toByte()
  ) //new suica 2017年12月14日

  private val suicamid1 = byteArrayOf(0x01.toByte(), 0x14.toByte()) //Suica ManufactureID
  private val suicamid2 = byteArrayOf(0x01.toByte(), 0x01.toByte()) //Suica ManufactureID
  private val suicamid3 = byteArrayOf(0x01.toByte(), 0x12.toByte()) //Suica ManufactureID
  private val pasmomid = byteArrayOf(0x01.toByte(), 0x10.toByte()) //PASMO ManufactureID
  private val icocamid = byteArrayOf(0x01.toByte(), 0x01.toByte()) //ICOCA ManufactureID
  private val icamid = byteArrayOf(0x01.toByte(), 0x12.toByte()) //ICOCA ManufactureID
  private val waonmid1 = byteArrayOf(0x01.toByte(), 0x14.toByte()) //WAON ManufactureID
  private val waonmid2 = byteArrayOf(0x01.toByte(), 0x16.toByte()) //WAON ManufactureID
  //private final byte[] nanacoid = {(byte)0x01,(byte)0x10}; //nanaco ManufactureID
  //private final byte[] pitapaid = {(byte)0x01,(byte)0x14}; //PiTaPa ManufactureID

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.readerview_fragment, container, false)
  }

  fun readTag(tag: Tag, context: Context) {

    val felicaIDm: ByteArray
    val felicapmm: ByteArray
    val mftid: ByteArray

    var number: Int

    val nfc = NfcF.get(tag)

    //IDm取得
    felicaIDm = tag.id
    Timber.d("IDm:%s", toHex(felicaIDm))

    //製造者ID取得
    mftid = byteArrayOf(felicaIDm[0], felicaIDm[1])
    Timber.d("ManufactureID:%s", toHex(mftid))

    //PMm取得
    felicapmm = nfc.manufacturer
    Timber.d("PMm:%s", toHex(felicapmm))

    try {
      nfc.connect()

      //カードの識別
      if (Arrays.equals(felicapmm, suicapasmopmm) || Arrays.equals(
              felicapmm,
              icocapitapappm1
          ) || Arrays.equals(felicapmm, icocapitapappm2) || Arrays.equals(felicapmm, suicapmm)
      ) {
        if (Arrays.equals(felicapmm, suicapasmopmm) || Arrays.equals(felicapmm, suicapmm)) {
          number = 1
          felica(nfc, felicaIDm, mftid, number, context)
        } else if (Arrays.equals(felicapmm, icocapitapappm1) || Arrays.equals(
                felicapmm,
                icocapitapappm2
            )
        ) {

          number = 2
          felica(nfc, felicaIDm, mftid, number, context)
        }
      } else if (Arrays.equals(felicapmm, edynanacopmm)) {
        try {
          number = 6
          felica(nfc, felicaIDm, mftid, number, context)
        } catch (e: Exception) {
          number = 4
          felica(nfc, felicaIDm, mftid, number, context)
        }
      } else if (Arrays.equals(felicapmm, icawaonpmm)) {
        if (Arrays.equals(mftid, icamid)) {
          number = 3
          felica(nfc, felicaIDm, mftid, number, context)
        } else if (Arrays.equals(mftid, waonmid1) || Arrays.equals(mftid, waonmid2)) {
          try {
            number = 5
            felica(nfc, felicaIDm, mftid, number, context)
          } catch (e: Exception) {
            number = 4
            felica(nfc, felicaIDm, mftid, number, context)
          }
        }
      } else if (Arrays.equals(felicapmm, edywaonpmm)) {
        try {
          number = 4
          felica(nfc, felicaIDm, mftid, number, context)
        } catch (e: Exception) {
          number = 5
          felica(nfc, felicaIDm, mftid, number, context)
        }
      }
    } catch (e: Exception) {
      Timber.e(e, e.message)
      Toast.makeText(context, R.string.read_error, Toast.LENGTH_LONG).show()
    } finally {
      try {
        nfc.close()
      } catch (e: Exception) {
      }
    }
  }

  /**
   * 各種FeliCa用の処理
   */
  @Throws(Exception::class)
  private fun felica(
    nfc: NfcF,
    felicaIDm: ByteArray,
    mftid: ByteArray,
    number: Int,
    context: Context
  ) {
    var felicaIDm = felicaIDm
    val polling: ByteArray
    val pollingRes: ByteArray
    val targetSystemcode: ByteArray
    var servicecode: ByteArray
    var card = ""
    servicecode = byteArrayOf()
    when (number) {
      1 -> {
        Log.d(TAG, "FeliCa:" + "Suica,PASMO")
        servicecode = byteArrayOf(0x09.toByte(), 0x0f.toByte())
        card =
            if (Arrays.equals(mftid, suicamid1) || Arrays.equals(mftid, suicamid2) || Arrays.equals(
                    mftid,
                    suicamid3
                )
            ) {
              "Suica"
            } else if (Arrays.equals(mftid, pasmomid)) {
              "PASMO"
            } else {
              "Suica/PASMO"
            }
      }
      2 -> {
        Log.d(TAG, "FeliCa:" + "ICOCA,PiTaPa")
        servicecode = byteArrayOf(0x09.toByte(), 0x0f.toByte())
        card = if (Arrays.equals(mftid, icocamid)) {
          "ICOCA"
        } else {
          //card = "PiTaPa";
          "ICOCA,PiTaPa"
        }
      }
      3 -> {
        Log.d(TAG, "FeliCa:" + "ICa")
        servicecode = byteArrayOf(0x89.toByte(), 0x8f.toByte())
        card = "ICa"
      }
      4 -> {
        Log.d(TAG, "FeliCa:" + "Edy")
        targetSystemcode = byteArrayOf(0xfe.toByte(), 0x00.toByte())   //System 1(第２層)
        polling = polling(targetSystemcode)                    //Poolingコマンド作成
        pollingRes = nfc.transceive(polling)                   //Poolingコマンド送信・結果取得
        felicaIDm = Arrays.copyOfRange(
            pollingRes,
            2,
            10
        )      //System1のIDm取得(1バイト目データサイズ,2バイト目レスポンスコード,IDm8バイト)
        servicecode = byteArrayOf(0x17.toByte(), 0x0f.toByte())
        card = "Edy"
      }
      5 -> {
        Log.d(TAG, "FeliCa:" + "WAON")
        targetSystemcode = byteArrayOf(0xfe.toByte(), 0x00.toByte())
        polling = polling(targetSystemcode)                    //Poolingコマンド作成
        pollingRes = nfc.transceive(polling)                   //Poolingコマンド送信・結果取得
        felicaIDm = Arrays.copyOfRange(
            pollingRes,
            2,
            10
        )      //System1のIDm取得(1バイト目データサイズ,2バイト目レスポンスコード,IDm8バイト)
        servicecode = byteArrayOf(0x68.toByte(), 0x17.toByte())
        card = "WAON"
      }
      6 -> {
        Log.d(TAG, "FeliCa:" + "nanaco")
        targetSystemcode = byteArrayOf(0xfe.toByte(), 0x00.toByte())
        polling = polling(targetSystemcode)                  //Poolingコマンド作成
        pollingRes = nfc.transceive(polling)                 //Poolingコマンド送信・結果取得
        felicaIDm = Arrays.copyOfRange(
            pollingRes,
            2,
            10
        )      //System1のIDm取得(1バイト目データサイズ,2バイト目レスポンスコード,IDm8バイト)
        servicecode = byteArrayOf(0x55.toByte(), 0x97.toByte())
        card = "nanaco"
      }
    }
    val req = readWithoutEncryption(
        felicaIDm,
        servicecode,
        1
    )  // Read Without Encryption コマンドを作成(IDm,読み取る個数)
    Log.d(TAG, "req:" + toHex(req))
    val res = nfc.transceive(req)                       // カードにリクエスト送信
    Log.d(TAG, "res:" + toHex(res))
    Log.d(TAG, "balance:" + parse(res, number, context))
    changeFragment(parse(res, number, context), card)
  }

  /**
   * Polling コマンド取得
   */
  private fun polling(systemCode: ByteArray): ByteArray {
    val bout = ByteArrayOutputStream(100)
    bout.write(0x00)           // データ長バイトのダミー
    bout.write(0x00)           // コマンドコード
    bout.write(systemCode[0].toInt())  // systemCode
    bout.write(systemCode[1].toInt())  // systemCode
    bout.write(0x01)           // リクエストコード
    bout.write(0x0f)           // タイムスロット

    val msg = bout.toByteArray()
    msg[0] = msg.size.toByte() // 先頭１バイトはデータ長
    return msg
  }

  /**
   * Read Without Encryption コマンド取得
   */
  @Throws(IOException::class)
  private fun readWithoutEncryption(idm: ByteArray, serviceCode: ByteArray, size: Int): ByteArray {
    val bout = ByteArrayOutputStream(100)

    bout.write(0)           // データ長バイトのダミー
    bout.write(0x06)        // コマンド「Read Without Encryption」
    bout.write(idm)         // IDm 8byte
    bout.write(1)           // サービスコードリストの長さ(以下２バイトがこの数分繰り返す)
    bout.write(serviceCode[1].toInt())        // 利用履歴のサービスコード下位バイト
    bout.write(serviceCode[0].toInt())        // 利用履歴のサービスコード上位バイト
    bout.write(size)        // ブロック数
    for (i in 0 until size) {
      bout.write(0x80)    // ブロックエレメント上位バイト 「Felicaユーザマニュアル抜粋」の4.3項参照
      bout.write(i)       // ブロック番号
    }

    val msg = bout.toByteArray()
    msg[0] = msg.size.toByte() // 先頭１バイトはデータ長
    return msg
  }

  private fun parse(res: ByteArray, number: Int, context: Context): String {
    // res[10] = エラーコード。0x00の場合正常。
//    if (res[10].toInt() != 0x00) throw RuntimeException("Felica error.")

    // res[12] = 応答ブロック数
    // res[13+n*16] = 履歴データ。16byte/ブロックの繰り返し。
    val size = res[12].toInt()
    var str = ""

    // 各FeliCa残高履歴の解析
    when (number) {
      1 -> for (i in 0 until size) {
        val rireki = SuicaPasmoHistory.parse(res, 13 + i * 16)
        str = rireki.toString() + "\n"
      }
      2 -> for (i in 0 until size) {
        val rireki = IcocaPitapaHistory.parse(res, 13 + i * 16)
        str = rireki.toString() + "\n"
      }
      3 -> for (i in 0 until size) {
        val rireki = ICaHistory.parse(res, 13 + i * 16)
        str = rireki.toString() + "\n"
      }
      4 -> for (i in 0 until size) {
        val rireki = EdyHistory.parse(res, 13 + i * 16)
        str = rireki.toString() + "\n"
      }
      5 -> for (i in 0 until size) {
        val rireki = WaonHistory.parse(res, 13 + i * 16)
        str = rireki.toString() + "\n"
      }
      6 -> for (i in 0 until size) {
        val rireki = NanacoHistory.parse(res, 13 + i * 16)
        str = rireki.toString() + "\n"
      }
      else -> Toast.makeText(context, R.string.support_error, Toast.LENGTH_LONG).show()
    }
    return str
  }

  private fun changeFragment(data: String, card: String) {
    val vibrator = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    vibrator.vibrate(70)
    val fragment = HistoryView()
    val transaction = this.fragmentManager!!
        .beginTransaction()
    val bundle = Bundle()
    bundle.putString("card", card)
    bundle.putString("data", data)
    fragment.arguments = bundle
    transaction.replace(R.id.fragment_container, fragment)
    transaction.addToBackStack(null)
    transaction.commit()
  }

  private fun toHex(id: ByteArray): String {
    val sbuf = StringBuilder()
    for (i in id.indices) {
      var hex = "0" + Integer.toString(id[i].toInt() and 0x0ff, 16)
      if (hex.length > 2)
        hex = hex.substring(1, 3)
      sbuf.append(" $i:$hex")
    }
    return sbuf.toString()
  }

  companion object {

    private val TAG = "NFC"
  }
}