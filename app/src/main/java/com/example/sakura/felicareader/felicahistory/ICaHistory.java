package com.example.sakura.felicareader.felicahistory;

/**
 * ICa履歴コード
 * 参考：http://jennychan.web.fc2.com/format/ica.html
 */
public class ICaHistory {

    public int remain;

    public ICaHistory(){
    }

    public static ICaHistory parse(byte[] res, int off) {
        ICaHistory self = new ICaHistory();
        self.init(res, off);
        return self;
    }

    private void init(byte[] res, int off) {
        this.remain  = toInt(res, off, 13,14); //13-14: ICa残高
    }

    private int toInt(byte[] res, int off, int... idx) {
        int num = 0;
        for (int i=0; i<idx.length; i++) {
            num = num << 8;
            num += ((int)res[off+idx[i]]) & 0x0ff;
        }
        return num;
    }

    public String toString() {
        String str = "残高："+remain+"円";
        return str;
    }
}
