package com.ericsson.mxe.modeltrainingservice.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ericsson.mxe.modeltrainingservice.dto.TrainingJob;
import com.ericsson.mxe.modeltrainingservice.dto.TrainingPackage;
import com.ericsson.mxe.modeltrainingservice.persistence.domain.TrainingJobEntity;
import com.ericsson.mxe.modeltrainingservice.persistence.domain.TrainingPackageEntity;

@Configuration
public class EntityAndDtoMapperConfig {

    @Bean
    public ModelMapper entityAndDtoMapper(PropertyMap<TrainingJobEntity, TrainingJob> trainingJobEntityToDto,
            PropertyMap<TrainingPackageEntity, TrainingPackage> trainingPackageEntityToDto) {
        final ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.addMappings(trainingJobEntityToDto);
        modelMapper.addMappings(trainingPackageEntityToDto);

        return modelMapper;
    }

    @Bean
    PropertyMap<TrainingJobEntity, TrainingJob> trainingJobEntityToDto() {
        return new PropertyMap<>() {
            protected void configure() {
                map().setPackageId(source.getTrainingPackageEntity().getId().getId());
                map().setPackageVersion(source.getTrainingPackageEntity().getId().getVersion());
            }
        };
    }

    @Bean
    PropertyMap<TrainingPackageEntity, TrainingPackage> trainingPackageEntityToDto() {
        return new PropertyMap<>() {
            protected void configure() {
                map().setId(source.getId().getId());
                map().setVersion(source.getId().getVersion());
            }
        };
    }

}
