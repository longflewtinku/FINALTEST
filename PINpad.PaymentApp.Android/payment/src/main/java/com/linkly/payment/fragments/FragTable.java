package com.linkly.payment.fragments;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_TABLE;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.ViewModelProviders;

import com.linkly.libui.IUIDisplay;
import com.linkly.libui.display.DisplayTableArray;
import com.linkly.libui.display.DisplayTableItem;
import com.linkly.libui.display.DisplayTableRow;
import com.linkly.payment.R;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.viewmodel.FragStandardViewModel;

import timber.log.Timber;

public class FragTable extends BaseFragment<ActivityTransBinding, FragStandardViewModel> {

    public static final String TAG = FragTable.class.getSimpleName();
    private FragStandardViewModel fragStandardViewModel;
    private DisplayTableArray tableData;

    public static FragTable newInstance() {
        Bundle args = new Bundle();
        FragTable fragment = new FragTable();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_table;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = ViewModelProviders.of(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_TABLE);
        return fragStandardViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        SetHeader(false, false);
        showScreen(v);
        return v;
    }

    @SuppressWarnings("deprecation")
    public void showScreen(View v) {
        int i = 0;
        int x = 0;

        super.showScreen();
        tableData = fragStandardViewModel.getDisplay().getValue().getUiExtras().getParcelable(IUIDisplay.uiScreenTableData);
        TableLayout table = (TableLayout) v.findViewById(R.id.tblView);

        /*For Each Row Add a new Row*/
        for (i = 0; i < tableData.getRows().size(); i++) {
            DisplayTableRow rowData = tableData.getRows().get(i);

            HorizontalScrollView scrl = new HorizontalScrollView(getBaseActivity());

            TableRow row = new TableRow(getBaseActivity());


            /*Add Content to the */
            for (x = 0; x < rowData.getItems().size(); x++) {
                TextView label = new TextView(getBaseActivity());
                DisplayTableItem item = rowData.getItems().get(x);

                if (item.getAlignment() == DisplayTableItem.LEFT_ALIGN) {
                    label.setGravity(Gravity.START);
                } else {
                    label.setGravity(Gravity.END);
                }

                /*Set the Text Style*/
                if (item.getTextStyle() == DisplayTableItem.TEXT_BOLD) {
                    TextViewCompat.setTextAppearance( label, R.style.ui2TableFontDarkBold);
                } else {
                    TextViewCompat.setTextAppearance( label, R.style.ui2TableFontDark);
                }

                label.setText(item.getTitle());
                label.setBackgroundColor(item.getBgColor());

                row.setPadding(0, 10, 0, 0);
                row.addView(label);
            }

            row.setTag("" + (i - 1));
            row.setOnClickListener( v1 -> {
                Timber.i(v1.getTag() + "");
                v1.setBackgroundColor(Color.DKGRAY);
                sendResponse(IUIDisplay.UIResultCode.OK, v1.getTag() + "", "");
            } );
            scrl.addView(row);

            table.addView(scrl);

        }
    }

}





