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
import com.example.sakura.felicareader.felicahistory.ICaHistory;
import com.example.sakura.felicareader.felicahistory.SuicaHistory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class FelicaReader extends Fragment {

    private static final String TAG = "NFC";

    private final byte[] ica = {(byte) 0x80,(byte) 0xef}; //ICa システムコード
    private final byte[] suica = {(byte) 0x00,(byte) 0x03}; //Suica システムコード

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.readerview_fragment, container, false);
    }

    public void readTag(Tag tag, Context context) {

        byte[] felicaIDm;
        byte[] systemcode;
        byte[] servicecode = new byte[]{};
        String card = "";

        NfcF nfc = NfcF.get(tag);

        felicaIDm = tag.getId(); //IDm取得
        Log.d(TAG, "IDm:" + toHex(felicaIDm));

        systemcode = nfc.getSystemCode(); //システムコード取得
        Log.d(TAG, "Systemcode:" + toHex(systemcode));

        try {
            nfc.connect();

            //カードの識別
            if(Arrays.equals(systemcode ,ica)){
                Log.d(TAG, "systemcode:" + "ICa");
                servicecode =new byte[]{(byte) 0x8f ,(byte) 0x89};
                card = "ICa";
            }else if(Arrays.equals(systemcode ,suica)){
                Log.d(TAG, "systemcode:" + "Suica");
                servicecode =new byte[]{(byte) 0x0f ,(byte) 0x09};
                card = "Suica";
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
     * Read Without Encryption コマンド取得
     */
    private byte[] readWithoutEncryption(byte[] idm, byte[] serviceCode,int size)
            throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(100);

        bout.write(0);           // データ長バイトのダミー
        bout.write(0x06);        // コマンド「Read Without Encryption」
        bout.write(idm);         // IDm 8byte
        bout.write(1);           // サービスコードリストの長さ(以下２バイトがこの数分繰り返す)
        bout.write(serviceCode[0]);        // 利用履歴のサービスコード下位バイト suica 0f ica 8f
        bout.write(serviceCode[1]);        // 利用履歴のサービスコード上位バイト suica 0f ica 89
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
            case "ICa":
                for (int i = 0; i < size; i++) {
                    ICaHistory rireki = ICaHistory.parse(res, 13 + i * 16);
                    str = rireki.toString() +"\n";
                }
                break;
            case "Suica":
                for (int i = 0; i < size; i++) {
                    SuicaHistory rireki = SuicaHistory.parse(res, 13 + i * 16);
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