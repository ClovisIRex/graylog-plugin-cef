package org.graylog.plugins.cef.parser;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyslogCEFParser {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogCEFParser.class);

    /*
     * TODO:
     *   - benchmark regex
     */

    private static final Pattern SYSLOG_PATTERN = Pattern.compile("^<\\d+>([a-zA-Z]{3}\\s+\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2})(.+)$");
    private static final DateTimeFormatter TIMESTAMP_PATTERN = DateTimeFormat.forPattern("MMM dd HH:mm:ss");

    private final DateTimeZone timezone;
    private final CEFParser parser;

    public SyslogCEFParser(DateTimeZone timezone) {
        this.timezone = timezone;
        this.parser = new CEFParser();
    }

    public CEFMessage parse(String x) throws ParserException {
        Matcher m = SYSLOG_PATTERN.matcher(x);

        if(m.find()) {
            try {
                DateTime timestamp = DateTime.parse(m.group(1).replaceAll("\\s+", " "), TIMESTAMP_PATTERN)
                        .withYear(DateTime.now(timezone).getYear())
                        .withZoneRetainFields(timezone);

                // Build the message with all CEF headers.
                CEFMessage.Builder builder = parser.parse(m.group(2));
                builder.timestamp(timestamp);
                return builder.build();
            } catch(Exception e) {
                LOG.debug("CEF was [{}].", m.group(2));
                throw new ParserException("Could not parse CEF message in envelope.", e);
            }
        } else {
            throw new ParserException("This message was not recognized as CEF and could not be parsed.");
        }
    }

}
