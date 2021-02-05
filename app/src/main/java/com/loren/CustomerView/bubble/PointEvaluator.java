package com.loren.CustomerView.bubble;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

public class PointEvaluator implements TypeEvaluator<PointF> {
    @Override
    public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
        //只要能保证：当fraction=0时返回值为startValue，并且当fraction=1时返回值为endValue，就是一个比较合理的函数
        float x = startValue.x + fraction * (endValue.x - startValue.x);
        float y = startValue.y + fraction * (endValue.y - startValue.y);
        return new PointF(x, y);
    }
}
