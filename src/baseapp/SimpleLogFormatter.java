package baseapp;

import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SimpleLogFormatter extends Formatter {

    private static final MessageFormat messageFormat = new MessageFormat(
            "{0,date,yyyy-MM-dd hh:mm:ss}|{1}|{2}|{3}: {4}\n");

    public SimpleLogFormatter() {
        super();
    }

    @Override
    public String format(LogRecord record) {
        Object[] arguments = new Object[6];
        arguments[0] = new Date(record.getMillis());
        arguments[1] = record.getLevel();
        arguments[2] = record.getLoggerName();
        arguments[3] = Thread.currentThread().getName();
        arguments[4] = record.getMessage();
        String msg = messageFormat.format(arguments);
        Throwable e = record.getThrown();
        if (e != null) {
            msg += "Exception: " + e.toString() + "\n";
            for (StackTraceElement s : e.getStackTrace()) {
                msg += "\t" + s.toString() + "\n";
            }
        }
        return msg;
    }

}