package com.ai.healthbridgemcp.config;

import com.ai.healthbridgemcp.tools.*;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider athenaToolProvider(AthenaTools tools) {
        return MethodToolCallbackProvider.builder().toolObjects(tools).build();
    }

    @Bean
    public ToolCallbackProvider modmedToolProvider(ModMedTools tools) {
        return MethodToolCallbackProvider.builder().toolObjects(tools).build();
    }

    @Bean
    public ToolCallbackProvider nextgenToolProvider(NextgenTools tools) {
        return MethodToolCallbackProvider.builder().toolObjects(tools).build();
    }

    @Bean
    public ToolCallbackProvider allscriptsToolProvider(AllscriptsTools tools) {
        return MethodToolCallbackProvider.builder().toolObjects(tools).build();
    }

    @Bean
    public ToolCallbackProvider jobDashboardToolProvider(JobDashboardTools tools) {
        return MethodToolCallbackProvider.builder().toolObjects(tools).build();
    }
}

