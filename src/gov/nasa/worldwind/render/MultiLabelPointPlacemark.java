/*
 * Copyright (C) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.OGLTextRenderer;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * a PointPlacemark that supports a variable number of left-aligned sub-labels
 *
 * this is an alternative for PointPlacemarks with associated GlobeAnnotations. It does not provide the full
 * capabilities of a MultiLineTextRenderer but can optionally set a different font for the sub labels.
 *
 * The use case for MultiLabelPointPlacemark is a large number of dynamically updated objects that frequently change
 * with respect to positions and single sub-labels in a context where we can not tolerate delays between image and
 * label rendering of the same object.
 *
 * MultiLabelPointPlacemark also tries to address the problem that some label colors require excessively large
 * or fat fonts to be readable against a multi-colored green/brown map background. Instead of using a fill color,
 * we draw strings with a lighter and a black backdrop. It is therefore recommended to use label PLAIN label fonts
 *
 * Created by pcmehlitz on 7/1/17.
 */
public class MultiLabelPointPlacemark extends PointPlacemark {

    ArrayList<String> subLabels = new ArrayList<String>(4);
    Font subFont = null;

    float labelHeight = 0;  // computed on first use
    float subHeight = 0; // computed on first use

    public MultiLabelPointPlacemark(Position position) {
        super(position);
    }

    public void setSubLabelFont(Font font) {
        subFont = font;
    }

    public void addSubLabelText (String text) {
        subLabels.add(text);
    }

    public void setSubLabelText (int idx, String text) {
        if (text == null) {
            subLabels.remove(idx);
        } else {
            subLabels.set(idx, text);
        }
    }

    public void removeAllLabels() {
        setLabelText(null);
        removeSubLabels();
    }

    public void removeSubLabels() {
        subLabels.clear();
    }

    public void removeSubLabelText (int idx) {
        subLabels.remove(idx);
    }

    public boolean hasLabel() {
        return labelText != null;
    }
    public boolean hasSubLabels() {
        return !subLabels.isEmpty();
    }

    @Override
    protected void renderLabelText(DrawContext dc, String labelText, float x, float y, Font font, Color color, Color backgroundColor) {
        TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), font);
        TextRenderer subRenderer = null;

        if (!subLabels.isEmpty()) {
            if (subFont!= null) {
                subRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), subFont);
            }
            if (labelHeight == 0) {
                labelHeight = getLineHeight(textRenderer);
            }
            if (subHeight == 0 && subLabels.size() > 1){
                subHeight = (subRenderer != null) ? getLineHeight(subRenderer) : labelHeight;
            }
        }

        try {
            textRenderer.begin3DRendering();
            drawString(labelText, textRenderer, x, y, color, backgroundColor);
            if (!subLabels.isEmpty() && subRenderer == null) drawSubLabels(textRenderer, x, y, color, backgroundColor);
        } finally {
            textRenderer.end3DRendering();
        }

        if (subRenderer != null) {
            try {
                subRenderer.begin3DRendering();
                drawSubLabels(subRenderer, x, y, color, backgroundColor);
            } finally {
                subRenderer.end3DRendering();
            }
        }
    }

    // Color.brighter() only multiplies, i.e. does not change colors that have 0 or 255 component values
    static Color lighter (Color clr) {
        int shift = 127;
        int r = Math.min(255,clr.getRed() + shift);
        int g = Math.min(255,clr.getGreen() + shift);
        int b = Math.min(255,clr.getBlue() + shift);
        return new Color(r,g,b);
    }

    // avoid tons of redundant color objects
    static HashMap<Color,Color> lightColors = new HashMap<Color,Color>();
    static Color lastColor = null;
    static Color lastLightColor = null;

    // we are going to render a lot of objects of the same color so cache the result and save redundant lookups
    static Color getLightColor(Color color) {
        if (color == lastColor) {
            return lastLightColor;
        } else {
            Color lightColor = lightColors.get(color);
            if (lightColor == null) {
                lightColor = lighter(color);
                lightColors.put(color, lightColor);
            }
            lastColor = color;
            lastLightColor = lightColor;
            return lightColor;
        }
    }


    /**
     * we use a different rendering scheme here than our base class, which only uses the label color on a
     * transparent black backdrop. While this works for "light" colors such as yellow and cyan, it does not
     * work on map background for "dark" colors such as blue and red. Instead of filling the background we use
     * a slightly more expensive approach and render the string three times, with both a lighter and a darker
     * backdrop. We compute and cache the lighter color since high contrast such as white is disturbing, but we
     * always use opaque black as the dark backdrop. This seems to be the best compromise across all label colors, but
     * since labels appear more "fat" is also works best with PLAIN fonts
     */
    protected void drawString (String text, TextRenderer textRenderer, float x, float y, Color color, Color backgroundColor){
        textRenderer.setColor(getLightColor(color));
        textRenderer.draw3D(text, x - 1, y + 1, 0, 1);

        textRenderer.setColor(Color.BLACK);  // instead of transparent backgroundColor
        textRenderer.draw3D(text, x + 1, y - 1, 0, 1);

        textRenderer.setColor(color);
        textRenderer.draw3D(text, x, y, 0, 1);
    }

    protected void drawSubLabels (TextRenderer textRenderer, float x, float y, Color color, Color backgroundColor){
        y -= labelHeight;
        for (String sub: subLabels){
            drawString(sub, textRenderer, x, y, color, backgroundColor);
            y -= subHeight;
        }
    }

    protected float getLineHeight (TextRenderer textRenderer) {
        Rectangle2D maxCharBounds = textRenderer.getFont().getMaxCharBounds(textRenderer.getFontRenderContext());
        return (float)maxCharBounds.getHeight();
    }
}
