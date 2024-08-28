
class SeldonPythonReferenceModelV1(object):
    
    def __init__(self):
        print("Initializing")
        self._metrics = []

    def predict(self,X,feature_names):
        print("Predict called with feature names: ")
        print(feature_names)
        print("Result:")
        print(X)
        value = X[0]

        self._metrics = [
            {"type":"COUNTER","key":"test_custom_counter","value":value}, # a counter which will increase by the given value
            {"type":"GAUGE","key":"test_custom_gauge","value":value}, # a gauge which will be set to given value
            {"type":"TIMER","key":"test_custom_timer","value":1}, # a timer which will add sum and count metrics - assumed millisecs
        ]
        return X

    def metrics(self):
        print("Returning metrics:")
        print(self._metrics)
        return self._metrics