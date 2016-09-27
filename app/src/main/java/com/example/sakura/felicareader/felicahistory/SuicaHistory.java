package com.example.sakura.felicareader.felicahistory;

/**
 * Suica履歴コード
 * 参考：https://osdn.jp/projects/felicalib/wiki/suica
 */

public class SuicaHistory {

    public int remain;

    public SuicaHistory(){
    }

    public static SuicaHistory parse(byte[] res, int off) {
        SuicaHistory self = new SuicaHistory();
        self.init(res, off);
        return self;
    }

    private void init(byte[] res, int off) {
        this.remain  = toInt(res, off, 13,14); //11-10: Suica残高
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
