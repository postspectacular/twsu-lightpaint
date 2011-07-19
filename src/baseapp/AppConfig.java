package baseapp;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.twsu.lightpaint.config.ArtNetConfig;
import org.twsu.lightpaint.config.FlickrConfig;
import org.twsu.lightpaint.config.RenderConfig;
import org.twsu.lightpaint.config.SlitScanConfig;
import org.twsu.lightpaint.config.UISettings;

@XmlRootElement(name = "config")
public class AppConfig {

    @XmlElement
    public AppSettings app;

    @XmlElement
    public ArtNetConfig artnet;

    @XmlElement
    public SlitScanConfig slitscanner;

    @XmlElement
    public FlickrConfig flickr;

    @XmlElement
    public UISettings ui;

    @XmlElement(name = "textRenderer")
    public RenderConfig render;
}
