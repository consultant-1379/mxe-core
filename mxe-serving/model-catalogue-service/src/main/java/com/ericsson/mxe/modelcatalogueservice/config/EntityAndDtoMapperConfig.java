
package com.ericsson.mxe.modelcatalogueservice.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ericsson.mxe.modelcatalogueservice.dto.Model;
import com.ericsson.mxe.modelcatalogueservice.persistence.domain.ModelEntity;


@Configuration
public class EntityAndDtoMapperConfig {

    @Bean
    public ModelMapper entityAndDtoMapper(PropertyMap<ModelEntity, Model> modelEntityToDto) {
        final ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.addMappings(modelEntityToDto);

        return modelMapper;
    }

    @Bean
    PropertyMap<ModelEntity, Model> modelEntityToDto() {
        return new PropertyMap<>() {
            protected void configure() {
                map().setId(source.getId().getId());
                map().setVersion(source.getId().getVersion());
            }
        };
    }

}
