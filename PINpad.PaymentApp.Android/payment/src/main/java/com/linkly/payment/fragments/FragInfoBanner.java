package com.linkly.payment.fragments;

import static com.linkly.libmal.global.util.Util.GetCellularSignalStrength;
import static com.linkly.libmal.global.util.Util.GetMobileConnected;
import static com.linkly.libui.display.DisplayUtil.GetSignalLevelString;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.payment.R;
import com.linkly.payment.activities.AppMain;

public class FragInfoBanner extends Fragment implements View.OnClickListener {

    private View view;

    public String header;
    private String line1;
    private String line2;
    private String line3;

    public static FragInfoBanner newInstance(int sectionNumber, String header, String line1) {
        return newInstance(sectionNumber, header, line1,"");
    }

    public static FragInfoBanner newInstance(int sectionNumber, String header, String line1, String line2) {
        return newInstance(sectionNumber, header, line1, line2, "");
    }

    public static FragInfoBanner newInstance(int sectionNumber, String header, String line1, String line2, String line3) {
        FragInfoBanner fragment = new FragInfoBanner();
        Bundle args = new Bundle();
        args.putInt("section_number", sectionNumber);
        fragment.setArguments(args);
        fragment.setHeader(header);
        fragment.setLine1(line1);
        fragment.setLine2(line2);
        fragment.setLine3(line3);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        TextView tvHeader = (TextView) getView().findViewById(R.id.info_header);
        if (tvHeader != null ) {

            /* DYNAMIC updates to the data should be done here, as its called reliably when the data needs refreshing (normally 1 screen ahead of itself) */
            if (header.contains(Engine.getDep().getPrompt(String_id.STR_STORED_TRANSACTIONS))) {
                FragHeader.transCount = TransRec.countTransInBatch();
                header = Engine.getDep().getPrompt(String_id.STR_STORED_TRANSACTIONS) + ": " + FragHeader.transCount;
            }  else if (header.contains(Engine.getDep().getPrompt(String_id.STR_CONNECTION_TYPE))) {
                String connectionType = Util.GetConnectedNetworkName(requireContext().getApplicationContext());
                if (connectionType.equals("Wi-Fi")) {
                    int signalLevel = Util.GetWifiSignalStrength(requireContext().getApplicationContext());
                    header = Engine.getDep().getPrompt(String_id.STR_CONNECTION_TYPE) + ": " + connectionType;
                    line2 = (signalLevel * 25) + "% " + GetSignalLevelString(signalLevel);
                } else if (GetMobileConnected(requireContext(), false) ){
                    int signalLevel = GetCellularSignalStrength(requireContext().getApplicationContext());
                    header = Engine.getDep().getPrompt(String_id.STR_CONNECTION_TYPE) + ": " + connectionType;
                    line2 = ( signalLevel * 25 ) + "% " + GetSignalLevelString(signalLevel);
                } else {
                    int signalLevel = 0;
                    header = Engine.getDep().getPrompt(String_id.STR_CONNECTION_TYPE) + ": " + connectionType;
                    line2 = ( signalLevel * 25 ) + "% " + GetSignalLevelString(signalLevel);
                }
            }

            tvHeader.setText(header);
        }

        TextView tvLine1 = (TextView) getView().findViewById(R.id.info_line1);
        if (tvLine1 != null) {
            if ( line1.length() != 0 ) {
                tvLine1.setText( line1 );
            } else {
                tvLine1.setVisibility( View.GONE );
            }
        }

        TextView tvLine2 = (TextView) getView().findViewById(R.id.info_line2);
        if (tvLine2 != null)
            if ( line2.length() != 0 ) {
                tvLine2.setText( line2 );
            } else {
                tvLine2.setVisibility( View.GONE );
            }

        TextView tvLine3 = (TextView) getView().findViewById(R.id.info_line3);
        if (tvLine3 != null)
            if ( line3.length() != 0 ) {
                tvLine3.setText( line3 );
            } else {
                tvLine3.setVisibility( View.GONE );
            }


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_info_banner, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppMain.addWatcher( this );
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public void setLine3(String line3) {
        this.line3 = line3;
    }
}
