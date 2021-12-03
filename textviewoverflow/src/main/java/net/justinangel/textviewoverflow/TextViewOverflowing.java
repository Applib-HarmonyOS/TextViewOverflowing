package net.justinangel.textviewoverflow;

import ohos.aafwk.ability.Ability;
import ohos.agp.components.AttrSet;
import ohos.agp.components.Component;
import ohos.agp.components.Text;
import ohos.agp.render.Paint;
import ohos.agp.text.Layout;
import ohos.agp.text.SimpleTextLayout;
import ohos.agp.utils.Rect;
import ohos.app.Context;

/**
 * TextViewOverflowing library file.
 */
public class TextViewOverflowing extends Text implements Component.LayoutRefreshedListener {
    private static final String SIMPLE_TEXT = "SimpleTextLayout";
    private Paint mTextPaint;
    private String overflowText;
    private boolean isRefreshDone = false;

    public TextViewOverflowing(Context context) {
        super(context);
        initViews();
    }

    public TextViewOverflowing(Context context, AttrSet attrs) {
        super(context, attrs);
        initViews();
    }

    public TextViewOverflowing(Context context, AttrSet attrs, java.lang.String defStyle) {
        super(context, attrs, defStyle);
        initViews();
    }

    @Override
    public void setText(String text) {
        super.setText(text);
    }

    private void initViews() {
        mTextPaint = new Paint();
        mTextPaint.setDither(true);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setMultipleLine(true);
        mTextPaint.setTextSize(getTextSize());
        setLayoutRefreshedListener(this);
    }

    @Override
    public void onRefreshed(Component component) {
        Layout mtextlayout;
        if (isRefreshDone) {
            return;
        }
        int width = component.getWidth();
        int height = component.getHeight();
        mtextlayout = new SimpleTextLayout(getText(), mTextPaint, new Rect(0, 0, width, height), width);
        // only try to calculate ellipsized text if MaxLines were set
        int lineHeight = calculateLineHeight();
        if (getMaxTextLines() != Integer.MAX_VALUE) {
            // get the index of the Ellipsis in the line that has an elipsis
            int ellipIndex = (int) mtextlayout.getEndCharIndex(getMaxTextLines() - 1);
            // get all the text after the elipsis
            setOverflowText((String) getText().subSequence(ellipIndex, getText().length()));
            isRefreshDone = true;
        }
        // only do crazy truncation if we need too.
        // this math isn't 100% accurate, but it should never fail.
        boolean allTextVisible = (getHeight() * mtextlayout.getLineCount()) <= mtextlayout.getHeight();
        if (!allTextVisible && !isRefreshDone) {
            // get the index of the last visible line
            int lastLineIndex = getHeight() / lineHeight;
            if (lastLineIndex > 0) {
                setMaxTextLines(lastLineIndex);
            }
        }
    }

    private int calculateLineHeight() {
        SimpleTextLayout simpleTextLayout = new SimpleTextLayout(SIMPLE_TEXT, mTextPaint, new Rect(), 0);
        return simpleTextLayout.getBottom(0) - simpleTextLayout.getTop(0);
    }



    public String getOverflowText() {
        return overflowText;
    }

    private void setOverflowText(String overflowText) {
        this.overflowText = overflowText;
        invokeOverflowTextListener();
        updateOverflowTextView();
    }

    private void updateOverflowTextView() {
        if (getOverflowTextViewId() != 0 && getContext() instanceof Ability) {
            Text overflowTextView = (Text) ((Ability) getContext()).findComponentById(overflowTextViewId);
            overflowTextView.setText(getOverflowText());
        }
    }

    private OverflowTextListener overflowTextListener;

    public void setOverflowTextListener(OverflowTextListener listener) {
        overflowTextListener = listener;
    }

    private void invokeOverflowTextListener() {
        OverflowTextListener listener = overflowTextListener;
        if (listener != null) {
            listener.overflowTextCalculated(overflowText);
        }
    }

    private int overflowTextViewId;

    public int getOverflowTextViewId() {
        return overflowTextViewId;
    }

    public void setOverflowTextViewId(int overflowTextViewId) {
        this.overflowTextViewId = overflowTextViewId;
    }

    /**
     * OverflowTextListener interface.
     */
    public interface OverflowTextListener {
        void overflowTextCalculated(String overflowText);
    }
}
