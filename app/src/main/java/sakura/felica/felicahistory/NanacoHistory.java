package sakura.felica.felicahistory;

/**
 * nanaco履歴コード
 * 参考：http://jennychan.web.fc2.com/format/nanaco.html
 */

public class NanacoHistory {
    public int remain;

    public NanacoHistory() {
    }

    public static NanacoHistory parse(byte[] res, int off) {
        NanacoHistory self = new NanacoHistory();
        self.init(res, off);
        return self;
    }

    private void init(byte[] res, int off) {
        this.remain = toInt(res, off, 3, 2, 1, 0); //12-15: Edy残高
    }

    private int toInt(byte[] res, int off, int... idx) {
        int num = 0;
        for (int i = 0; i < idx.length; i++) {
            num = num << 8;
            num += ((int) res[off + idx[i]]) & 0x0ff;
        }
        return num;
    }

    public String toString() {
        String str = "残高：" + remain + "円";
        return str;
    }
}
