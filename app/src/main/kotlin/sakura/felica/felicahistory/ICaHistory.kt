package sakura.felica.felicahistory

/**
 * ICa履歴コード
 * 参考：http://jennychan.web.fc2.com/format/ica.html
 */
class ICaHistory {

  private var remain: Int = 0

  private fun init(res: ByteArray, off: Int) {
    this.remain = toInt(res, off, 13, 14) //13-14: ICa残高
  }

  private fun toInt(res: ByteArray, off: Int, vararg idx: Int): Int {
    var num = 0
    for (anIdx in idx) {
      num = num shl 8
      num += res[off + anIdx].toInt() and 0x0ff
    }
    return num
  }

  override fun toString(): String = "残高：" + remain + "円"

  companion object {

    fun parse(res: ByteArray, off: Int): ICaHistory {
      val self = ICaHistory()
      self.init(res, off)
      return self
    }
  }
}
