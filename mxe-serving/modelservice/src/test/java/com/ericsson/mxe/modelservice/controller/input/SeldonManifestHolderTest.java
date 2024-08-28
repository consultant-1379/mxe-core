package com.ericsson.mxe.modelservice.controller.input;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import com.ericsson.mxe.backendservicescommon.exception.MxeRuntimeException;
import com.ericsson.mxe.modelservice.dto.AutoscalingMetricName;

public class SeldonManifestHolderTest {
    @Test
    public void testGetAutoscalingMetricValue() {
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(SeldonManifestHolder.getAutoscalingMetricValue(AutoscalingMetricName.CPU_MILLI_CORES, "100m"))
                .isEqualTo(100);
        softly.assertThat(SeldonManifestHolder.getAutoscalingMetricValue(AutoscalingMetricName.CPU_MILLI_CORES, "100"))
                .isEqualTo(100000);
        softly.assertThat(SeldonManifestHolder.getAutoscalingMetricValue(AutoscalingMetricName.CPU_MILLI_CORES, "100k"))
                .isEqualTo(100000000);
        softly.assertThat(SeldonManifestHolder.getAutoscalingMetricValue(AutoscalingMetricName.CPU_MILLI_CORES, "1M"))
                .isEqualTo(1000000000);
        softly.assertThat(
                SeldonManifestHolder.getAutoscalingMetricValue(AutoscalingMetricName.MEMORY_MEGA_BYTES, "100Mi"))
                .isEqualTo(100);
        softly.assertAll();
        assertThatExceptionOfType(MxeRuntimeException.class).isThrownBy(() -> {
            SeldonManifestHolder.getAutoscalingMetricValue(AutoscalingMetricName.CPU_MILLI_CORES, "100Mi");
        }).withMessage(SeldonManifestHolder.INVALID_UNIT_FOUND_IN_SELDON_DEPLOYMENT.formatted("Mi"));
        assertThatExceptionOfType(MxeRuntimeException.class).isThrownBy(() -> {
            SeldonManifestHolder.getAutoscalingMetricValue(AutoscalingMetricName.MEMORY_MEGA_BYTES, "100");
        }).withMessage(SeldonManifestHolder.INVALID_UNIT_FOUND_IN_SELDON_DEPLOYMENT.formatted(""));
        assertThatExceptionOfType(MxeRuntimeException.class).isThrownBy(() -> {
            SeldonManifestHolder.getAutoscalingMetricValue(AutoscalingMetricName.UNKNOWN, "100");
        }).withMessage(SeldonManifestHolder.INVALID_METRIC_NAME_FOUND_IN_SELDON_DEPLOYMENT
                .formatted(AutoscalingMetricName.UNKNOWN));
    }
}
