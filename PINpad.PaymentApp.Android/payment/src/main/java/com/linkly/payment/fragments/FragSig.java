package com.linkly.payment.fragments;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_SIG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.MalFactory;
import com.linkly.libui.IUICurrency;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UI;
import com.linkly.payment.R;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.utilities.UIUtilities;
import com.linkly.payment.viewmodel.FragStandardViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import timber.log.Timber;

public class FragSig extends BaseFragment<ActivityTransBinding, FragStandardViewModel> {

    public static final String TAG = FragSig.class.getSimpleName();
    private FragStandardViewModel fragStandardViewModel;

    LinearLayout mContent;
    FragSig.signature mSignature;
    Button done;
    public static String ext_sig;
    public int count = 1;
    public String current = null;
    private Bitmap mBitmap;
    View mView;
    File app_sig;

    private String uniqueId;
    private EditText yourName;


    public static FragSig newInstance() {
        Bundle args = new Bundle();
        FragSig fragment = new FragSig();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_sig;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = ViewModelProviders.of(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_SIG);
        return fragStandardViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }


    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        SetHeader(false, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }


        TransRec t = Engine.getDep().getCurrentTransaction();
        if (t != null) {
            String details = UI.getInstance().getPrompt(t.getTransType().displayId);
            details += ": " + Engine.getDep().getFramework().getCurrency().formatUIAmount(String.valueOf(t.getAmounts().getTotalAmount()), IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, Engine.getDep().getPayCfg().getCountryCode(), true);

            TextView transDetails = (TextView) v.findViewById(R.id.transDetails);
            transDetails.setText(details);
        }

        Button cancel = (Button) v.findViewById(R.id.btnCancel);
        UIUtilities.borderTransparentButton(getActivity(),cancel);
        cancel.setOnClickListener(v12 -> sendResponse(IUIDisplay.UIResultCode.ABORT, "", ""));

        Button clear = (Button) v.findViewById(R.id.btnClear);
        GradientDrawable clearDrawable = (GradientDrawable) clear.getBackground().getConstantState().newDrawable().mutate();
        clearDrawable.setStroke(2, Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
        clearDrawable.setColor(ContextCompat.getColor(requireContext(), R.color.colorWhite));
        int btnColor = clearDrawable.getColor().getDefaultColor();
        int textColor = Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault();
        if(btnColor == Color.WHITE && textColor == Color.WHITE) {
            clear.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
        } else {
            clear.setTextColor(textColor);
        }
        clear.setBackground(clearDrawable);
        clear.setOnClickListener(v13 -> {
            mSignature.clear();
            done.setEnabled(false);
        });

        done = (Button) v.findViewById(R.id.btnDone);
        GradientDrawable doneDrawable = (GradientDrawable) done.getBackground().getConstantState().newDrawable().mutate();
        doneDrawable.setColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
        done.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault());
        done.setBackground(doneDrawable);
        done.setOnClickListener(v1 -> {
            mView.setDrawingCacheEnabled(true);
            mSignature.save(mView);
            sendResponse(IUIDisplay.UIResultCode.OK, "", "");

        });
        done.setEnabled(false);

        ext_sig = MalFactory.getInstance().getFile().getCommonDir() + "/sig/";

        prepareDirectory();
        uniqueId = Engine.getDep().getCurrentTransaction().getAudit().getUti();
        current = uniqueId + ".png";
        app_sig = new File(ext_sig + current);


        mContent = (LinearLayout) v.findViewById(R.id.signContent);
        mSignature = new FragSig.signature(getBaseActivity(), null);
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mView = mContent;
        return v;
    }

    private boolean captureSignature() {

        boolean error = false;
        return error;
    }

    private String getTodaysDate() {

        final Calendar c = Calendar.getInstance();
        int todaysDate =     (c.get(Calendar.YEAR) * 10000) +
                ((c.get(Calendar.MONTH) + 1) * 100) +
                (c.get(Calendar.DAY_OF_MONTH));
        Timber.w(String.valueOf(todaysDate));
        return(String.valueOf(todaysDate));

    }

    private String getCurrentTime() {

        final Calendar c = Calendar.getInstance();
        int currentTime =     (c.get(Calendar.HOUR_OF_DAY) * 10000) +
                (c.get(Calendar.MINUTE) * 100) +
                (c.get(Calendar.SECOND));
        Timber.w(String.valueOf(currentTime));
        return(String.valueOf(currentTime));

    }


    private boolean prepareDirectory()
    {
        try
        {
            if (makedirs())
            {
                return true;
            } else {
                return false;
            }
        } catch (Exception e)
        {
            Timber.w(e);
            return false;
        }
    }

    private boolean makedirs()
    {
        File tempdir = new File(ext_sig);
        if (!tempdir.exists())
            tempdir.mkdirs();

        if (tempdir.isDirectory())
        {
            File[] files = tempdir.listFiles();
            for (File file : files)
            {
                if (!file.delete())
                {
                    Timber.w("Failed to delete " + file);
                }
            }
        }
        return (tempdir.isDirectory());
    }

    public class signature extends View
    {
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs)
        {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }


        @SuppressWarnings("deprecation")
        public void save(View v)
        {
            Timber.v("Width: " + v.getWidth());
            Timber.v( "Height: " + v.getHeight());
            if(mBitmap == null)
            {
                mBitmap =  Bitmap.createBitmap (mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);;
            }
            Canvas canvas = new Canvas(mBitmap);
            try
            {
                FileOutputStream mFileOutStream = new FileOutputStream(app_sig);

                v.draw(canvas);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream);
                mFileOutStream.flush();
                mFileOutStream.close();
                String url = MediaStore.Images.Media.insertImage(getBaseActivity().getContentResolver(), mBitmap, "title", null);
                Timber.v("url: " + url);

            }
            catch(Exception e)
            {
                Timber.v( e.toString());
            }
        }

        public void clear()
        {
            path.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event)
        {
            float eventX = event.getX();
            float eventY = event.getY();
            done.setEnabled(true);

            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++)
                    {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate();

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string){
        }

        private void expandDirtyRect(float historicalX, float historicalY)
        {
            if (historicalX < dirtyRect.left)
            {
                dirtyRect.left = historicalX;
            }
            else if (historicalX > dirtyRect.right)
            {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top)
            {
                dirtyRect.top = historicalY;
            }
            else if (historicalY > dirtyRect.bottom)
            {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY)
        {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }
}





