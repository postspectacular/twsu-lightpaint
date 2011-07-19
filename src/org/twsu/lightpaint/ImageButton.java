/*
 * .____    .__       .__   import processing.core.PImage;
import toxi.color.TColor;
import toxi.geom.Rect;
import toxi.geom.Vec2D;
import toxi.math.MathUtils;
 |  \   __\     ___/\__  \ |  |/    \   __\
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

import processing.core.PImage;
import toxi.color.TColor;
import toxi.geom.Rect;
import toxi.geom.Vec2D;

public class ImageButton extends Rect {

    private static HighlightModulator highlighter = new HighlightModulator(
            TColor.WHITE, TColor.MAGENTA);

    private PImage img;
    private PImage thumb;

    private boolean isRollOver;

    public ImageButton(int gridX, int gridY, PImage img, PImage thumb) {
        super(gridX, gridY, thumb.width, thumb.height);
        this.img = img;
        this.thumb = thumb;
    }

    public void draw(LightPaintApp app) {
        if (isRollOver) {
            app.tint(highlighter.updateColor().toARGB());
        } else {
            app.noTint();
        }
        app.image(thumb, x, y);
    }

    public PImage getImage() {
        return img != null ? img : thumb;
    }

    public boolean rollOver(Vec2D mousePos) {
        return isRollOver = containsPoint(mousePos);
    }
}
