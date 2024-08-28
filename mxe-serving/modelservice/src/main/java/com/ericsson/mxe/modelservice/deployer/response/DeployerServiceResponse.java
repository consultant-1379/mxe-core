package com.ericsson.mxe.modelservice.deployer.response;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeployerServiceResponse {

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Application {

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Metadata {
            public String name;
            public String namespace;
            public String selfLink;
            public String uid;
            public String resourceVersion;
            public Integer generation;
            public String creationTimestamp;
            public Map<String, String> labels;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Spec {

            @JsonIgnoreProperties(ignoreUnknown = true)
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class Source {
                public String repoURL;
                public String path;
                public String targetRevision;
            }
            @JsonIgnoreProperties(ignoreUnknown = true)
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class Destination {
                public String server;
                public String namespace;
            }
            @JsonIgnoreProperties(ignoreUnknown = true)
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class SyncPolicy {

                @JsonIgnoreProperties(ignoreUnknown = true)
                @JsonInclude(JsonInclude.Include.NON_NULL)
                public static class Automated {
                    public Boolean prune;
                    public Boolean selfHeal;
                    public Boolean allowEmpty;
                }

                public Automated automated;
            }

            public String project;
            public Source source;
            public Destination destination;
            public SyncPolicy syncPolicy;

        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Status {

            @JsonIgnoreProperties(ignoreUnknown = true)
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class Sync {
                @JsonIgnoreProperties(ignoreUnknown = true)
                @JsonInclude(JsonInclude.Include.NON_NULL)
                public static class CompareTo {
                    public Map<String, Object> source;
                    public Map<String, Object> destination;
                }

                public String status;
                public CompareTo compareTo;
            }

            public Sync sync;
            public Map<String, Object> health;
            public Map<String, Object> summary;

        }

        public Metadata metadata;
        public Spec spec;
        public Status status;
    }

    public Application application;
    public String err;
}
