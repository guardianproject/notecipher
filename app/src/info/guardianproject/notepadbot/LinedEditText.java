package info.guardianproject.notepadbot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * A custom EditText that draws lines between each line of text that is displayed.
 */
public class LinedEditText extends EditText {
    private Rect mRect;
    private Paint mPaint;
    private final static String lineColor = "#cccccc";

    // we need this constructor for LayoutInflater
    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.parseColor(lineColor));
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        int height = canvas.getHeight();
        int curHeight = 0;
        int baseline = getLineBounds(0, mRect);
        for (curHeight = baseline + 3; curHeight < height; curHeight += getLineHeight()) {
            canvas.drawLine(mRect.left, curHeight, mRect.right, curHeight, mPaint);
        }
        super.onDraw(canvas);
    }
}