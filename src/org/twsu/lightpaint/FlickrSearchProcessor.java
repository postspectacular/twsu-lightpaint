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

import java.awt.color.CMMException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import org.twsu.lightpaint.config.FlickrConfig;

import processing.core.PConstants;
import processing.core.PImage;
import toxi.util.events.EventDispatcher;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.PhotosInterface;
import com.aetrion.flickr.photos.SearchParameters;

public class FlickrSearchProcessor implements Runnable {

    protected static final Logger logger = Logger
            .getLogger(FlickrSearchProcessor.class.getSimpleName());

    private static Flickr flickr;

    private final FlickrConfig config;
    private final SearchParameters params;

    private boolean isActive = true;

    public final EventDispatcher<FlickrListener> listeners = new EventDispatcher<FlickrListener>();

    public FlickrSearchProcessor(FlickrConfig config, SearchParameters params) {
        this.config = config;
        this.params = params;
    }

    public void cancel() {
        isActive = false;
    }

    private PImage loadImage(String url) {
        try {
            BufferedImage bufImg = ImageIO.read(new URL(url));
            if (bufImg != null) {
                PImage img = new PImage(bufImg.getWidth(), bufImg.getHeight(),
                        PConstants.ARGB);
                bufImg.getRGB(0, 0, img.width, img.height, img.pixels, 0,
                        img.width);
                img.updatePixels();
                return img;
            }
        } catch (MalformedURLException e) {
            logger.warning("invalid flickr url: " + url);
        } catch (IOException e) {
            logger.warning("error loading flickr image: " + url);
        } catch (CMMException e) {
            logger.warning("invalid image format: " + url);
        }
        return null;
    }

    private void notifySearchFailed(Exception e) {
        for (FlickrListener l : listeners) {
            l.flickSearchFailed(e);
        }
    }

    public void run() {
        Thread.currentThread().setName("flickrsearch");
        logger.info("executing flickr search...");
        if (flickr == null) {
            try {
                flickr = new Flickr(config.apikey, new REST());
            } catch (ParserConfigurationException e) {
                logger.log(Level.SEVERE, "couldn't create flickr client:", e);
                return;
            }
        }
        PhotosInterface photoAPI = flickr.getPhotosInterface();
        PhotoList results;
        try {
            results = photoAPI.search(params, config.maxImages, 1);
            logger.info(results.size() + " images loaded");
            for (Object r : results) {
                Photo photo = (Photo) r;
                PImage small = loadImage(photo.getSmallUrl());
                PImage thumb = loadImage(photo.getSmallSquareUrl());
                if (small != null && thumb != null) {
                    for (FlickrListener l : listeners) {
                        l.newFlickrImage(photo.getSmallUrl(), small, thumb);
                    }
                }
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                }
                if (!isActive) {
                    return;
                }
            }
            for (FlickrListener l : listeners) {
                l.flickSearchComplete();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "IO error executing flickr search", e);
            notifySearchFailed(e);
        }
    }
}
