/*
 * .____    .__       .__     __ __________        .__        __   
 * |    |   |__| ____ |  |___/  |\______   \_____  |__| _____/  |_ 
 * |    |   |  |/ ___\|  |  \   __\     ___/\__  \ |  |/    \   __\
 * |    |___|  / /_/  >   Y  \  | |    |     / __ \|  |   |  \  |  
 * |_______ \__\___  /|___|  /__| |____|    (____  /__|___|  /__|  
 *         \/ /_____/      \/                    \/        \/ 
 *  
 *  Controller software for controlling the DMX LED strip module
 *  by Technology Will Save Us
 *  
 *  (c) 2011 Karsten Schmidt <k at technologywillsaveus.org>
 *  
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.twsu.lightpaint;

import java.util.ArrayList;
import java.util.List;

import org.twsu.lightpaint.states.AppState;
import org.twsu.lightpaint.states.DiscoveryState;
import org.twsu.lightpaint.states.FlickrSearchState;
import org.twsu.lightpaint.states.MainState;
import org.twsu.lightpaint.states.StartupState;
import org.twsu.lightpaint.states.TwitterSearchState;

import processing.core.PFont;
import processing.core.PImage;
import sojamo.drop.DropEvent;
import sojamo.drop.SDrop;
import toxi.color.TColor;
import toxi.gui.GUIManager;
import artnet4j.ArtNetNode;
import artnet4j.DmxUniverseConfig;
import baseapp.BaseApp;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Textlabel;

public class LightPaintApp extends BaseApp {

    public static final AppState STATE_DISCOVERY = new DiscoveryState();
    public static final AppState STATE_STARTUP = new StartupState();
    public static final AppState STATE_MAIN = new MainState();
    public static final AppState STATE_FLICKR = new FlickrSearchState();
    public static final AppState STATE_TWITTER = new TwitterSearchState();

    public static void main(String[] args) {
        BaseApp.main(args);
    }

    private ArtNetManager artnet;
    private AppState state;

    private ControlP5 cp5;
    private GUIManager gui;
    private SlitScanner scanner;

    private Textlabel uiStatus;
    private Textlabel uiCurrentImg;
    private PFont fontHD;
    private PFont fontBody;
    private TextRenderer txtRenderer;
    private SDrop sDrop;

    public void controlEvent(ControlEvent e) {
        String label = e.label();
        if (label.equalsIgnoreCase(MainState.BT_TWITTER)) {
            transitionToState(STATE_TWITTER);
        } else if (label.equalsIgnoreCase(MainState.BT_FLICKR)) {
            transitionToState(STATE_FLICKR);
        } else if (state != null) {
            state.controlEvent(this, e);
        }
    }

    public void draw() {
        super.updateWindowState(false);
        background(config.app.bgColor.toARGB());
        noStroke();
        noTint();
        textAlign(LEFT);
        if (state != null) {
            state.update(this);
            if (uiStatus != null) {
                uiStatus.setValue(state.getStatus());
            }
        }
        if (scanner != null) {
            PImage img = scanner.getImage();
            float scale = 160f / img.width;
            noTint();
            int y = (int) (height - 40 - img.height * scale);
            image(img, 20, y, img.width * scale, img.height * scale);
            stroke(255, 0, 255);
            float x = 20 + scanner.getCurrentPos() * scale;
            line(x, y, x, y + img.height * scale);
            uiCurrentImg.setPosition(20, y - 20);
        }
    }

    public void dropEvent(DropEvent e) {
        if (e.isImage()) {
            logger.info("drag&drop: loading image..." + e.filePath());
            PImage img = loadImage(e.filePath());
            if (img != null) {
                scanner.setImage(img);
            }
        }
    }

    public PFont getBodyFont() {
        return fontBody;
    }

    public List<DmxUniverseConfig> getConfigForNode(ArtNetNode node) {
        List<DmxUniverseConfig> configs = new ArrayList<DmxUniverseConfig>();
        for (DmxUniverseConfig nc : config.artnet.nodes) {
            if (node.getIPAddress().equals(nc.ip)) {
                logger.info("found matching IP address: " + nc.ip);
                byte[] dmxOuts = node.getDmxOuts();
                for (int i = 0; i < dmxOuts.length; i++) {
                    if (dmxOuts[i] == nc.universeID) {
                        configs.add(nc);
                        logger.info("found matching config: " + nc);
                        break;
                    }
                }
            }
        }
        return configs;
    }

    public GUIManager getGUI() {
        return gui;
    }

    public PFont getHDFont() {
        return fontBody;
    }

    public AppState getState() {
        return state;
    }

    private void initArtNet() {
        artnet = new ArtNetManager(this);
        artnet.start();
    }

    public void initDND() {
        sDrop = new SDrop(this);
    }

    public void initGUI() {
        if (cp5 == null) {
            cp5 = new ControlP5(this);
            gui = new GUIManager(cp5);
            gui.createControllers(STATE_MAIN, 20, 60, null);
            uiStatus = cp5.addTextlabel("uiStatus", "", 20, height - 20);
            uiCurrentImg = cp5.addTextlabel("uiStatus", "Current Image", 20,
                    height - 20 - 200);
        }
    }

    public void initSlitScanner() {
        scanner = new SlitScanner(config.slitscanner, artnet);
        if (artnet.getKnownUniverses().size() > 0) {
            scanner.setDMXUniverse(artnet.getKnownUniverses().get(0));
            scanner.setDefaultImageColor(TColor.GREEN);
        } else {
            scanner.setDefaultImageColor(TColor.RED);
        }
        gui.createControllers(scanner);
        new Thread(scanner).start();
    }

    private void initTextRenderer() {
        txtRenderer = new TextRenderer(this, config.render);
    }

    public void mouseDragged() {
        state.mouseDragged(this);
    }

    public void mouseMoved() {
        state.mouseMoved(this);
    }

    public void mousePressed() {
        state.mousePressed(this);
    }

    public void mouseReleased() {
        state.mouseReleased(this);
    }

    public void setSlitScanImage(PImage image) {
        scanner.setImage(image);
    }

    public void setSlitScanText(String content) {
        scanner.setImage(txtRenderer.getRenderedText(content));
    }

    public void setState(AppState newState) {
        logger.info("entering new app state: " + newState);
        this.state = newState;
        this.state.enter(this);
    }

    public void setup() {
        super.setup();
        setState(STATE_DISCOVERY);
        initArtNet();
        initTextRenderer();
        fontHD = loadFont(config.ui.fontHD);
        fontBody = loadFont(config.ui.fontBody);
    }

    @Override
    public void stop() {
        logger.info("shutting down application...");
        scanner.shutdown();
        artnet.shutdown();
        super.stop();
    }

    public void transitionState() {
        state.transition(this);
    }

    public void transitionToState(AppState newState) {
        if (state != null) {
            state.leave(this);
        }
        setState(newState);
    }

    public void useBodyFont() {
        textFont(fontBody);
    }

    public void useHdFont() {
        textFont(fontHD);
    }

    public void writeHeader(String hd) {
        textFont(fontHD);
        fill(255);
        text(hd, 200, 40);
    }
}
