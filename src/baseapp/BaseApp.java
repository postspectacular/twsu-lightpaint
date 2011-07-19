package baseapp;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import processing.core.PApplet;

public class BaseApp extends PApplet {

    protected static final Logger logger = Logger.getLogger(BaseApp.class
            .getSimpleName());

    protected static String CONFIG_FILE = "config/appconfig.xml";

    public static AppConfig config;

    private static boolean loadConfig() {
        try {
            logger.info("loading config file: "
                    + new File(CONFIG_FILE).getAbsolutePath());
            JAXBContext context = JAXBContext.newInstance(AppConfig.class);
            config = (AppConfig) context.createUnmarshaller().unmarshal(
                    new File(CONFIG_FILE));
            return true;
        } catch (JAXBException e) {
            logger.severe("can't load config file");
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            CONFIG_FILE = args[0];
        }
        if (loadConfig()) {
            ArrayList<String> argList = new ArrayList<String>();
            if (config.app.windowLoc.x != -1) {
                int x = (int) config.app.windowLoc.x;
                int y = (int) config.app.windowLoc.y;
                argList.add("--location=" + x + "," + y);
            }
            if (config.app.isPresent) {
                argList.add("--present");
            }
            argList.add("--bgcolor=" + config.app.borderColor);
            argList.add("--hide-stop");
            argList.add(config.app.mainClass);
            logger.info("launching app: " + config.app.mainClass);
            PApplet.main(argList.toArray(new String[0]));
        }
    }

    public AppConfig getConfig() {
        return config;
    }

    public boolean saveConfig(String path) {
        try {
            logger.info("saving config to: " + path);
            JAXBContext context = JAXBContext.newInstance(AppConfig.class);
            context.createMarshaller().marshal(config, new File(path));
            return true;
        } catch (JAXBException e) {
            logger.severe("couldn't save config file");
            e.printStackTrace();
            return false;
        }
    }

    public void setup() {
        if (config.app.hasChrome) {
            frame.setTitle(config.app.name);
        } else {
            frame.removeNotify();
            frame.setUndecorated(true);
            frame.addNotify();
        }
        size(config.app.width, config.app.height, config.app.renderer);
        frameRate(config.app.fps);
    }

    public void updateWindowState(boolean force) {
        if (!config.app.isDebug || force) {
            int x = (int) config.app.windowLoc.x;
            int y = (int) config.app.windowLoc.y;
            if (x != -1) {
                frame.setLocation(x, y);
            }
        }
        frame.setAlwaysOnTop(config.app.isWindowOnTop);
        if (config.app.hideMouse) {
            noCursor();
        }
    }
}
