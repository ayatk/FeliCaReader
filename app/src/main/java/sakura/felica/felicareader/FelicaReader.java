package sakura.felica.felicareader;

import android.content.Context;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import sakura.felica.R;
import sakura.felica.felicahistory.EdyHistory;
import sakura.felica.felicahistory.ICaHistory;
import sakura.felica.felicahistory.IcocaPitapaHistory;
import sakura.felica.felicahistory.NanacoHistory;
import sakura.felica.felicahistory.SuicaPasmoHistory;
import sakura.felica.felicahistory.WaonHistory;

public class FelicaReader extends Fragment {

    private static final String TAG = "NFC";

    private final byte[] suicapasmopmm = {(byte) 0x10, (byte) 0x0B, (byte) 0x4B, (byte) 0x42, (byte) 0x84, (byte) 0x85, (byte) 0xD0, (byte) 0xFF}; //Suica,PASMO PMm
    private final byte[] icocapitapappm1 = {(byte) 0x04, (byte) 0x01, (byte) 0x4B, (byte) 0x02, (byte) 0x4F, (byte) 0x49, (byte) 0x93, (byte) 0xFF}; //ICOCA,PiTaPa PMm
    private final byte[] icocapitapappm2 = {(byte) 0x01, (byte) 0x36, (byte) 0x42, (byte) 0x82, (byte) 0x47, (byte) 0x45, (byte) 0x9a, (byte) 0xFF}; //ICOCA,PiTaPa PMm
    private final byte[] icawaonpmm = {(byte) 0x03, (byte) 0x01, (byte) 0x4B, (byte) 0x02, (byte) 0x4F, (byte) 0x49, (byte) 0x93, (byte) 0xFF}; //ICa,WAON(JAL),Edy PMm
    private final byte[] edywaonpmm = {(byte) 0x01, (byte) 0x20, (byte) 0x22, (byte) 0x04, (byte) 0x27, (byte) 0x67, (byte) 0x4E, (byte) 0xFF}; //Edy,WAON(credit) PMm
    private final byte[] edynanacopmm = {(byte) 0x03, (byte) 0x32, (byte) 0x42, (byte) 0x82, (byte) 0x82, (byte) 0x47, (byte) 0xAA, (byte) 0xFF}; //nanaco,Edy PMm

    private final byte[] suicapmm = {(byte) 0x05, (byte) 0x31, (byte) 0x43, (byte) 0x45, (byte) 0x46, (byte) 0x82, (byte) 0xB7, (byte) 0xFF}; //new suica 2017年12月14日

    private final byte[] suicamid1 = {(byte) 0x01, (byte) 0x14}; //Suica ManufactureID
    private final byte[] suicamid2 = {(byte) 0x01, (byte) 0x01}; //Suica ManufactureID
    private final byte[] suicamid3 = {(byte) 0x01, (byte) 0x12}; //Suica ManufactureID
    private final byte[] pasmomid = {(byte) 0x01, (byte) 0x10}; //PASMO ManufactureID
    private final byte[] icocamid = {(byte) 0x01, (byte) 0x01}; //ICOCA ManufactureID
    private final byte[] icamid = {(byte) 0x01, (byte) 0x12}; //ICOCA ManufactureID
    private final byte[] waonmid1 = {(byte) 0x01, (byte) 0x14}; //WAON ManufactureID
    private final byte[] waonmid2 = {(byte) 0x01, (byte) 0x16}; //WAON ManufactureID
    //private final byte[] nanacoid = {(byte)0x01,(byte)0x10}; //nanaco ManufactureID
    //private final byte[] pitapaid = {(byte)0x01,(byte)0x14}; //PiTaPa ManufactureID

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.readerview_fragment, container, false);
    }

    public void readTag(Tag tag, Context context) {

        byte[] felicaIDm;
        byte[] felicapmm;
        byte[] mftid;

        int number;

        NfcF nfc = NfcF.get(tag);

        //IDm取得
        felicaIDm = tag.getId();
        Log.d(TAG, "IDm:" + toHex(felicaIDm));

        //製造者ID取得
        mftid = new byte[]{felicaIDm[0], felicaIDm[1]};
        Log.d(TAG, "ManufactureID:" + toHex(mftid));

        //PMm取得
        felicapmm = nfc.getManufacturer();
        Log.d(TAG, "PMm:" + toHex(felicapmm));

        try {
            nfc.connect();

            //カードの識別
            if (Arrays.equals(felicapmm, suicapasmopmm) || Arrays.equals(felicapmm, icocapitapappm1) || Arrays.equals(felicapmm, icocapitapappm2) || Arrays.equals(felicapmm, suicapmm)) {
                if (Arrays.equals(felicapmm, suicapasmopmm) || Arrays.equals(felicapmm, suicapmm)) {
                    number = 1;
                    felica(nfc, felicaIDm, mftid, number, context);
                } else if (Arrays.equals(felicapmm, icocapitapappm1) || Arrays.equals(felicapmm, icocapitapappm2)) {

                    number = 2;
                    felica(nfc, felicaIDm, mftid, number, context);
                }
            } else if (Arrays.equals(felicapmm, edynanacopmm)) {
                try {
                    number = 6;
                    felica(nfc, felicaIDm, mftid, number, context);
                } catch (Exception e) {
                    number = 4;
                    felica(nfc, felicaIDm, mftid, number, context);
                }
            } else if (Arrays.equals(felicapmm, icawaonpmm)) {
                if (Arrays.equals(mftid, icamid)) {
                    number = 3;
                    felica(nfc, felicaIDm, mftid, number, context);
                } else if (Arrays.equals(mftid, waonmid1) || Arrays.equals(mftid, waonmid2)) {
                    try {
                        number = 5;
                        felica(nfc, felicaIDm, mftid, number, context);
                    } catch (Exception e) {
                        number = 4;
                        felica(nfc, felicaIDm, mftid, number, context);
                    }
                }
            } else if (Arrays.equals(felicapmm, edywaonpmm)) {
                try {
                    number = 4;
                    felica(nfc, felicaIDm, mftid, number, context);
                } catch (Exception e) {
                    number = 5;
                    felica(nfc, felicaIDm, mftid, number, context);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(context, R.string.read_error, Toast.LENGTH_LONG).show();
        } finally {
            try {
                nfc.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 各種FeliCa用の処理
     */
    private void felica(NfcF nfc, byte[] felicaIDm, byte[] mftid, int number, Context context) throws Exception {
        byte[] polling;
        byte[] pollingRes;
        byte[] targetSystemcode;
        byte[] servicecode;
        String card = "";
        servicecode = new byte[]{};
        switch (number) {
            case 1:
                Log.d(TAG, "FeliCa:" + "Suica,PASMO");
                servicecode = new byte[]{(byte) 0x09, (byte) 0x0f};
                if (Arrays.equals(mftid, suicamid1) || Arrays.equals(mftid, suicamid2) || Arrays.equals(mftid, suicamid3)) {
                    card = "Suica";
                } else if (Arrays.equals(mftid, pasmomid)) {
                    card = "PASMO";
                } else {
                    card = "Suica/PASMO";
                }
                break;
            case 2:
                Log.d(TAG, "FeliCa:" + "ICOCA,PiTaPa");
                servicecode = new byte[]{(byte) 0x09, (byte) 0x0f};
                if (Arrays.equals(mftid, icocamid)) {
                    card = "ICOCA";
                } else {
                    //card = "PiTaPa";
                    card = "ICOCA,PiTaPa";
                }
                break;
            case 3:
                Log.d(TAG, "FeliCa:" + "ICa");
                servicecode = new byte[]{(byte) 0x89, (byte) 0x8f};
                card = "ICa";
                break;
            case 4:
                Log.d(TAG, "FeliCa:" + "Edy");
                targetSystemcode = new byte[]{(byte) 0xfe, (byte) 0x00};   //System 1(第２層)
                polling = polling(targetSystemcode);                    //Poolingコマンド作成
                pollingRes = nfc.transceive(polling);                   //Poolingコマンド送信・結果取得
                felicaIDm = Arrays.copyOfRange(pollingRes, 2, 10);      //System1のIDm取得(1バイト目データサイズ,2バイト目レスポンスコード,IDm8バイト)
                servicecode = new byte[]{(byte) 0x17, (byte) 0x0f};
                card = "Edy";
                break;
            case 5:
                Log.d(TAG, "FeliCa:" + "WAON");
                targetSystemcode = new byte[]{(byte) 0xfe, (byte) 0x00};
                polling = polling(targetSystemcode);                    //Poolingコマンド作成
                pollingRes = nfc.transceive(polling);                   //Poolingコマンド送信・結果取得
                felicaIDm = Arrays.copyOfRange(pollingRes, 2, 10);      //System1のIDm取得(1バイト目データサイズ,2バイト目レスポンスコード,IDm8バイト)
                servicecode = new byte[]{(byte) 0x68, (byte) 0x17};
                card = "WAON";
                break;
            case 6:
                Log.d(TAG, "FeliCa:" + "nanaco");
                targetSystemcode = new byte[]{(byte) 0xfe, (byte) 0x00};
                polling = polling(targetSystemcode);                  //Poolingコマンド作成
                pollingRes = nfc.transceive(polling);                 //Poolingコマンド送信・結果取得
                felicaIDm = Arrays.copyOfRange(pollingRes, 2, 10);      //System1のIDm取得(1バイト目データサイズ,2バイト目レスポンスコード,IDm8バイト)
                servicecode = new byte[]{(byte) 0x55, (byte) 0x97};
                card = "nanaco";
                break;
        }
        byte[] req = readWithoutEncryption(felicaIDm, servicecode, 1);  // Read Without Encryption コマンドを作成(IDm,読み取る個数)
        Log.d(TAG, "req:" + toHex(req));
        byte[] res = nfc.transceive(req);                       // カードにリクエスト送信
        Log.d(TAG, "res:" + toHex(res));
        Log.d(TAG, "balance:" + parse(res, number, context));
        changeFragment((parse(res, number, context)), card);
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
    private byte[] readWithoutEncryption(byte[] idm, byte[] serviceCode, int size)
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

    private String parse(byte[] res, int number, Context context) {
        // res[10] = エラーコード。0x00の場合正常。
        if (res[10] != 0x00) throw new RuntimeException("Felica error.");

        // res[12] = 応答ブロック数
        // res[13+n*16] = 履歴データ。16byte/ブロックの繰り返し。
        int size = res[12];
        String str = "";

        // 各FeliCa残高履歴の解析
        switch (number) {
            case 1:
                for (int i = 0; i < size; i++) {
                    SuicaPasmoHistory rireki = SuicaPasmoHistory.parse(res, 13 + i * 16);
                    str = rireki.toString() + "\n";
                }
                break;
            case 2:
                for (int i = 0; i < size; i++) {
                    IcocaPitapaHistory rireki = IcocaPitapaHistory.parse(res, 13 + i * 16);
                    str = rireki.toString() + "\n";
                }
                break;
            case 3:
                for (int i = 0; i < size; i++) {
                    ICaHistory rireki = ICaHistory.parse(res, 13 + i * 16);
                    str = rireki.toString() + "\n";
                }
                break;
            case 4:
                for (int i = 0; i < size; i++) {
                    EdyHistory rireki = EdyHistory.parse(res, 13 + i * 16);
                    str = rireki.toString() + "\n";
                }
                break;
            case 5:
                for (int i = 0; i < size; i++) {
                    WaonHistory rireki = WaonHistory.parse(res, 13 + i * 16);
                    str = rireki.toString() + "\n";
                }
                break;
            case 6:
                for (int i = 0; i < size; i++) {
                    NanacoHistory rireki = NanacoHistory.parse(res, 13 + i * 16);
                    str = rireki.toString() + "\n";
                }
                break;
            default:
                Toast.makeText(context, R.string.support_error, Toast.LENGTH_LONG).show();
                break;
        }
        return str;
    }

    private void changeFragment(String data, String card) {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(70);
        HistoryView fragment = new HistoryView();
        FragmentTransaction transaction = this.getFragmentManager()
                .beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("card", card);
        bundle.putString("data", data);
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