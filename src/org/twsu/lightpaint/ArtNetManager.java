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

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import artnet4j.ArtNet;
import artnet4j.ArtNetException;
import artnet4j.ArtNetNode;
import artnet4j.ArtNetNodeDiscovery;
import artnet4j.DmxUniverse;
import artnet4j.DmxUniverseConfig;
import artnet4j.events.ArtNetDiscoveryListener;
import artnet4j.packets.ArtDmxPacket;
import artnet4j.packets.ArtNetPacketParser;

public class ArtNetManager implements ArtNetDiscoveryListener {

    protected static final Logger logger = Logger.getLogger(ArtNetManager.class
            .getName());

    private final LightPaintApp app;

    private final List<ArtNetNode> connectedNodes = new ArrayList<ArtNetNode>();
    private final ConcurrentHashMap<String, DmxUniverse> dmxUniverses = new ConcurrentHashMap<String, DmxUniverse>();

    private ArtNet artnet;

    public ArtNetManager(LightPaintApp app) {
        this.app = app;
        ArtNetPacketParser.logger.setLevel(Level.WARNING);
    }

    @Override
    public void discoveredNewNode(ArtNetNode node) {
        logger.info("node discovered: " + node);
        List<DmxUniverseConfig> nodeDefs = app.getConfigForNode(node);
        for (DmxUniverseConfig nc : nodeDefs) {
            DmxUniverse universe = new DmxUniverse(node, nc);
            dmxUniverses.put(universe.getID(), universe);
            connectedNodes.add(node);
            logger.info("new universe added: " + universe.getID());
        }
    }

    @Override
    public void discoveredNodeDisconnected(ArtNetNode node) {
        logger.info("disconnected: " + node);
        for (DmxUniverse u : dmxUniverses.values()) {
            if (u.getNode() == node) {
                u.setEnabled(false);
            }
        }
    }

    @Override
    public void discoveryCompleted(List<ArtNetNode> nodes) {
        if (app.getState() == LightPaintApp.STATE_DISCOVERY) {
            app.transitionState();
        }
    }

    @Override
    public void discoveryFailed(Throwable e) {
        logger.log(Level.SEVERE, "discovery failed", e);
    }

    public List<DmxUniverse> getKnownUniverses() {
        return new ArrayList<DmxUniverse>(dmxUniverses.values());
    }

    public DmxUniverse getUniverseForID(String id) {
        DmxUniverse u = dmxUniverses.get(id);
        return u;
    }

    public void shutdown() {

    }

    public void start() {
        try {
            artnet = new ArtNet();
            artnet.start();
            ArtNetNodeDiscovery discovery = artnet.getNodeDiscovery();
            discovery.addListener(this);
            discovery.setInterval(LightPaintApp.config.artnet.pollInterval);
            discovery.start();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (ArtNetException e) {
            e.printStackTrace();
        }
    }

    public void updateUniverses(int sequenceID, boolean sendToAll) {
        logger.finer("updating all DMX universes");
        for (DmxUniverse u : dmxUniverses.values()) {
            ArtDmxPacket packet = u.getPacket(0);// sequenceID % 255);
            // System.out.println(new ByteUtils(packet.getData()).toHex(32));
            if (sendToAll) {
                artnet.broadcastPacket(packet);
            } else if (u.isEnabled() && u.isActive()) {
                artnet.unicastPacket(packet, u.getNode());
            }
        }
    }
}
