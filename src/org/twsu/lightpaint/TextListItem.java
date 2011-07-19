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

import toxi.color.TColor;
import toxi.geom.Rect;
import toxi.geom.Vec2D;

public class TextListItem extends Rect {

    private static HighlightModulator highlighter = new HighlightModulator(
            TColor.newGray(0.2f), TColor.MAGENTA);

    private String content;

    private boolean isRollOver;

    public TextListItem(String content, int x, int y, int w, int h) {
        super(x, y, w, h);
        this.content = content;
    }

    public void draw(LightPaintApp app) {
        if (isRollOver) {
            app.fill(highlighter.updateColor().toARGB());
        } else {
            app.fill(highlighter.getOffColor().toARGB());
        }
        app.rect(x, y, width, height - 1);
        app.fill(255);
        app.text(content, x + 4, y + app.textAscent() - app.textDescent(),
                width - 8, height - 8);

    }

    public String getContent() {
        return content;
    }

    public boolean isRollOver(Vec2D mousePos) {
        return isRollOver = containsPoint(mousePos);
    }
}
