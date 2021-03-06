/*******************************************************************************
 * Copyright 2012 Steven Rudenko
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package shared.ui.actionscontentview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.widget.LinearLayout;

class ContentLayout extends LinearLayout implements BaseLayout {

  public interface OnSwipeListener {
    public void onSwipe(int scrollPosition);
  }

  private final EffectsController mEffectsController = new EffectsController();

  private final Rect mHitRect = new Rect();
  private final Paint mFadePaint = new Paint();

  private int mFadeFactor = 0;

  private OnSwipeListener mOnSwipeListener;

  public ContentLayout(Context context) {
    this(context, null);
  }

  public ContentLayout(Context context, AttributeSet attrs) {
    super(context, attrs);

    // we need to be sure we have horizontal layout to add shadow to left border
    setOrientation(LinearLayout.HORIZONTAL);
  }

  public ContentLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    // we need to be sure we have horizontal layout to add shadow to left border
    setOrientation(LinearLayout.HORIZONTAL);
  }

  public void setOnSwipeListener(OnSwipeListener listener) {
    mOnSwipeListener = listener;
  }

  @Override
  public void setEffects(Animation effects) {
    mEffectsController.setEffects(effects);
  }

  @Override
  public Animation getEffects() {
    return mEffectsController.getEffects();
  }

  @Override
  public void onScroll(float factor, int fadeFactor) {
    mFadeFactor = fadeFactor;
    if (mEffectsController.apply(factor) || mFadeFactor > 0)
      invalidate();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    // prevent ray cast of touch events to actions container
    getHitRect(mHitRect);
    mHitRect.offset(-getScrollX(), -getScrollY());
    if (mHitRect.contains((int)event.getX(), (int)event.getY())) {
      return true;
    }

    return super.onTouchEvent(event);
  }

  @Override
  protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    super.onScrollChanged(l, t, oldl, oldt);
    if (mOnSwipeListener != null)
      mOnSwipeListener.onSwipe(-getScrollX());
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    final int saveCount = canvas.save();
    canvas.concat(mEffectsController.getEffectsMatrix());
    canvas.saveLayerAlpha(0, 0, canvas.getWidth(), canvas.getHeight(), (int)(255 * mEffectsController.getEffectsAlpha()), Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);

    super.dispatchDraw(canvas);

    if (mFadeFactor > 0f) {
      mFadePaint.setColor(Color.argb(mFadeFactor, 0, 0, 0));
      canvas.drawRect(0, 0, getWidth(), getHeight(), mFadePaint);
    }

    canvas.restoreToCount(saveCount);
  }
}
