package com.sptracer;

import com.sptracer.configuration.ConfigurationRegistry;
import com.sptracer.configuration.source.ConfigurationSource;

import java.util.List;

public abstract class SpTracerConfigurationSourceInitializer implements SpTracerSPI {

    public abstract void modifyConfigurationSources(ModifyArguments modifyArguments);

    public void onConfigurationInitialized(ConfigInitializedArguments configInitializedArguments) throws Exception {
    }

    public static class ModifyArguments {
        private final List<ConfigurationSource> configurationSources;


        ModifyArguments(List<ConfigurationSource> configurationSources) {
            this.configurationSources = configurationSources;
        }

        public void addConfigurationSourceAsFirst(ConfigurationSource configurationSource) {
            configurationSources.add(0, configurationSource);
        }

        public void addConfigurationSourceAsLast(ConfigurationSource configurationSource) {
            configurationSources.add(configurationSource);
        }
    }

    public static class ConfigInitializedArguments {
        private final ConfigurationRegistry configuration;

        /**
         * @param configuration the configuration that has just been initialized
         */
        ConfigInitializedArguments(ConfigurationRegistry configuration) {
            this.configuration = configuration;
        }

        public ConfigurationRegistry getConfiguration() {
            return configuration;
        }
    }
}
