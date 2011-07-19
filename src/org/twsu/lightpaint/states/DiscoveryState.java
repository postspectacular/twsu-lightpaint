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
package org.twsu.lightpaint.states;

import org.twsu.lightpaint.LightPaintApp;

import processing.core.PConstants;
import toxi.color.TColor;

public class DiscoveryState extends AppState {

    public DiscoveryState() {
    }

    @Override
    public String getStatus() {
        return "discovering DMX nodes...";
    }

    @Override
    public void transition(LightPaintApp app) {
        app.setState(LightPaintApp.STATE_STARTUP);
    }

    @Override
    public void update(LightPaintApp app) {
        app.fill(TColor.WHITE.toARGB());
        app.useBodyFont();
        app.textAlign(PConstants.CENTER);
        app.text(getStatus(), app.width / 2, app.height / 2);
    }
}
