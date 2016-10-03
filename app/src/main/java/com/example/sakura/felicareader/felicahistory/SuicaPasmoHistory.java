package com.example.sakura.felicareader.felicahistory;

/**
 * Suica,Pasmo履歴コード
 * 参考：https://osdn.jp/projects/felicalib/wiki/suica
 */

public class SuicaPasmoHistory {

    public int remain;

    public SuicaPasmoHistory(){
    }

    public static SuicaPasmoHistory parse(byte[] res, int off) {
        SuicaPasmoHistory self = new SuicaPasmoHistory();
        self.init(res, off);
        return self;
    }

    private void init(byte[] res, int off) {
        this.remain  = toInt(res, off, 11,10); //11-10: Suica,Pasmo残高
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
