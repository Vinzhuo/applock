package com.drinker.applock.applist.view;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.drinker.applock.R;


/**
 */
public class DividerDecoration extends RecyclerView.ItemDecoration {

    private Paint paint;

    private int strokeWidth;

    private int leftStart;

    private int rightStart;

    private DividerTailor tailor;

    public interface DividerTailor {
        boolean ignoreDivider(RecyclerView parent, View child);
    }

    public DividerDecoration(Resources resources) {
        strokeWidth = (int) Math.ceil(resources.getDimension(R.dimen.item_divider_height));
//        leftStart = (int) Math.ceil(resources.getDimension(R.dimen.app_list_icon_size) +
//                2 * resources.getDimension(R.dimen.setting_item_edge_element_margin));
        leftStart = (int)resources.getDimension(R.dimen.setting_item_edge_element_margin);
        rightStart = leftStart;
        leftStart = leftStart + resources.getDimensionPixelSize(R.dimen.icon_width);
        paint = new Paint();
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(resources.getColor(R.color.color_33000000));
    }

    public void setDividerTailor(DividerTailor tailor) {
        this.tailor = tailor;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(0, 0, 0, strokeWidth);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        drawVertical(c, parent);
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft() + leftStart;
        final int right = parent.getWidth() - parent.getPaddingRight() - rightStart;

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View child = parent.getChildAt(i);
            if (tailor != null && tailor.ignoreDivider(parent, child)) {
                continue;
            }
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getBottom() + params.bottomMargin;
            int bottom = top;
            c.drawLine(left, bottom, right, bottom, paint);
        }
    }

}
