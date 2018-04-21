package sakura.felica.felicahistory;

/**
 * Edy履歴コード
 * 参考：http://jennychan.web.fc2.com/format/edy.html
 */

public class EdyHistory {
    private int remain;

    public static EdyHistory parse(byte[] res, int off) {
        EdyHistory self = new EdyHistory();
        self.init(res, off);
        return self;
    }

    private void init(byte[] res, int off) {
        this.remain = toInt(res, off, 12, 13, 14, 15); //12-15: Edy残高
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
