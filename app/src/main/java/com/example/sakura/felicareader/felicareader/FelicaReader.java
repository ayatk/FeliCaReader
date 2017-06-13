package com.example.sakura.felicareader.felicareader;

import android.content.Context;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sakura.felicareader.R;
import com.example.sakura.felicareader.felicahistory.EdyHistory;
import com.example.sakura.felicareader.felicahistory.ICaHistory;
import com.example.sakura.felicareader.felicahistory.IcocaPitapaHistory;
import com.example.sakura.felicareader.felicahistory.SuicaPasmoHistory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class FelicaReader extends Fragment {

    private static final String TAG = "NFC";

    private final byte[] suicapasmopmm = {(byte)0x10,(byte)0x0B,(byte)0x4B,(byte)0x42,(byte)0x84,(byte)0x85,(byte)0xD0,(byte)0xFF}; //Suica,PASMO PMm
    //private final byte[] icocapitapappm = {(byte)0x04,(byte)0x01,(byte)0x4B,(byte)0x02,(byte)0x4F,(byte)0x49,(byte)0x93,(byte)0xFF}; //ICOCA,PiTaPa PMm
    private final byte[] icocapitapappm = {(byte)0x01,(byte)0x36,(byte)0x42,(byte)0x82,(byte)0x47,(byte)0x45,(byte)0x9a,(byte)0xFF}; //ICOCA,PiTaPa PMm
    private final byte[] icapmm = {(byte)0x03,(byte)0x01,(byte)0x4B,(byte)0x02,(byte)0x4F,(byte)0x49,(byte)0x93,(byte)0xFF}; //ICa PMm
    private final byte[] edypmm = {(byte)0x01,(byte)0x20,(byte)0x22,(byte)0x04,(byte)0x27,(byte)0x67,(byte)0x4E,(byte)0xFF}; //Edy PMm

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.readerview_fragment, container, false);
    }

    public void readTag(Tag tag, Context context) {

        byte[] felicaIDm;
        byte[] felicapmm;
        byte[] servicecode;
        byte[] targetSystemcode;
        byte[] polling;
        byte[] pollingRes;

        String card = "";

        servicecode = new byte[]{};
        //pollingRes = new byte[]{};

        NfcF nfc = NfcF.get(tag);

        felicaIDm = tag.getId(); //IDm取得
        Log.d(TAG, "IDm:" + toHex(felicaIDm));

        felicapmm = nfc.getManufacturer(); //PMm取得
        Log.d(TAG, "PMm:" + toHex(felicapmm));

        try {
            nfc.connect();

            //カードの識別
            if(Arrays.equals(felicapmm ,suicapasmopmm)){
                Log.d(TAG, "systemcode:" + "Suica,PASMO");
                servicecode =new byte[]{(byte) 0x09 ,(byte) 0x0f};
                card = "Suica,PASMO";
            }else if(Arrays.equals(felicapmm ,icocapitapappm)){
                Log.d(TAG, "systemcode:" + "ICOCA,PiTaPa");
                servicecode =new byte[]{(byte) 0x09 ,(byte) 0x0f};
                card = "ICOCA,PiTaPa";
            }else if(Arrays.equals(felicapmm ,icapmm)){
                Log.d(TAG, "systemcode:" + "ICa");
                servicecode =new byte[]{(byte) 0x89 ,(byte) 0x8f};
                card = "ICa";
            }else if(Arrays.equals(felicapmm ,edypmm)){
                Log.d(TAG, "systemcode:" + "Edy");
                targetSystemcode = new byte[]{(byte)0xfe,(byte)0x00}; //System 1(第２層)
                polling = polling(targetSystemcode);                  //Poolingコマンド作成
                pollingRes = nfc.transceive(polling);                 //Poolingコマンド送信・結果取得
                felicaIDm = Arrays.copyOfRange(pollingRes,2,10);      //System1のIDm取得(1バイト目データサイズ,2バイト目レスポンスコード,IDm8バイト)
                Log.d(TAG, "IDm:" + toHex(felicaIDm));
                servicecode = new byte[]{(byte) 0x17 ,(byte) 0x0f};
                card = "Edy";
            }

            // Read Without Encryption コマンドを作成(IDm,読み取る個数)
            byte[] req = readWithoutEncryption(felicaIDm, servicecode, 1);
            Log.d(TAG, "req:" + toHex(req));
            // カードにリクエスト送信
            byte[] res = nfc.transceive(req);
            Log.d(TAG, "res:" + toHex(res));
            nfc.close();

            Log.d(TAG, "balance:" + parse(res,card,context));

            changeFragment((parse(res,card,context)),card);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(context, R.string.read_error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Polling コマンド取得
     */
    private byte[] polling(byte[] systemCode) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(100);

        bout.write(0x00);           // データ長バイトのダミー
        bout.write(0x00);           // コマンドコード
        bout.write(systemCode[0]);  // systemCode
        bout.write(systemCode[1]);  // systemCode
        bout.write(0x01);           // リクエストコード
        bout.write(0x0f);           // タイムスロット

        byte[] msg = bout.toByteArray();
        msg[0] = (byte) msg.length; // 先頭１バイトはデータ長
        return msg;
    }

    /**
     * Read Without Encryption コマンド取得
     */
    private byte[] readWithoutEncryption(byte[] idm, byte[] serviceCode,int size)
            throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(100);

        bout.write(0);           // データ長バイトのダミー
        bout.write(0x06);        // コマンド「Read Without Encryption」
        bout.write(idm);         // IDm 8byte
        bout.write(1);           // サービスコードリストの長さ(以下２バイトがこの数分繰り返す)
        bout.write(serviceCode[1]);        // 利用履歴のサービスコード下位バイト
        bout.write(serviceCode[0]);        // 利用履歴のサービスコード上位バイト
        bout.write(size);        // ブロック数
        for (int i = 0; i < size; i++) {
            bout.write(0x80);    // ブロックエレメント上位バイト 「Felicaユーザマニュアル抜粋」の4.3項参照
            bout.write(i);       // ブロック番号
        }

        byte[] msg = bout.toByteArray();
        msg[0] = (byte) msg.length; // 先頭１バイトはデータ長
        return msg;
    }

    private String parse(byte[] res,String card,Context context) throws Exception {
        // res[10] = エラーコード。0x00の場合正常。
        if (res[10] != 0x00) throw new RuntimeException("Felica error.");

        // res[12] = 応答ブロック数
        // res[13+n*16] = 履歴データ。16byte/ブロックの繰り返し。
        int size = res[12];
        String str = "";

        // 各FeliCa残高履歴の解析
        switch (card){
            case "Suica,PASMO":
                for (int i = 0; i < size; i++) {
                    SuicaPasmoHistory rireki = SuicaPasmoHistory.parse(res, 13 + i * 16);
                    str = rireki.toString() +"\n";
                }
                break;
            case "ICOCA,PiTaPa":
                for (int i = 0; i < size; i++) {
                    IcocaPitapaHistory rireki = IcocaPitapaHistory.parse(res, 13 + i * 16);
                    str = rireki.toString() +"\n";
                }
                break;
            case "ICa":
                for (int i = 0; i < size; i++) {
                    ICaHistory rireki = ICaHistory.parse(res, 13 + i * 16);
                    str = rireki.toString() +"\n";
                }
                break;
            case "Edy":
                for (int i = 0; i < size; i++) {
                    EdyHistory rireki = EdyHistory.parse(res, 13 + i * 16);
                    str = rireki.toString() +"\n";
                }
                break;
            default:
                Toast.makeText(context, R.string.support_error, Toast.LENGTH_LONG).show();
                break;
        }
        return str;
    }

    private void changeFragment(String data,String card){
        HistoryView fragment = new HistoryView();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                .beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("card",card);
        bundle.putString("data",data);
        fragment.setArguments(bundle);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private String toHex(byte[] id) {
        StringBuilder sbuf = new StringBuilder();
        for (int i = 0; i < id.length; i++) {
            String hex = "0" + Integer.toString((int) id[i] & 0x0ff, 16);
            if (hex.length() > 2)
                hex = hex.substring(1, 3);
            sbuf.append(" " + i + ":" + hex);
        }
        return sbuf.toString();
    }
}