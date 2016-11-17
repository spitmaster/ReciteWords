package com.zhouyijin.zyj.recitewords;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.DateKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;


/**
 * Created by zyj on 2016/8/13.
 * <p>
 * 这个类是用来显示例句,并将要背的单词空下来,用户需要在空位里写单词,如果单词输入错误字会变红,单词输入正确字会变绿
 */

// TODO: 2016/8/23 当遇到单词的一些变化形式时不能正确变绿,比如,buy的过去式bought
public class ReciteWords extends FrameLayout implements TVReciteWords.OnKeyWordHasBeenDrawnListener, TextView.OnEditorActionListener, EVWords.OnTextChangedListener {

    private final float mTextSize;
    private final int mTextColor;
    private String mContent = "";
    private String mKeyWords = "";
    private String mKeywordsTrans = "";
    private float mLineInterval;

    private InputMethodManager imm;

    private EVWords mEVWords;
    private OnTextChangedListener listener;

    private TVReciteWords mTVReciteWords;

    public ReciteWords(Context context) {
        this(context, null);
    }

    public ReciteWords(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReciteWords(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ReciteWords);
        mTextSize = a.getDimension(R.styleable.ReciteWords_TextSize, 20);
        mTextColor = a.getColor(R.styleable.ReciteWords_TextColor, Color.BLACK);
        mContent = a.getString(R.styleable.ReciteWords_Content);
        mKeyWords = a.getString(R.styleable.ReciteWords_KeyWords);
        mLineInterval = a.getDimension(R.styleable.ReciteWords_LineInterval, 5);
        a.recycle();
        imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        initView();
    }

    private OnCheckListener checkListener;

    public void setOnCheckListener(OnCheckListener listener) {
        checkListener = listener;
    }


    private void initView() {
        mEVWords = new EVWords(getContext());
        mTVReciteWords = new TVReciteWords(getContext());
        mTVReciteWords.setOnKeyWordHasBeenDrawnListener(this);
        mEVWords.setOnTextChangedListener(this);


        mEVWords.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        mEVWords.setImeOptions(EditorInfo.IME_ACTION_DONE); //设置右下方按键为完成
        mEVWords.setOnEditorActionListener(this);

        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetter(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };

        mEVWords.setFilters(new InputFilter[]{filter});


        this.setFocusable(false);
        this.setFocusableInTouchMode(false);

        mTVReciteWords.setFocusable(false);
        mTVReciteWords.setFocusableInTouchMode(false);

        //// TODO: 2016/10/23 原先使用的是MarginLayoutParams,需要观察是否能使用
        MarginLayoutParams evParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        MarginLayoutParams tvParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTVReciteWords.setElevation(0);
            mEVWords.setElevation(4);
        }
        setTextSize(mTextSize);
        setTextColor(mTextColor);

        mEVWords.setLayoutParams(evParams);
        mTVReciteWords.setLayoutParams(tvParams);

        mEVWords.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        addView(mTVReciteWords);
        addView(mEVWords);
        mEVWords.setFocusable(true);
        mEVWords.setFocusableInTouchMode(true);
    }

    public void setTextSize(float textSize) {
        mTVReciteWords.setTextSizeSP(textSize);
        //要重新给ev调整布局
        adjustLayout();
    }

    public void setTextColor(int color) {
        mTVReciteWords.setTextColor(color);
    }

    public float getLineInterval() {
        return mLineInterval;
    }

    public void setLineInterval(float mLineInterval) {
        mTVReciteWords.setLineInterval((int) mLineInterval);
        this.mLineInterval = mLineInterval;
        adjustLayout();
    }

    public void setWordChangeListener(OnTextChangedListener listener) {
        this.listener = listener;
    }

    /**
     * 这个方法用来设置view中的内容,
     *
     * @param content       这个是整个句子的内容, 但是keyword的位置会被掏空留白
     * @param keywords      如果在没有keywordsTrans的情况下留白的位置是keywords
     * @param keywordsTrans 这个如果不是null,则留白位置以keywordsTrans为准,这个代表的单词在句中的变化形式
     *                      如content为"this was a pen"
     *                      其中keyword就是"is",而keywordsTrans就是"was"
     *                      在有keywordsTrans的情况下,输入keywordsTrans或者keywords都会使输入的文字变绿.表示正确
     */
    public void setReciteWords(String content, String keywords, @Nullable String keywordsTrans) {
        //如果文字没有改变,则什么都不做
        if (mContent != null) {
            if (mContent.equals(content) && mKeyWords.equals(keywords)) {
                return;
            }
        }
        mContent = content;
        mKeyWords = keywords;
        mKeywordsTrans = keywordsTrans;
        if (keywordsTrans != null && !keywordsTrans.equals("")) {
            mTVReciteWords.setText(content, keywordsTrans);
            mEVWords.setKeyWords(keywords, keywordsTrans);
        } else {
            mTVReciteWords.setText(content, keywords);
            mEVWords.setKeyWords(keywords);
        }
        //要重新给ev调整布局
        adjustLayout();
    }

    /**
     * 如果没有 keywordsTrans的情况下直接调用这个也行
     *
     * @param content  同setReciteWords(String content, String keywords, @Nullable String keywordsTrans);
     * @param keywords 同setReciteWords(String content, String keywords, @Nullable String keywordsTrans);
     */
    public void setReciteWords(String content, String keywords) {
        setReciteWords(content, keywords, null);
    }

    //当改变了布局的时候要调用这个方法,不然edittext的位置不对
    private void adjustLayout() {

        MarginLayoutParams params = (MarginLayoutParams) mEVWords.getLayoutParams();
        if (params == null) {
            //// TODO: 2016/10/23 原先使用的是MarginLayoutParams,需要观察是否能使用
            params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        params.width = mTVReciteWords.getKeyWordWidth();
        params.setMargins(mTVReciteWords.getKeyWordsX(), mTVReciteWords.getKeyWordMarginTop(), 0, 0);
        mEVWords.setLayoutParams(params);
        mEVWords.setTextSizeSP(mTVReciteWords.getTextSizeSP());
        mEVWords.setFocusable(true);
        mEVWords.setFocusableInTouchMode(true);
        requestTextFocus();
    }

    public boolean isInputCorrect() {
        String inputString = mEVWords.getText().toString();
        if (inputString == null || inputString.equals("")) {
            return false;
        }
        inputString = inputString.toLowerCase();
        String lowKeywords = mKeyWords.toLowerCase();
        if (inputString.equals(lowKeywords)) {
            imm.hideSoftInputFromWindow(mEVWords.getWindowToken(), 0);
            return true;
        } else if (mKeywordsTrans != null) {
            String lowKeywordTrans = mKeywordsTrans.toLowerCase();
            if (!lowKeywordTrans.equals("") && inputString.equals(lowKeywordTrans)) {
                imm.hideSoftInputFromWindow(mEVWords.getWindowToken(), 0);
                return true;
            }
        }
        mEVWords.setTextColor(getResources().getColor(R.color.wrongColor));
        return false;
    }


    @Override
    public void onKeyWordHasBeenDrawn(int keyWordsX, int keyWordsY, int keyWordWidth, float textSize) {
        MarginLayoutParams params = (MarginLayoutParams) mEVWords.getLayoutParams();
        if (params == null) {
            //// TODO: 2016/10/23 原先使用的是MarginLayoutParams,需要观察是否能使用
            params = new LayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
        }
        params.width = keyWordWidth;
        params.setMargins(keyWordsX, keyWordsY, 0, 0);
        mEVWords.setLayoutParams(params);
        mEVWords.setTextSizePX(textSize);
    }

    public void requestTextFocus() {
        mEVWords.requestFocus();
        imm.showSoftInput(mEVWords, InputMethodManager.SHOW_FORCED);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (actionId) {
            case EditorInfo.IME_ACTION_DONE:
                checkListener.onCheck(isInputCorrect());
                return true;
        }
        return false;
    }

    @Override   //当输入的文本发生改变
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (listener != null) {
            listener.onTextChanged(text, start, lengthBefore, lengthAfter);
        }
    }


    public interface OnTextChangedListener {
        void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter);
    }

    public interface OnCheckListener {
        void onCheck(boolean isInputCorrect);
    }

}
