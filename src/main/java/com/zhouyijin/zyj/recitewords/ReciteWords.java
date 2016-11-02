package com.zhouyijin.zyj.recitewords;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;


/**
 * Created by zyj on 2016/8/13.
 *
 * 这个类是用来显示例句,并将要背的单词空下来,用户需要在空位里写单词,如果单词输入错误字会变红,单词输入正确字会变绿
 *
 * 
 */

// TODO: 2016/8/23 当遇到单词的一些变化形式时不能正确变绿,比如,buy的过去式bought
public class ReciteWords extends FrameLayout implements TVReciteWords.OnKeyWordHasBeenDrawnListener {

    private final float mTextSize;
    private final int mTextColor;
    private String mContent = "";
    private String mKeyWords = "";
    private float mLineInterval;

    private EVWords mEVWords;
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


        initView();
    }

    private void initView() {
        mEVWords = new EVWords(getContext());
        mTVReciteWords = new TVReciteWords(getContext());
        mTVReciteWords.setOnKeyWordHasBeenDrawnListener(this);


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

        addView(mTVReciteWords);
        addView(mEVWords);

    }

    public void setTextSize(float textSize) {
        mTVReciteWords.setTextSizeSP(textSize);
        //要重新给ev调整布局
//        adjustLayout();
    }

    public void setTextColor(int color) {
        mTVReciteWords.setTextColor(color);
    }

    public float getmLineInterval() {
        return mLineInterval;
    }

    public void setmLineInterval(float mLineInterval) {
        mTVReciteWords.setLineInterval((int) mLineInterval);
        this.mLineInterval = mLineInterval;
//        adjustLayout();
    }

    public void setReciteWords(String content, String keywords) {
        //如果文字没有改变,则什么都不做
        if (mContent != null) {
            if (mContent.equals(content) && mKeyWords.equals(keywords)) {
                return;
            }
        }
        mContent = content;
        mKeyWords = keywords;
        mTVReciteWords.setText(content, keywords);
        mEVWords.setKeyWords(keywords);
        //要重新给ev调整布局
//        adjustLayout();
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
        mEVWords.requestFocus();
    }

    public boolean isInputCorrect() {
        String inputString = mEVWords.getText().toString();
        if (inputString == null || inputString.equals("")) {
            return false;
        }
        if (inputString.equals(mKeyWords)) {
            return true;
        }
        mEVWords.setTextColor(getResources().getColor(R.color.wrongColor));
        return false;
    }


    @Override
    public void onKeyWordHasBeenDrawn(int keyWordsX, int keyWordsY, int keyWordWidth, float textSize) {
        MarginLayoutParams params = (MarginLayoutParams) mEVWords.getLayoutParams();
        if (params == null) {
            //// TODO: 2016/10/23 原先使用的是MarginLayoutParams,需要观察是否能使用
            params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        params.width = keyWordWidth;
        params.setMargins(keyWordsX, keyWordsY, 0, 0);
        mEVWords.setLayoutParams(params);
        mEVWords.setTextSizePX(textSize);
        mEVWords.setFocusable(true);
        mEVWords.setFocusableInTouchMode(true);
        mEVWords.requestFocus();
    }
}
