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

import java.util.Arrays;
import java.util.logging.Logger;

import org.twsu.lightpaint.config.SlitScanConfig;

import processing.core.PConstants;
import processing.core.PImage;
import toxi.color.ReadonlyTColor;
import toxi.gui.GUIElement;
import toxi.gui.Range;
import artnet4j.DmxUniverse;

public class SlitScanner implements Runnable {

    protected static final Logger logger = Logger.getLogger(SlitScanner.class
            .getSimpleName());

    private final SlitScanConfig config;
    private final ArtNetManager artnet;

    @GUIElement(label = "brightness", x = 20, y = 200)
    @Range(min = 0, max = 1)
    public float brightness = 0.66f;

    @GUIElement(label = "speed", x = 20, y = 240)
    @Range(min = 0.1f, max = 4)
    public float speed = 1;

    private PImage img;
    private PImage newImg;

    private DmxUniverse dmx;
    private float scanPos;

    private int sequenceID;
    private boolean isActive = true;

    public SlitScanner(SlitScanConfig config, ArtNetManager artnet) {
        this.config = config;
        this.artnet = artnet;
        this.img = new PImage((int) (config.height * 1.333f), config.height,
                PConstants.ARGB);
    }

    public int getCurrentPos() {
        return (int) scanPos;
    }

    public PImage getImage() {
        return img;
    }

    public int getImageHeight() {
        return config.height;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("scanner");
        logger.info("slitscanner running...");
        try {
            int interval = Math.round(1000f / config.dmxUpdateFPS);
            long previousTime = System.nanoTime();
            while (isActive) {
                double timePassed = (System.nanoTime() - previousTime) * 1.0e-6;
                while (timePassed < interval) {
                    timePassed = (System.nanoTime() - previousTime) * 1.0e-6;
                }
                if (newImg != null) {
                    updateImage();
                    newImg = null;
                }
                if (img != null && dmx != null) {
                    sendNextColumn();
                }
                long delay = (long) (interval - (System.nanoTime() - previousTime) * 1.0e-6);
                previousTime = System.nanoTime();
                if (delay > 0) {
                    Thread.sleep(delay);
                }
            }
        } catch (InterruptedException e) {
            logger.warning("slitscanner thread interrupted...");
        }
        logger.info("slitscanner stopped.");
    }

    private void sendNextColumn() {
        scanPos = (scanPos + speed) % img.width;
        // channel 0 = smoothing channel on light strip
        dmx.setChannel(0, 0);
        for (int idx = (int) scanPos + (img.height - 1) * img.width, dmxOffset = 1, y = 0; y < img.height; y++, idx -= img.width) {
            int col = img.pixels[idx];
            dmx.setChannel(dmxOffset++,
                    (byte) ((col >> 16 & 0xff) * brightness));
            dmx.setChannel(dmxOffset++, (byte) ((col >> 8 & 0xff) * brightness));
            dmx.setChannel(dmxOffset++, (byte) ((col & 0xff) * brightness));
        }
        artnet.updateUniverses(sequenceID++, true);
    }

    public void setDefaultImageColor(ReadonlyTColor col) {
        Arrays.fill(img.pixels, col.toARGB());
        img.updatePixels();
    }

    public void setDMXUniverse(DmxUniverse dmx) {
        this.dmx = dmx;
    }

    public void setImage(PImage img) {
        logger.info("setting new slit scan image");
        this.newImg = img;
    }

    public void shutdown() {
        isActive = false;
    }

    private void updateImage() {
        scanPos = 0;
        float scale = (float) config.height / newImg.height;
        this.img = new PImage((int) (newImg.width * scale),
                (int) (newImg.height * scale));
        img.copy(newImg, 0, 0, newImg.width, newImg.height, 0, 0, img.width,
                img.height);
        logger.info("activated new image");
    }
}
