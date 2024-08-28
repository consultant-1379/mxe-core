package com.ericsson.mxe.modelservice;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import java.util.List;
import org.apache.commons.compress.utils.Lists;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import com.ericsson.mxe.backendservicescommon.exception.MxeInternalException;
import com.ericsson.mxe.modelservice.dto.MxeModelData;

class MxeParameterValidatorTest {

    @Test
    public void testValidateWeights() {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(MxeParameterValidator.validateWeights(getModelList(0.1), true, 1)).isEqualTo(getWeightList());
        softly.assertThat(MxeParameterValidator.validateWeights(getModelList(1.2), true, 1)).isEqualTo(getWeightList());
        softly.assertThat(MxeParameterValidator.validateWeights(getModelList(0.1), true, 2))
                .isEqualTo(getWeightList(0.1, 0.9));
        softly.assertThat(MxeParameterValidator.validateWeights(getModelList(0.1, null), true, 2))
                .isEqualTo(getWeightList(0.1, 0.9));
        softly.assertThat(MxeParameterValidator.validateWeights(getModelList(0.79999999999, 0.20000000000), true, 2))
                .isEqualTo(getWeightList(0.8, 0.2));
        softly.assertThat(MxeParameterValidator.validateWeights(getModelList(0.8, null), true, 2))
                .isEqualTo(getWeightList(0.8, 0.2));
        softly.assertThat(MxeParameterValidator.validateWeights(getModelList(0.799999, 0.200001), true, 2))
                .isEqualTo(getWeightList(0.8, 0.2));
        softly.assertThat(MxeParameterValidator.validateWeights(getModelList(null, null), true, 2))
                .isEqualTo(getWeightList(0.5, 0.5));
        softly.assertThat(MxeParameterValidator.validateWeights(getModelList(null, null), false, 2))
                .isEqualTo(getWeightList(null, null));
        softly.assertAll();
    }

    @Test
    public void testValidateWeight() {
        SoftAssertions softly = new SoftAssertions();
        MxeParameterValidator.validateWeightIsBetweenZeroAndOne(0.1);
        MxeParameterValidator.validateWeightIsBetweenZeroAndOne(0.5);
        MxeParameterValidator.validateWeightIsBetweenZeroAndOne(0.0);
        MxeParameterValidator.validateWeightIsBetweenZeroAndOne(1.0);
        softly.assertAll();
        assertThatExceptionOfType(MxeInternalException.class).isThrownBy(() -> {
            MxeParameterValidator.validateWeightIsBetweenZeroAndOne(1.1);
        }).withMessage(MxeParameterValidator.WEIGHT_IS_NOT_BETWEEN_0_AND_1_ERROR_MESSAGE.formatted(1.1));
        assertThatExceptionOfType(MxeInternalException.class).isThrownBy(() -> {
            MxeParameterValidator.validateWeightIsBetweenZeroAndOne(-0.1);
        }).withMessage(MxeParameterValidator.WEIGHT_IS_NOT_BETWEEN_0_AND_1_ERROR_MESSAGE.formatted(-0.1));
    }

    @Test
    public void testCheckWeightsAndCalculateMissingWeightIfNeededAndPossible() {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(
                MxeParameterValidator.checkWeightsAndCalculateMissingWeightIfNeededAndPossible(getWeightList(0.5)))
                .isEqualTo(getWeightList(0.5));
        softly.assertThat(MxeParameterValidator
                .checkWeightsAndCalculateMissingWeightIfNeededAndPossible(getWeightList(null, null, null)))
                .isEqualTo(getWeightList(null, null, null));
        softly.assertThat(MxeParameterValidator
                .checkWeightsAndCalculateMissingWeightIfNeededAndPossible(getWeightList(0.2, 0.3, 0.5)))
                .isEqualTo(getWeightList(0.2, 0.3, 0.5));
        softly.assertThat(MxeParameterValidator
                .checkWeightsAndCalculateMissingWeightIfNeededAndPossible(getWeightList(0.0, 0.0, 1.0)))
                .isEqualTo(getWeightList(0.0, 0.0, 1.0));
        softly.assertThat(MxeParameterValidator
                .checkWeightsAndCalculateMissingWeightIfNeededAndPossible(getWeightList(1.0, null, 0.0)))
                .isEqualTo(getWeightList(1.0, 0.0, 0.0));
        softly.assertThat(MxeParameterValidator
                .checkWeightsAndCalculateMissingWeightIfNeededAndPossible(getWeightList(0.2, null, 0.4)))
                .isEqualTo(getWeightList(0.2, 0.4, 0.4));
        softly.assertAll();
        assertThatExceptionOfType(MxeInternalException.class).isThrownBy(() -> {
            MxeParameterValidator.checkWeightsAndCalculateMissingWeightIfNeededAndPossible(getWeightList(0.1, 1.0));
        }).withMessage(MxeParameterValidator.SUM_OF_WEIGHTS_SHOULD_BE_ONE.formatted("1.1"));
        assertThatExceptionOfType(MxeInternalException.class).isThrownBy(() -> {
            MxeParameterValidator.checkWeightsAndCalculateMissingWeightIfNeededAndPossible(getWeightList(0.1, 0.5));
        }).withMessage(MxeParameterValidator.SUM_OF_WEIGHTS_SHOULD_BE_ONE.formatted("0.6"));
        assertThatExceptionOfType(MxeInternalException.class).isThrownBy(() -> {
            MxeParameterValidator
                    .checkWeightsAndCalculateMissingWeightIfNeededAndPossible(getWeightList(1.0, null, null));
        }).withMessage(MxeParameterValidator.MORE_THAN_ONE_WEIGHT_IS_MISSING.formatted("2"));
        assertThatExceptionOfType(MxeInternalException.class).isThrownBy(() -> {
            MxeParameterValidator
                    .checkWeightsAndCalculateMissingWeightIfNeededAndPossible(getWeightList(1.0, null, 1.0));
        }).withMessage(MxeParameterValidator.SUM_OF_DEFINED_WEIGHTS_IS_MORE_THAN_ONE.formatted("2.0"));
    }

    private List<MxeModelData> getModelList(Double... weightsParam) {
        List<MxeModelData> models = Lists.newArrayList();
        for (Double weight : weightsParam) {
            MxeModelData model = new MxeModelData();
            model.weight = weight;
            models.add(model);
        }
        return models;
    }

    private List<Double> getWeightList(Double... weightsParam) {
        List<Double> weights = Lists.newArrayList();
        for (Double weight : weightsParam) {
            weights.add(weight);
        }
        return weights;
    }
}

