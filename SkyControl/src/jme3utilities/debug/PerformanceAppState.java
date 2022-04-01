/*
 Copyright (c) 2014, Stephen Gold
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Stephen Gold's name may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL STEPHEN GOLD BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jme3utilities.debug;

import com.jme3.app.Application;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import java.util.logging.Logger;
import jme3utilities.Misc;
import jme3utilities.MyAsset;
import jme3utilities.SimpleAppState;

/**
 * App state which implements a performance monitor for jME3. Each second it
 * displays the duration of the longest update during the previous second.
 * <p>
 * Each instance is enabled at creation.
 *
 * @author Stephen Gold <sgold@sonic.net>
 */
public class PerformanceAppState
        extends SimpleAppState {
    // *************************************************************************
    // constants

    /**
     * message logger for this class
     */
    final private static Logger logger =
            Logger.getLogger(PerformanceAppState.class.getName());
    /**
     * asset path to the default font
     */
    final private static String fontPath = "Interface/Fonts/Default.fnt";
    // *************************************************************************
    // fields
    /**
     * text object to display statistics: set by initialize()
     */
    private BitmapText text = null;
    /**
     * color of background for statistics text
     */
    private ColorRGBA backgroundColor = new ColorRGBA(0f, 0f, 0f, 0.5f);
    /**
     * color of statistics text
     */
    private ColorRGBA textColor = ColorRGBA.White;
    /**
     * time remaining in the current measurement interval (in seconds)
     */
    private double secondsToNextUpdate = 0f;
    /**
     * largest time per frame observed during the current measurement interval
     * (in seconds)
     */
    private float maxTPF = 0f;
    /**
     * duration of the upcoming measurement interval (in seconds)
     */
    private float updateInterval = 1f;
    /*
     * background for statistics text: set by initialize()
     */
    private Geometry background;
    // *************************************************************************
    // SimpleAppState methods

    /**
     * Clean up this performance monitor on detach.
     */
    @Override
    public void cleanup() {
        super.cleanup();

        guiNode.detachChild(background);
        guiNode.detachChild(text);
    }

    /**
     * Initialize this performance monitor prior to its 1st update.
     *
     * @param stateManager (not null)
     * @param application (not null)
     */
    @Override
    public void initialize(AppStateManager stateManager,
            Application application) {
        super.initialize(stateManager, application);

        secondsToNextUpdate = updateInterval;
        /*
         * Create and attach a GUI text object to display statistics.
         */
        BitmapFont font = assetManager.loadFont(fontPath);
        text = new BitmapText(font);
        float lineHeight = text.getLineHeight();
        text.setColor(textColor);
        text.setLocalTranslation(0f, lineHeight, 0f);
        text.setText("(awaiting update)");
        guiNode.attachChild(text);
        /*
         * Create and attach a colored background for the display.
         */
        Material backgroudMaterial =
                MyAsset.createUnshadedMaterial(assetManager);
        backgroudMaterial.setColor("Color", backgroundColor);
        RenderState renderState = backgroudMaterial.getAdditionalRenderState();
        renderState.setBlendMode(RenderState.BlendMode.Alpha);
        float backgroundWidth = 250f; // pixels
        Quad quad = new Quad(backgroundWidth, lineHeight);
        background = new Geometry("perf stats background", quad);
        background.setMaterial(backgroudMaterial);
        background.setLocalTranslation(0f, 0f, -1f);
        guiNode.attachChild(background);
        /*
         * Detach any JME stats app state(s).
         */
        Misc.detachAll(stateManager, StatsAppState.class);
    }

    /**
     * Update the performance statistics.
     *
     * @param elapsedTime since previous frame/update (in seconds, &ge;0)
     */
    @Override
    public void update(float elapsedTime) {
        super.update(elapsedTime);

        maxTPF = Math.max(maxTPF, elapsedTime);

        secondsToNextUpdate -= elapsedTime;
        if (secondsToNextUpdate < 0.0) {
            float milliseconds = 1000f * maxTPF;
            String message = String.format("Max time per frame = %.1f msec",
                    milliseconds);
            text.setText(message);

            maxTPF = 0f;
            secondsToNextUpdate = updateInterval;
        }
    }
}