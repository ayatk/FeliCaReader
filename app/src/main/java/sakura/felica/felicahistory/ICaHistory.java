package sakura.felica.felicahistory;

/**
 * ICa履歴コード
 * 参考：http://jennychan.web.fc2.com/format/ica.html
 */
public class ICaHistory {

    private int remain;

    public static ICaHistory parse(byte[] res, int off) {
        ICaHistory self = new ICaHistory();
        self.init(res, off);
        return self;
    }

    private void init(byte[] res, int off) {
        this.remain = toInt(res, off, 13, 14); //13-14: ICa残高
    }

    private int toInt(byte[] res, int off, int... idx) {
        int num = 0;
        for (int anIdx : idx) {
            num = num << 8;
            num += ((int) res[off + anIdx]) & 0x0ff;
        }
        return num;
    }

    public String toString() {
        return "残高：" + remain + "円";
    }
}
