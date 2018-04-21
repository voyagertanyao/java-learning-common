package com.cmcciot.platform.common.log;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.layout.LoggerFields;
import org.apache.logging.log4j.core.layout.Rfc5424Layout;
import org.apache.logging.log4j.core.net.Facility;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.util.Integers;
import org.apache.logging.log4j.flume.appender.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author : ChenZhenYu
 * @Time : Created in: 10:42 2018/1/24
 */
@Plugin(
    name = "FlumeLog",
    category = "Core",
    elementType = "appender",
    printObject = true
)
public final class FlumeLogAppender extends AbstractAppender implements FlumeEventFactory {
    private static final String[] EXCLUDED_PACKAGES = new String[]{"org.apache.flume", "org.apache.avro"};
    private static final int DEFAULT_MAX_DELAY = 60000;
    private static final int DEFAULT_LOCK_TIMEOUT_RETRY_COUNT = 5;
    private final String systemCode;
    private final String messageType;
    private final AbstractFlumeManager manager;
    private final String mdcIncludes;
    private final String mdcExcludes;
    private final String mdcRequired;
    private final String eventPrefix;
    private final String mdcPrefix;
    private final boolean compressBody;
    private final FlumeEventFactory factory;

    private FlumeLogAppender(String systemCode, String messageType, String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, String includes, String excludes, String required, String mdcPrefix, String eventPrefix, boolean compress, FlumeEventFactory factory, AbstractFlumeManager manager) {
        super(name, filter, layout, ignoreExceptions);
        this.systemCode = systemCode;
        this.messageType = messageType;
        this.manager = manager;
        this.mdcIncludes = includes;
        this.mdcExcludes = excludes;
        this.mdcRequired = required;
        this.eventPrefix = eventPrefix;
        this.mdcPrefix = mdcPrefix;
        this.compressBody = compress;
        this.factory = (FlumeEventFactory)(factory == null ? this : factory);
    }
    public void append(LogEvent event) {
        String name = event.getLoggerName();
        if (name != null) {
            String[] arr$ = EXCLUDED_PACKAGES;
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                String pkg = arr$[i$];
                if (name.startsWith(pkg)) {
                    return;
                }
            }
        }

        FlumeEvent flumeEvent = this.factory.createEvent(event, this.mdcIncludes, this.mdcExcludes, this.mdcRequired, this.mdcPrefix, this.eventPrefix, this.compressBody);
        flumeEvent.setBody(this.getLayout().toByteArray(flumeEvent));
        Map<String, String> headers = new HashMap();
        headers.put("systemCode", systemCode);
        headers.put("messageType", messageType);
        flumeEvent.setHeaders(headers);
        this.manager.send(flumeEvent);
    }

    public boolean stop(long timeout, TimeUnit timeUnit) {
        this.setStopping();
        boolean stopped = super.stop(timeout, timeUnit, false);
        stopped &= this.manager.stop(timeout, timeUnit);
        this.setStopped();
        return stopped;
    }

    public FlumeEvent createEvent(LogEvent event, String includes, String excludes, String required, String mdcPrefix, String eventPrefix, boolean compress) {
        return new FlumeEvent(event, this.mdcIncludes, this.mdcExcludes, this.mdcRequired, mdcPrefix, eventPrefix, this.compressBody);
    }

    @PluginFactory
    public static FlumeLogAppender createAppender(@PluginAttribute("systemCode") String systemCode, @PluginAttribute("messageType") String messageType, @PluginElement("Agents") Agent[] agents, @PluginElement("Properties") Property[] properties, @PluginAttribute("hosts") String hosts, @PluginAttribute("embedded") String embedded, @PluginAttribute("type") String type, @PluginAttribute("dataDir") String dataDir, @PluginAliases({"connectTimeout"}) @PluginAttribute("connectTimeoutMillis") String connectionTimeoutMillis, @PluginAliases({"requestTimeout"}) @PluginAttribute("requestTimeoutMillis") String requestTimeoutMillis, @PluginAttribute("agentRetries") String agentRetries, @PluginAliases({"maxDelay"}) @PluginAttribute("maxDelayMillis") String maxDelayMillis, @PluginAttribute("name") String name, @PluginAttribute("ignoreExceptions") String ignore, @PluginAttribute("mdcExcludes") String excludes, @PluginAttribute("mdcIncludes") String includes, @PluginAttribute("mdcRequired") String required, @PluginAttribute("mdcPrefix") String mdcPrefix, @PluginAttribute("eventPrefix") String eventPrefix, @PluginAttribute("compress") String compressBody, @PluginAttribute("batchSize") String batchSize, @PluginAttribute("lockTimeoutRetries") String lockTimeoutRetries, @PluginElement("FlumeEventFactory") FlumeEventFactory factory, @PluginElement("Layout") Layout<? extends Serializable> layout, @PluginElement("Filter") Filter filter) {
        boolean embed = embedded != null ? Boolean.parseBoolean(embedded) : (agents == null || agents.length == 0 || hosts == null || hosts.isEmpty()) && properties != null && properties.length > 0;
        boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);
        boolean compress = Booleans.parseBoolean(compressBody, true);
        MyManagerType managerType;
        if (systemCode == null || messageType == null) {
            LOGGER.error("systemCode和messageType不能为空");
            return null;
        }
        if (type != null) {
            if (embed && embedded != null) {
                try {
                    managerType = MyManagerType.getType(type);
                    LOGGER.warn("Embedded and type attributes are mutually exclusive. Using type " + type);
                } catch (Exception var35) {
                    LOGGER.warn("Embedded and type attributes are mutually exclusive and type " + type + " is invalid.");
                    managerType = MyManagerType.EMBEDDED;
                }
            } else {
                try {
                    managerType = MyManagerType.getType(type);
                } catch (Exception var34) {
                    LOGGER.warn("Type " + type + " is invalid.");
                    managerType = MyManagerType.EMBEDDED;
                }
            }
        } else if (embed) {
            managerType = MyManagerType.EMBEDDED;
        } else {
            managerType = MyManagerType.AVRO;
        }

        int batchCount = Integers.parseInt(batchSize, 1);
        int connectTimeoutMillis = Integers.parseInt(connectionTimeoutMillis, 0);
        int reqTimeoutMillis = Integers.parseInt(requestTimeoutMillis, 0);
        int retries = Integers.parseInt(agentRetries, 0);
        int lockTimeoutRetryCount = Integers.parseInt(lockTimeoutRetries, 5);
        int delayMillis = Integers.parseInt(maxDelayMillis, 60000);
        if (layout == null) {
            layout = Rfc5424Layout.createLayout(Facility.LOCAL0, (String)null, 18060, true, "mdc", mdcPrefix, eventPrefix, false, (String)null, (String)null, (String)null, excludes, includes, required, (String)null, false, (LoggerFields[])null, (Configuration)null);
        }

        if (name == null) {
            LOGGER.error("No name provided for Appender");
            return null;
        } else {
            Object manager;
            switch(managerType) {
                case EMBEDDED:
                    manager = FlumeEmbeddedManager.getManager(name, agents, properties, batchCount, dataDir);
                    break;
                case AVRO:
                    manager = FlumeAvroManager.getManager(name, getAgents(agents, hosts), batchCount, delayMillis, retries, connectTimeoutMillis, reqTimeoutMillis);
                    break;
                case PERSISTENT:
                    manager = FlumePersistentManager.getManager(name, getAgents(agents, hosts), properties, batchCount, retries, connectTimeoutMillis, reqTimeoutMillis, delayMillis, lockTimeoutRetryCount, dataDir);
                    break;
                default:
                    LOGGER.debug("No manager type specified. Defaulting to AVRO");
                    manager = FlumeAvroManager.getManager(name, getAgents(agents, hosts), batchCount, delayMillis, retries, connectTimeoutMillis, reqTimeoutMillis);
            }

            return manager == null ? null : new FlumeLogAppender(systemCode, messageType,name, filter, (Layout)layout, ignoreExceptions, includes, excludes, required, mdcPrefix, eventPrefix, compress, factory, (AbstractFlumeManager)manager);
        }
    }

    private static Agent[] getAgents(Agent[] agents, String hosts) {
        if (agents == null || agents.length == 0) {
            if (hosts != null && !hosts.isEmpty()) {
                LOGGER.debug("Parsing agents from hosts parameter");
                String[] hostports = hosts.split(",");
                agents = new Agent[hostports.length];

                for(int i = 0; i < hostports.length; ++i) {
                    String[] h = hostports[i].split(":");
                    agents[i] = Agent.createAgent(h[0], h.length > 1 ? h[1] : null);
                }
            } else {
                LOGGER.debug("No agents provided, using defaults");
                agents = new Agent[]{Agent.createAgent((String)null, (String)null)};
            }
        }

        LOGGER.debug("Using agents {}", agents);
        return agents;
    }

    private static enum MyManagerType {
        AVRO,
        EMBEDDED,
        PERSISTENT;

        private MyManagerType() {
        }

        public static MyManagerType getType(String type) {
            return valueOf(type.toUpperCase(Locale.US));
        }
    }

}
