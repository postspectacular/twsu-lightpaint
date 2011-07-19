package baseapp;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import processing.opengl.PGraphicsOpenGL;
import toxi.color.NamedColor;
import toxi.color.TColor;
import toxi.geom.Vec2D;

public class AppSettings {

    @XmlElement
    public String renderer = PGraphicsOpenGL.OPENGL;

    @XmlAttribute
    public int width = 1280;

    @XmlAttribute
    public int height = 720;

    @XmlAttribute
    public int fps = 999;

    @XmlAttribute
    public boolean isPresent;

    @XmlAttribute(name = "border")
    public String borderColor = "000000";

    @XmlAttribute(name = "bg")
    @XmlJavaTypeAdapter(toxi.color.TColorAdapter.class)
    public TColor bgColor = (TColor) NamedColor.BLACK;

    @XmlElement(name = "mainclass")
    public String mainClass;

    @XmlElement
    public Vec2D windowLoc = new Vec2D(-1, -1);

    @XmlElement
    public boolean hideMouse;

    @XmlElement(name = "modal")
    public boolean isWindowOnTop;

    @XmlElement(name = "debug")
    public boolean isDebug = true;

    @XmlElement
    public String name;

    @XmlElement(name = "chrome")
    public boolean hasChrome = true;

    @XmlElement
    public long timeout = 5 * 60000;
}
