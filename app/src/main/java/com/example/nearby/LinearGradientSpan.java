package com.example.nearby;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

import androidx.annotation.ColorInt;

public class LinearGradientSpan extends CharacterStyle implements UpdateAppearance {
    private String containingText;
    private String textToStyle;
    @ColorInt
    private int startColorInt;
    @ColorInt
    private int endColorInt;

    public LinearGradientSpan(String containingText, String textToStyle, int startColorInt, int endColorInt) {
        this.containingText = containingText;
        this.textToStyle = textToStyle;
        this.startColorInt = startColorInt;
        this.endColorInt = endColorInt;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        if (tp == null) {
            return;
        }

        float leadingWidth = 0f;
        int indexOfTextToStyle = containingText.indexOf(textToStyle);
        if (!containingText.startsWith(textToStyle) && !containingText.equals(textToStyle)) {
            leadingWidth = tp.measureText(containingText, 0, indexOfTextToStyle);
        }
        float gradientWidth = tp.measureText(containingText, indexOfTextToStyle,
                indexOfTextToStyle + textToStyle.length());

        tp.setShader(new LinearGradient(
                leadingWidth,
                0f,
                leadingWidth + gradientWidth,
                0f,
                startColorInt,
                endColorInt,
                Shader.TileMode.REPEAT
        ));
    }
}
