package sakura.felica.felicareader;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import sakura.felica.R;

public class MainActivity extends AppCompatActivity {

    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    private FelicaReader felicaReader = new FelicaReader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, felicaReader).commit();
        }

        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            ndef.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[]{ndef};

        // FelicaはNFC-TypeFなのでNfcFのみ指定
        techListsArray = new String[][]{
                new String[]{NfcF.class.getName()}
        };

        // NfcAdapterを取得
        mAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

        if (mAdapter == null) {
            //NFCが搭載されてない端末
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
            builder.setMessage("サービス対象外です");
            builder.setPositiveButton("キャンセル", null);

        } else if (!mAdapter.isEnabled()) {
            //NFCが無効になっている時
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
            builder.setTitle("NFC無効");
            builder.setMessage("NFCを有効にしてください");
            builder.setPositiveButton("設定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                }
            });
            builder.setNegativeButton("キャンセル", null);

            AlertDialog myDialog = builder.create();
            //ダイアログ画面外をタッチされても消えないようにする。
            myDialog.setCanceledOnTouchOutside(false);
            //ダイアログ表示
            myDialog.show();
        }

        Intent intent = getIntent();
        getTag(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // NFCの読み込みを有効化
        mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getTag(intent);
    }

    @Override
    protected void onPause() {
        if (this.isFinishing()) {
            mAdapter.disableForegroundDispatch(this);
        }
        super.onPause();
    }

    public void getTag(Intent intent) {
        // IntentにTagの基本データが入ってくるので取得。
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) {
            return;
        }
        felicaReader.readTag(tag, getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
