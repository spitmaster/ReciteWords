package com.zhouyijin.zyj.recitewords;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;


/**
 * Created by zyj on 2016/8/13.
 * <p>
 * don't use this class directly
 * please use ReciteWords
 */
public class EVWords extends AppCompatEditText implements TextWatcher {


    public EVWords(Context context) {
        this(context, null);
    }

    public EVWords(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EVWords(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setTextAlignment(TEXT_ALIGNMENT_CENTER);
        }

        this.addTextChangedListener(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setSingleLine();
        setBackgroundResource(R.drawable.under_line);
    }

    protected String mKeyWords;
    protected String mKeyWordsTrans;

    public String getKeyWords() {
        return mKeyWords;
    }

    public void setKeyWords(String keyWords) {
        setKeyWords(keyWords, null);
    }

    public void setKeyWords(String keyWords, String keywordsTrans) {
        this.mKeyWords = keyWords;
        if (keywordsTrans == null) mKeyWordsTrans = keyWords;
        else this.mKeyWordsTrans = keywordsTrans;
        setText("");
    }

    public void setTextSizeSP(float textSize) {
        setTextSize(textSize);
    }

    public void setTextSizePX(float textSize) {
        setTextSize(DisplayUtil.px2sp(getContext(), textSize));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    private OnTextChangedListener listener;

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (listener != null) {
            listener.onTextChanged(text, start, lengthBefore, lengthAfter);
        }
    }

    public void setOnTextChangedListener(OnTextChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().equals("") || s == null || mKeyWords == null || mKeyWords.equals("")) {
            return;
        }
        String inputString = s.toString();
        if (mKeyWords.startsWith(inputString) || mKeyWordsTrans.startsWith(inputString)) {
            setTextColor(getResources().getColor(R.color.bingoColor));
        } else {
            setTextColor(getResources().getColor(R.color.wrongColor));
        }
    }


    //把文字改变监听向外传递
    public interface OnTextChangedListener {
        void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter);
    }

}
