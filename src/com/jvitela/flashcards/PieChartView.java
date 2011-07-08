package com.jvitela.flashcards;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class PieChartView extends View {
	private static final int	STROKE_WIDTH = 5;
	private static final int	SUCCESS_COLOR = 0xff6aa84f;
	private static final int	FAIL_COLOR = 0xffe06666;

    private Paint 	mFillPaintSuccess;
    private Paint 	mFillPaintFail;
    private Paint 	mStrokePaint;
    private RectF 	mFrame;
    private int		mSuccesses;
    private int		mFails;

    /**
     * Constructor.  This version is only needed if you will be instantiating
     * the object manually (not from a layout XML file).
     * @param context
     */
    public PieChartView(Context context) {
        super(context);
        initPieChart();
    }

    /**
     * Construct object, initializing with any attributes we understand from a
     * layout file. These attributes are defined in
     * SDK/assets/res/any/classes.xml.
     * 
     * @see android.view.View#View(android.content.Context, android.util.AttributeSet)
     */
    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPieChart();
    }

    /** 
     * Initialize internal values
     */
    private void initPieChart() {
    	mFillPaintSuccess = new Paint();
    	mFillPaintSuccess.setAntiAlias(true);
    	mFillPaintSuccess.setStyle(Paint.Style.FILL);
    	mFillPaintSuccess.setColor(SUCCESS_COLOR);

    	mFillPaintFail = new Paint();
    	mFillPaintFail.setAntiAlias(true);
    	mFillPaintFail.setStyle(Paint.Style.FILL);
    	mFillPaintFail.setColor(FAIL_COLOR);
        
        mStrokePaint = new Paint();
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(STROKE_WIDTH);
        mStrokePaint.setColor(Color.WHITE);

        mFrame = new RectF();
        mSuccesses = 50;
        mFails = 50;
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
    	//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	setMeasuredDimension( measure(widthMeasureSpec), measure(heightMeasureSpec) );
    }

    /**
     * Determines the width of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measure(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } 
        else {
            // Measure
            result = specSize/2;
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
    	mFrame.set(STROKE_WIDTH, STROKE_WIDTH, w-STROKE_WIDTH, h-STROKE_WIDTH);
    }

    @Override 
    protected void onDraw(Canvas canvas) {
    	float total = mSuccesses+mFails;
    	float angle1 = (mSuccesses*360/total);
        canvas.drawArc(mFrame, 0, angle1, true, mFillPaintSuccess);
        canvas.drawArc(mFrame, 0, angle1, true, mStrokePaint);

        float angle2 = (mFails*360/total);
        canvas.drawArc(mFrame, angle1, angle2, true, mFillPaintFail);
        canvas.drawArc(mFrame, angle1, angle2, true, mStrokePaint);
   }

    public void setValues( int successes, int fails) {
    	mSuccesses = successes;
    	mFails = fails;
    	invalidate();
    }
}
