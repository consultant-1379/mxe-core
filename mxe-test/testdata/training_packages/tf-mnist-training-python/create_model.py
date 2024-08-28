import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers

# Load MNIST dataset
(x_train, y_train), (x_test, y_test) = keras.datasets.mnist.load_data()

# Preprocess data
x_train = x_train.reshape(60000, 784).astype("float32") / 255
x_test = x_test.reshape(10000, 784).astype("float32") / 255
y_train = y_train.astype("float32")
y_test = y_test.astype("float32")

# Create model
model = keras.Sequential([
    layers.Dense(10, input_shape=(784,), activation='softmax')
])

# Compile model with SGD optimizer
model.compile(
    optimizer=keras.optimizers.SGD(learning_rate=0.1),
    loss='sparse_categorical_crossentropy',
    metrics=['accuracy']
)

# Train model for 100 epochs with a batch size of 100
model.fit(x_train, y_train, batch_size=100, epochs=10)

# Evaluate the trained model on the test dataset
test_loss, test_acc = model.evaluate(x_test, y_test)
print('Test accuracy:', test_acc)

# Save model to disk
# Create a Checkpoint object
checkpoint = tf.train.Checkpoint(model=model)

# Save the model checkpoint
checkpoint.save(file_prefix="./output/deep_mnist_model")