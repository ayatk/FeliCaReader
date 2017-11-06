package com.example.sakura.felicareader.felicahistory;

/**
 * Edy履歴コード
 * 参考：http://jennychan.web.fc2.com/format/edy.html
 */

public class EdyHistory {
    public int remain;

    public EdyHistory(){
    }

    public static EdyHistory parse(byte[] res, int off) {
        EdyHistory self = new EdyHistory();
        self.init(res, off);
        return self;
    }

    private void init(byte[] res, int off) {
        this.remain  = toInt(res, off, 12, 13, 14, 15); //12-15: Edy残高
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
