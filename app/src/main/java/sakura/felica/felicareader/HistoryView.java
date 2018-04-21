package sakura.felica.felicareader;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sakura.felica.R;

public class HistoryView extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.historyview_fragment, container, false);

        Log.v("Cardview", getArguments().getString("card"));
        Log.v("Dataview", getArguments().getString("data"));

        // カード名表示
        TextView cardText = view.findViewById(R.id.card);
        cardText.setText(getArguments().getString("card"));
        // 残高表示
        TextView balanceText = view.findViewById(R.id.balance);
        balanceText.setText(getArguments().getString("data"));

        return view;
    }
}
