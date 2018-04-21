package sakura.felica.felicahistory

/**
 * nanaco履歴コード
 * 参考：http://jennychan.web.fc2.com/format/nanaco.html
 */

class NanacoHistory {
  var remain: Int = 0

  private fun init(res: ByteArray, off: Int) {
    this.remain = toInt(res, off, 3, 2, 1, 0) //12-15: Edy残高
  }

  private fun toInt(res: ByteArray, off: Int, vararg idx: Int): Int {
    var num = 0
    for (i in idx.indices) {
      num = num shl 8
      num += res[off + idx[i]].toInt() and 0x0ff
    }
    return num
  }

  override fun toString(): String = "残高：" + remain + "円"

  companion object {

    fun parse(res: ByteArray, off: Int): NanacoHistory {
      val self = NanacoHistory()
      self.init(res, off)
      return self
    }
  }
}
