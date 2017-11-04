package com.example.sakura.felicareader.felicahistory;

/**
 * Created by sakura on 2017/11/04.
 */

public class WaonHistory {

    public int remain;

    public WaonHistory(){
    }

    public static WaonHistory parse(byte[] res, int off) {
        WaonHistory self = new WaonHistory();
        self.init(res, off);
        return self;
    }

    private void init(byte[] res, int off) {
        this.remain  = toInt(res, off,0); //0: Edy残高
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
