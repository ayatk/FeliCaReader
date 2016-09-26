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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FelicaReader extends Fragment {

    private static final String TAG = "NFC";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.readerview_fragment, container, false);
    }

    public void readTag(Tag tag, Context context) {

        byte[] felicaIDm;

        NfcF nfc = NfcF.get(tag);

        felicaIDm = tag.getId(); //IDm取得
        Log.d(TAG, "IDm:" + toHex(felicaIDm));


        try {
            nfc.connect();
            // Read Without Encryption コマンドを作成(IDm,読み取る個数)
            byte[] req = readWithoutEncryption(felicaIDm, 1);
            Log.d(TAG, "req:" + toHex(req));
            // カードにリクエスト送信
            byte[] res = nfc.transceive(req);
            Log.d(TAG, "res:" + toHex(res));
            nfc.close();

            Log.d(TAG, "balance:" + parse(res));

            changeFragment(parse(res));

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(context, R.string.read_error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Read Without Encryption コマンド取得
     */
    private byte[] readWithoutEncryption(byte[] idm, int size)
            throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(100);

        bout.write(0);           // データ長バイトのダミー
        bout.write(0x06);        // コマンド「Read Without Encryption」
        bout.write(idm);         // IDm 8byte
        bout.write(1);           // サービスコードリストの長さ(以下２バイトがこの数分繰り返す)
        bout.write(0x8f);        // 利用履歴のサービスコード下位バイト suica 0f ica 8f
        bout.write(0x89);        // 利用履歴のサービスコード上位バイト suica 0f ica 89
        bout.write(size);        // ブロック数
        for (int i = 0; i < size; i++) {
            bout.write(0x80);    // ブロックエレメント上位バイト 「Felicaユーザマニュアル抜粋」の4.3項参照
            bout.write(i);       // ブロック番号
        }

        byte[] msg = bout.toByteArray();
        msg[0] = (byte) msg.length; // 先頭１バイトはデータ長
        return msg;
    }

    private String parse(byte[] res) throws Exception {
        // res[10] = エラーコード。0x00の場合正常。
        if (res[10] != 0x00) throw new RuntimeException("Felica error.");

        // res[12] = 応答ブロック数
        // res[13+n*16] = 履歴データ。16byte/ブロックの繰り返し。
        int size = res[12];
        String str = "";
        for (int i = 0; i < size; i++) {
            // 残高履歴の解析。
            ICaHistory rireki = ICaHistory.parse(res, 13 + i * 16);
            str = rireki.toString() +"\n";
        }
        return str;
    }

    private void changeFragment(String data){
        HistoryView fragment = new HistoryView();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                .beginTransaction();
        Bundle bundle = new Bundle();
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