/*
 * .____    .__       .__     __ __________        .__        __   
 * |    |   |__| ____ |  |___/  |\______   \_____  |__| _____/  |_ 
 * |    |   |  |/ ___\|  |  \   __\     ___/\__  \ |  |/    \   __\
 * |    |___|  / /_/  >   Y  \  | |    |     / __ \|  |   |  \  |  
 * |_______ \__\___  /|___|  /__| |____|    (____  /__|___|  /__|  
 *         \/ /_____/      \/                    \/        \/ 
 *         
 *  By Technology Will Save Us
 *  
 *  (c) 2011 Karsten Schmidt <k at technologywillsaveus.org>
 *  
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * http://opensource.org/licenses/gpl-3.0.html
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 */
package org.twsu.lightpaint;

import java.util.logging.Logger;

import org.twsu.lightpaint.config.RenderConfig;

import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import toxi.math.MathUtils;

public class TextRenderer {

    protected static final Logger logger = Logger.getLogger(TextRenderer.class
            .getSimpleName());

    private LightPaintApp app;
    private RenderConfig config;
    private PFont font;

    public TextRenderer(LightPaintApp app, RenderConfig config) {
        this.app = app;
        this.config = config;
        this.font = app.loadFont(config.fontPath);
    }

    public PImage getRenderedText(String txt) {
        logger.info("rendering bitmap msg: " + txt);
        app.textFont(font);
        int ih = app.getScanner().getImageHeight();
        float tw = app.textWidth(txt);
        int iw = MathUtils.min((int) tw / 2 + 20, config.maxWidth);
        PGraphics gfx = app.createGraphics(iw, ih, PConstants.JAVA2D);
        gfx.beginDraw();
        gfx.background(config.bgCol.toARGB());
        gfx.textFont(font);
        gfx.fill(config.txtCol.toARGB());
        gfx.text(txt, 10, font.getSize(), iw - 20, ih);
        gfx.endDraw();
        return gfx;
    }
}
