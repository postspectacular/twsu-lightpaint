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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JOptionPane;

import org.twsu.lightpaint.FlickrListener;
import org.twsu.lightpaint.FlickrSearchProcessor;
import org.twsu.lightpaint.ImageButton;
import org.twsu.lightpaint.LightPaintApp;
import org.twsu.lightpaint.config.FlickrConfig;

import processing.core.PImage;
import toxi.geom.Vec2D;

import com.aetrion.flickr.photos.SearchParameters;

public class FlickrSearchState extends AppState implements FlickrListener {

    private static final int GRID_HEIGHT = 180;

    private FlickrConfig flickrConfig;
    private FlickrSearchProcessor flickr;
    private LightPaintApp app;

    private ConcurrentLinkedQueue<ImageButton> imageQueue = new ConcurrentLinkedQueue<ImageButton>();
    private List<ImageButton> images = new ArrayList<ImageButton>();

    private String query;

    private int gridX, gridY;

    private boolean requestQueryInput;

    @Override
    public void enter(LightPaintApp app) {
        this.app = app;
        flickrConfig = LightPaintApp.config.flickr;
        if (flickr != null) {
            flickr.cancel();
        }
        flickr = null;
        imageQueue.clear();
        images.clear();
        gridX = 200;
        gridY = 60;
        query = null;
        requestQueryInput = true;
        setStatus("waiting for user input");
    }

    private void executeSearch(String queryTerms) {
        imageQueue.clear();
        SearchParameters params = new SearchParameters();
        params.setTags(queryTerms.split("\\s"));
        params.setTagMode("all");
        flickr = new FlickrSearchProcessor(flickrConfig, params);
        flickr.listeners.addListener(this);
        new Thread(flickr).start();
        setStatus("executing flickr search...");
    }

    @Override
    public void flickSearchComplete() {
        setStatus("search complete");
    }

    @Override
    public void flickSearchFailed(Exception e) {
        setStatus("search failed");
    }

    @Override
    public void leave(LightPaintApp app) {
        if (flickr != null) {
            flickr.cancel();
            flickr = null;
        }
        imageQueue.clear();
        images.clear();
    }

    @Override
    public void mouseMoved(LightPaintApp app) {
        Vec2D mousePos = new Vec2D(app.mouseX, app.mouseY);
        for (ImageButton img : images) {
            img.isRollOver(mousePos);
        }
    }

    @Override
    public void mousePressed(LightPaintApp app) {
        Vec2D mousePos = new Vec2D(app.mouseX, app.mouseY);
        for (ImageButton img : images) {
            if (img.isRollOver(mousePos)) {
                app.setSlitScanImage(img.getImage());
            }
        }
    }

    @Override
    public void newFlickrImage(String url, PImage img, PImage thumb) {
        ImageButton bt = new ImageButton(gridX, gridY, img, thumb);
        imageQueue.offer(bt);
        logger.info("image added to queue: " + url);
        gridX += bt.width;
        if (gridX > app.width - bt.width - 20) {
            gridX = 200;
            gridY += bt.height;
        }
    }

    @Override
    public void transition(LightPaintApp app) {
    }

    @Override
    public void update(LightPaintApp app) {
        app.writeHeader(query != null ? "Flickr results for " + query
                : "Flickr image search");
        if (requestQueryInput) {
            requestQueryInput = false;
            query = JOptionPane
                    .showInputDialog("Please list some tags to search on Flickr");
            if (query != null) {
                logger.info("entered query: " + query);
                executeSearch(query);
            }
        } else {
            while (imageQueue.peek() != null) {
                images.add(imageQueue.poll());
            }
            for (ImageButton img : images) {
                img.draw(app);
            }
        }
    }
}