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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.twsu.lightpaint.LightPaintApp;
import org.twsu.lightpaint.TextListItem;

import toxi.data.feeds.AtomEntry;
import toxi.data.feeds.AtomFeed;
import toxi.geom.Vec2D;

public class TwitterSearchState extends AppState {

    private static final String TWITTER_URL = "http://search.twitter.com/search.atom?q=%s&rpp=%d";

    private static final int ITEM_HEIGHT = 30;
    private static final int MAX_ITEMS = 25;

    private LightPaintApp app;
    private List<TextListItem> items = new ArrayList<TextListItem>();

    private boolean requestQueryInput;

    private String query;

    @Override
    public void enter(LightPaintApp app) {
        this.app = app;
        items.clear();
        requestQueryInput = true;
        query = null;
        setStatus("waiting for user input...");
    }

    private void executeSearch(String query) {
        try {
            String url = String.format(TWITTER_URL,
                    URLEncoder.encode(query, "UTF-8"), MAX_ITEMS);
            logger.info("executing twitter search: " + url);
            AtomFeed feed = AtomFeed.newFromURL(url);
            if (feed != null) {
                int gridY = 60;
                for (AtomEntry e : feed) {
                    TextListItem item = new TextListItem(e.title, 200, gridY,
                            app.width - 240, ITEM_HEIGHT);
                    items.add(item);
                    gridY += ITEM_HEIGHT;
                }
                setStatus("search complete: " + items.size() + " results");
            } else {
                setStatus("failed to load twitter search results.");
            }
        } catch (UnsupportedEncodingException e) {
            logger.warning("error encoding search query");
        }
    }

    @Override
    public void mouseMoved(LightPaintApp app) {
        Vec2D mousePos = new Vec2D(app.mouseX, app.mouseY);
        for (TextListItem i : items) {
            i.isRollOver(mousePos);
        }
    }

    @Override
    public void mousePressed(LightPaintApp app) {
        Vec2D mousePos = new Vec2D(app.mouseX, app.mouseY);
        for (TextListItem i : items) {
            if (i.isRollOver(mousePos)) {
                app.setSlitScanText(i.getContent());
            }
        }
    }

    @Override
    public void transition(LightPaintApp app) {
    }

    @Override
    public void update(LightPaintApp app) {
        app.writeHeader(query != null ? "Twitter results for: " + query
                : "Twitter search");
        if (requestQueryInput) {
            requestQueryInput = false;
            query = JOptionPane
                    .showInputDialog("Please enter your Twitter search query");
            if (query != null) {
                logger.info("entered query: " + query);
                executeSearch(query);
            }
        } else {
            app.useBodyFont();
            for (TextListItem i : items) {
                i.draw(app);
            }
        }
    }
}
