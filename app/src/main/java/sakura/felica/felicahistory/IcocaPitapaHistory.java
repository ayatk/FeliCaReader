package sakura.felica.felicahistory;

/**
 * ICOCA,PiTaPa履歴コード
 * 参考：https://osdn.jp/projects/felicalib/wiki/suica
 */
public class IcocaPitapaHistory {

    public int remain;

    public IcocaPitapaHistory() {
    }

    public static IcocaPitapaHistory parse(byte[] res, int off) {
        IcocaPitapaHistory self = new IcocaPitapaHistory();
        self.init(res, off);
        return self;
    }

    private void init(byte[] res, int off) {
        this.remain = toInt(res, off, 11, 10); //11-10: ICOCA,PiTaPa残高
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
