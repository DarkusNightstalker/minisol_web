package cs.bms.bean.util;

import gkfire.web.bean.AbstractSessionBean;

public class PNotifyMessage extends gkfire.web.util.JavaScriptMessage {

    protected String title;
    protected String content;
    protected String shadow;
    protected double opacity;
    protected String styleClass;
    protected Type type;
    protected String stack;
    protected String width;
    protected long delay;
    protected String icon;

    public static PNotifyMessage errorMessage(String content) {
        PNotifyMessage message = new PNotifyMessage("ERROR", content, Type.Danger, "fa fa-warning shaked animated", 3000L);
        message.execute();
        return message;
    }

    public static void infoMessage(String content) {
        new PNotifyMessage("INFO", content, Type.Info, "fa fa-exclamation shaked animated", 3000L).execute();
    }

    public static void saveMessage(String content) {
        new PNotifyMessage("EXITO", content, Type.Success, "fa fa-save shaked animated", 3000L).execute();
    }

    public static PNotifyMessage systemError(Exception ex, AbstractSessionBean sessionBean) {
        return errorMessage("Consulte el log de la app CODE : " + sessionBean.addError(ex));
    }

    public static enum Type {

        Light, Dark, Primary, Success, Info, Warning, Danger, Alert, System;

        private Type() {
        }
    }

    public PNotifyMessage() {
        this.width = "500px";
        this.opacity = 1.0D;
        this.shadow = "";
        this.styleClass = "stack_top_right";
        this.stack = "{'dir1': 'down','dir2': 'left','push': 'top','spacing1': 10,'spacing2': 10}";

        this.delay = 1400L;
    }

    public PNotifyMessage(String title, String content, Type type, String icon) {
        this();
        this.title = title;
        this.content = content;
        this.type = type;
        this.icon = icon;
    }

    public PNotifyMessage(String title, String content, Type type, String icon, long delay) {
        this(title, content, type, icon);
        this.delay = delay;
    }

    public String toJavaScript() {
        return "new PNotify({title: '" + this.title + "'," + "text: '" + this.content + "'," + "shadow: '" + this.shadow + "'," + "opacity: " + this.opacity + "," + "addclass: '" + this.styleClass + "'," + "type: '" + this.type.name().toLowerCase() + "'," + "stack: " + this.stack + "," + "width: '" + this.width + "'," + "delay: " + this.delay + "," + "icon: '" + this.icon + "'" + "});";
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getShadow() {
        return this.shadow;
    }

    public void setShadow(String shadow) {
        this.shadow = shadow;
    }

    public double getOpacity() {
        return this.opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    public String getStyleClass() {
        return this.styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getStack() {
        return this.stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

    public String getWidth() {
        return this.width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public long getDelay() {
        return this.delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public String getIcon() {
        return this.icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
