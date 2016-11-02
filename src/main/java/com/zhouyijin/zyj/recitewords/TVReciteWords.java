package com.zhouyijin.zyj.recitewords;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


/**
 * Created by zyj on 2016/8/13.
 * <p>
 * don't use this class directly
 * please use ReciteWords
 */
public class TVReciteWords extends View {

    /**
     * 可以初始化的属性
     */
    volatile protected String mContent;
    volatile protected String mKeyWords;
    volatile protected Paint mPaint = new Paint();
    //这个是分开的每个单词组成的数组,在onMeasure的时候初始化;
    volatile protected String[] spiltWords;

    /**
     *
     *
     *
     */
    //行数
    protected int mLines;
    //行距
    protected int mLineInterval;

    protected int keyWordsX;


    public int getKeyWordsY() {
        return keyWordsY;
    }

    public int getKeyWordsX() {
        return keyWordsX;
    }

    public int getKeyWordWidth() {
        return keyWordWidth;
    }

    public int getKeyWordMarginTop() {
        return (int) (keyWordsY - getTextSize());
    }

    protected int keyWordsY;
    protected int keyWordWidth;

    public TVReciteWords(Context context) {
        this(context, null);
    }

    public TVReciteWords(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TVReciteWords(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        //初始化paint
        mPaint.setTextSize(15);
        mPaint.setColor(Color.BLACK);
        //初始化每行的间隔
        mLineInterval = 10;
        setWillNotDraw(false);

    }

    public void setTextColor(int mTextColor) {
        mPaint.setColor(mTextColor);
        invalidate();
    }

    public void setTextSizePX(float mTextSize) {
        mPaint.setTextSize(mTextSize);
        requestLayout();
        invalidate();
    }

    public void setTextSizeSP(float mTextSize) {
        mPaint.setTextSize(DisplayUtil.sp2px(getContext(), mTextSize));
        requestLayout();
        invalidate();
    }

    public float getTextSize() {
        return mPaint.getTextSize();
    }

    public float getTextSizeSP() {
        return DisplayUtil.px2sp(getContext(), mPaint.getTextSize());
    }

    public void setText(String content, String keyWords) {
        mContent = content;
        mKeyWords = keyWords;
        requestLayout();
        invalidate();
    }

    public void setLineInterval(int interval) {
        mLineInterval = interval;
        invalidate();
    }

    public int getLineInterval() {
        return mLineInterval;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    //测量高度
    //测量高度要在测量宽度之后,因为测量宽度的时候会计算总共有多少行
    private int measureHeight(int heightMeasureSpec) {
        if (mContent == null || mContent.equals("")) {
            return 0;
        }
        int result = 0;
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);

        if (specMode == MeasureSpec.EXACTLY) {  //如果是exactly 则按父类给的大小来设置
            result = specSize;
        } else { //如果是at_most或者UNSPECIFIED,则自己计算高度
            int lineHeight = (int) getTextSize();
            if (mLines > 0) {
                result = (lineHeight + mLineInterval) * mLines + mLineInterval;
            } else {
                result = 0;
            }
        }
        return result;
    }

    //测量宽度
    private int measureWidth(int widthMeasureSpec) {
        if (mContent == null || mContent.equals("")) {
            return 0;
        }
        int result = 0;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);

        //计算每个字符的宽度,并把每个字符的宽度放入一个浮点数数组中
        float[] widths = new float[mContent.length()];
        int totalWidthCount = mPaint.getTextWidths(mContent, widths);
        int totalWidth = 0;
        for (int i = 0; i < totalWidthCount; i++) {
            totalWidth += widths[i];
        }

        result = specSize;
        if (specMode != MeasureSpec.EXACTLY) {
            if (totalWidth < specSize) {
                result = totalWidth;
            } else {
                result = specSize;
            }
        }

        //下面要计算总共有多少行了!,很关键!
        //默认情况下狂赌是specSize
        //每次计算前先清零

        mLines = 1;
        int lineWidth = 0;


        //计算空格的宽度
        float[] tempVariable = new float[2];
        int spaceWidthCount = mPaint.getTextWidths(" ", tempVariable);
        int spaceWidth = 0;
        for (int i = 0; i < spaceWidthCount; i++) {
            spaceWidth += tempVariable[i];
        }


        spiltWords = mContent.split(" ");
        for (int i = 0; i < spiltWords.length; i++) {

            //计算每个单词的宽度
            float[] tempArray = new float[spiltWords[i].length()];
            int wordWidthCount = mPaint.getTextWidths(spiltWords[i], tempArray);
            int wordWidth = 0;
            for (int j = 0; j < wordWidthCount; j++) {
                wordWidth += tempArray[j];
            }

            if (lineWidth + wordWidth + spaceWidth <= result) { //如果之前的宽度+单词的宽度+空格 比控件宽度小
                lineWidth = lineWidth + wordWidth + spaceWidth;
            } else if (lineWidth + wordWidth <= result) {      //如果之前的宽度+单词的宽度 比控件宽度小
                lineWidth = lineWidth + wordWidth;
            } else {      //如果是其他情况,那么肯定需要换行了!
                mLines += 1;
                lineWidth = wordWidth;
            }
        }
        return result;
    }


    @Override
    protected void onDraw(Canvas canvas) {

        if (mContent == null || mContent.equals("")) {
            return;
        }

        canvas.save();

        //当前绘制文字的X坐标
        int lineWidth = 0;
        //当前绘制文字的Y坐标
        int lineHeight;

        //空格的宽度
        float[] tempVariable = new float[2];
        int spaceWidthCount = mPaint.getTextWidths(" ", tempVariable);
        int spaceWidth = 0;
        for (int i = 0; i < spaceWidthCount; i++) {
            spaceWidth += tempVariable[i];
        }


        //第一行的高度Y
        lineHeight = (int) getTextSize() + mLineInterval;
        //默认不是keywords,默认keywords是第一个,只把第一个keyword的空留下,后面的就不管了照样显示
        boolean isKeyWords = false;
        boolean isFirstKeyWords = true;

        //分别绘制每个单词
        for (int i = 0; i < spiltWords.length; i++) {

            //如果目前绘制的单词就是keyword,设置个flag
            if (spiltWords[i].equals(mKeyWords)) {
                isKeyWords = true;
            }

            //tempArray用来存数每个字符的宽度
            float[] tempArray = new float[spiltWords[i].length()];
            /**
             * mPaint.getTextWidths(spiltWords[i], tempArray)方法会把每个字的宽度写入tempArray中
             * 并返回文字的个数
             */
            int wordWidthCount = mPaint.getTextWidths(spiltWords[i], tempArray);
            int wordWidth = 0;
            //把每个字的宽度相加,得到这个单词的宽度
            for (int j = 0; j < wordWidthCount; j++) {
                wordWidth += tempArray[j];
            }


            if (lineWidth + wordWidth + spaceWidth <= getMeasuredWidth()) {

                //如果是keyword,则记录下位置信息,且不用draw
                if (isKeyWords && isFirstKeyWords) {
                    keyWordsX = lineWidth;
                    keyWordsY = lineHeight;
                    isFirstKeyWords = false;
                    isKeyWords = false;
                    keyWordWidth = wordWidth;
                } else {
                    //如果之前的宽度+单词的宽度+空格 比控件宽度小 直接接在后面绘制
                    canvas.drawText(spiltWords[i] + " ", lineWidth, lineHeight, mPaint);
                }
                //绘制完成后重新设置X坐标
                lineWidth = lineWidth + wordWidth + spaceWidth;

            } else if (lineWidth + wordWidth <= getMeasuredWidth()) {
                //如果是keyword,则记录下位置信息
                if (isKeyWords && isFirstKeyWords) {
                    keyWordsX = lineWidth;
                    keyWordsY = lineHeight;
                    isFirstKeyWords = false;
                    isKeyWords = false;
                    keyWordWidth = wordWidth;
                } else {
                    //如果之前的宽度+单词的宽度 比控件宽度小 直接在后面绘制
                    canvas.drawText(spiltWords[i], lineWidth, lineHeight, mPaint);
                }
                //绘制完成后重新设置X坐标
                lineWidth = lineWidth + wordWidth;
            } else {
                //如果是其他情况,那么肯定需要换行了!
                //换行的话要重新定位高度
                //然后再绘制
                lineHeight += ((int) getTextSize() + mLineInterval);
                mLines += 1;
                lineWidth = 0;

                //如果是keyword,则记录下位置信息
                if (isKeyWords && isFirstKeyWords) {
                    keyWordsX = lineWidth;
                    keyWordsY = lineHeight;
                    isFirstKeyWords = false;
                    isKeyWords = false;
                    keyWordWidth = wordWidth;
                } else {
                    canvas.drawText(spiltWords[i] + " ", lineWidth, lineHeight, mPaint);
                }
                //绘制完成后重新设置X坐标
                lineWidth = wordWidth + spaceWidth;
            }
        }
        onKeyWordHasBeenDrawn(keyWordsX, getKeyWordMarginTop(), keyWordWidth, getTextSize());
        canvas.restore();
    }

    OnKeyWordHasBeenDrawnListener l;

    private void onKeyWordHasBeenDrawn(int keyWordsMarginLeft, int keyWordsMarginTop, int keyWordWidth, float textSize) {
        if (l != null) {
            l.onKeyWordHasBeenDrawn(keyWordsMarginLeft, keyWordsMarginTop, keyWordWidth, textSize);
        }
    }

    public void setOnKeyWordHasBeenDrawnListener(OnKeyWordHasBeenDrawnListener l) {
        this.l = l;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }


    interface OnKeyWordHasBeenDrawnListener {
        void onKeyWordHasBeenDrawn(int keyWordsMarginLeft, int keyWordsMarginTop, int keyWordWidth, float textSize);
    }
}
