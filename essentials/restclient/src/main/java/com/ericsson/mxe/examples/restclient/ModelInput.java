package com.ericsson.mxe.examples.restclient;

import java.util.Base64;

public class ModelInput {
    private DataWrapper data;

    public ModelInput(byte[] input) {
        data = new DataWrapper(input);
    }

    public void setData(DataWrapper data) {
        this.data = data;
    }

    public DataWrapper getData() {
        return data;
    }

    public static class DataWrapper {
        private String[][] ndarray;

        public DataWrapper(byte[] input) {
            ndarray = new String[1][];
            ndarray[0] = new String[]{Base64.getEncoder().encodeToString(input)};
        }

        public void setNdarray(String[][] ndarray) {
            this.ndarray = ndarray;
        }

        public String[][] getNdarray() {
            return ndarray;
        }
    }
}
