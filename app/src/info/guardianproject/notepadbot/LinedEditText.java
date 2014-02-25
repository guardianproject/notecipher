package info.guardianproject.notepadbot;

import android.content.Context;
import android.graphics.Canvas;
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
    private boolean showLines;
    
    // we need this constructor for LayoutInflater
    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(getResources().getColor(R.color.gray));
        
        showLines = Settings.getNoteLinesOption(context);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        int height = canvas.getHeight();
        int curHeight = 0;
        int baseline = getLineBounds(0, mRect);
        if(showLines) {
	        for (curHeight = baseline + 3; curHeight < height; curHeight += getLineHeight()) {
	            canvas.drawLine(mRect.left, curHeight, mRect.right, curHeight, mPaint);
	        }
        }
        super.onDraw(canvas);
    }
}