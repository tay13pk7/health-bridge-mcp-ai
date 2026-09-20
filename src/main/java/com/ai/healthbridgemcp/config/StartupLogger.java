package com.ai.healthbridgemcp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Logs registered Spring beans at application startup.
 * Useful for debugging bean registration, proxy creation, and Spring AI tool provider setup.
 * 
 * Enable/disable via application.yml:
 *   app.debug.startup-logging: true/false
 */
@Component
@ConditionalOnProperty(
    name = "app.debug.startup-logging",
    havingValue = "true",
    matchIfMissing = false  // Default: OFF (no logging noise in production)
)
public class StartupLogger implements ApplicationListener<ContextRefreshedEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(StartupLogger.class);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext ctx = event.getApplicationContext();
        
        log.info("==========================================");
        log.info(" PRASAD KUTE  REGISTERED BEANS (Startup Debug)");
        log.info("==========================================");
        
        int count = 0;
        for (String name : ctx.getBeanDefinitionNames()) {
            if (shouldLogBean(name)) {
                Object bean = ctx.getBean(name);
                String beanClass = bean.getClass().getName();
                
                // Highlight proxies
                if (beanClass.contains("CGLIB") || beanClass.contains("Proxy")) {
                    log.info("🎭 {} → {} [PROXIED]", name, beanClass);
                } else {
                    log.info("📦 {} → {}", name, beanClass);
                }
                count++;
            }
        }
        
        log.info("==========================================");
        log.info("   Total beans logged: {}", count);
        log.info("==========================================");
    }
    
    /**
     * Filter which beans to log (reduces noise).
     */
    private boolean shouldLogBean(String beanName) {
        String lower = beanName.toLowerCase();
        return lower.contains("tool") 
            || lower.contains("mcp") 
            || lower.contains("groq")
            || lower.contains("chat")
            || lower.contains("audit")
            || lower.contains("config");
    }
}
