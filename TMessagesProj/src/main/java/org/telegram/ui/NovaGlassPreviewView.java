package org.telegram.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;

/**
 * Preview of the bottom tab bar with NOVA liquid glass. It is an approximation drawn with plain canvas:
 * the real refraction runs in the glass shader on the tab bar itself.
 */
public class NovaGlassPreviewView extends View {
    private static final int[] BACKDROP = {
        0xFF04110F, 0xFF0F6F6A, 0xFF14B8A6, 0xFFB9F26B, 0xFF1E6F8A
    };

    private final RectF card = new RectF();
    private final RectF pill = new RectF();
    private final RectF selected = new RectF();
    private final Paint backdropPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private boolean glassEnabled = true;
    private int intensity = 50;
    private int selectedIndex = 2;

    public NovaGlassPreviewView(Context context) {
        super(context);
        iconPaint.setColor(0xFFFFFFFF);
        rimPaint.setStyle(Paint.Style.STROKE);
        rimPaint.setStrokeWidth(AndroidUtilities.dp(1));
    }

    public void setState(boolean enabled, int intensity) {
        if (glassEnabled == enabled && this.intensity == intensity) {
            return;
        }
        glassEnabled = enabled;
        this.intensity = Math.max(0, Math.min(100, intensity));
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        backdropPaint.setShader(new LinearGradient(0, 0, w, h, BACKDROP, null, Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float w = getWidth();
        final float h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        final float inset = AndroidUtilities.dp(12);
        final float radius = AndroidUtilities.dp(16);

        // backdrop: the colours the glass refracts; a few shapes make the distortion visible
        card.set(0, 0, w, h);
        canvas.drawRoundRect(card, radius, radius, backdropPaint);
        fillPaint.setColor(0x33FFFFFF);
        canvas.drawCircle(w * 0.2f, h * 0.25f, AndroidUtilities.dp(26), fillPaint);
        canvas.drawCircle(w * 0.78f, h * 0.2f, AndroidUtilities.dp(18), fillPaint);

        final float pillHeight = AndroidUtilities.dp(60);
        pill.set(inset, h - inset - pillHeight, w - inset, h - inset);
        final float pillRadius = pillHeight / 2f;
        final float k = intensity / 100f;

        if (glassEnabled) {
            // translucent body: stronger intensity = more tint and a brighter body
            fillPaint.setColor(Color.argb((int) (255 * (0.14f + 0.22f * k)), 255, 255, 255));
            canvas.drawRoundRect(pill, pillRadius, pillRadius, fillPaint);

            // specular top highlight, scaled by intensity
            highlightPaint.setShader(new LinearGradient(0, pill.top, 0, pill.centerY(),
                Color.argb((int) (255 * 0.45f * k), 255, 255, 255), 0x00FFFFFF, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(pill, pillRadius, pillRadius, highlightPaint);

            // rim: bright on top, a little darker at the bottom
            rimPaint.setShader(new LinearGradient(0, pill.top, 0, pill.bottom,
                0x88FFFFFF, 0x33FFFFFF, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(pill, pillRadius, pillRadius, rimPaint);
        } else {
            // plain frosted bar, as with liquid glass turned off
            fillPaint.setColor(0xE61A2630);
            canvas.drawRoundRect(pill, pillRadius, pillRadius, fillPaint);
        }

        // four tab marks and the selected tab
        final float slot = pill.width() / 4f;
        final float selectedInset = AndroidUtilities.dp(6);
        selected.set(pill.left + slot * selectedIndex + selectedInset, pill.top + selectedInset,
            pill.left + slot * (selectedIndex + 1) - selectedInset, pill.bottom - selectedInset);
        selectedPaint.setColor(glassEnabled ? Color.argb((int) (255 * 0.18f), 255, 255, 255) : 0x332B9CFF);
        canvas.drawRoundRect(selected, selected.height() / 2f, selected.height() / 2f, selectedPaint);

        final float iconRadius = AndroidUtilities.dp(9);
        for (int i = 0; i < 4; i++) {
            iconPaint.setColor(i == selectedIndex ? 0xFF2B9CFF : 0xCCFFFFFF);
            canvas.drawCircle(pill.left + slot * i + slot / 2f, pill.centerY(), iconRadius, iconPaint);
        }
    }
}
