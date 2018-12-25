package köntröller;

public class Network {
    private double[][] output;
    private double[][][] weights;
    private double[][] bias;

    private double[][] errorSignal;
    private double[][] outputDerivative;

    public final int[] networkLayerSizes;
    public final int inputSize;
    public final int outputSize;
    public final int networkSize;

    public Network(int... networkLayerSizes) {
        this.networkLayerSizes = networkLayerSizes;
        this.inputSize = networkLayerSizes[0];
        this.networkSize = networkLayerSizes.length;
        this.outputSize = networkLayerSizes[networkSize - 1];

        this.output = new double[networkSize][];
        this.weights = new double[networkSize][][];
        this.bias = new double[networkSize][];

        this.errorSignal = new double[networkSize][];
        this.outputDerivative = new double[networkSize][];

        for (int i = 0; i < networkSize; i++) {
            this.output[i] = new double[networkLayerSizes[i]];
            this.errorSignal[i] = new double[networkLayerSizes[i]];
            this.outputDerivative[i] = new double[networkLayerSizes[i]];

            this.bias[i] = NetworkTools.createRandomArray(networkLayerSizes[i], 0.3, 0.7);

            if (i > 0) {
                weights[i] = NetworkTools.createRandomArray(networkLayerSizes[i], networkLayerSizes[i - 1], -0.3, 0.5);
            }
        }
    }

    public double[] calculate(double... input) {
        if (input.length != this.inputSize)
            return null;
        this.output[0] = input;
        for (int layer = 1; layer < networkSize; layer++) {
            for (int neuron = 0; neuron < networkLayerSizes[layer]; neuron++) {

                double sum = bias[layer][neuron];
                ;
                for (int prevNeuron = 0; prevNeuron < networkLayerSizes[layer - 1]; prevNeuron++) {
                    sum += output[layer - 1][prevNeuron] * weights[layer][neuron][prevNeuron];
                }

                output[layer][neuron] = sigmoid(sum);
                outputDerivative[layer][neuron] = output[layer][neuron] * (1 - output[layer][neuron]);

            }
        }

        return output[networkSize - 1];

    }

    public void train(TrainSet set, int loops, int batchSize) {
        if (set.INPUT_SIZE != inputSize || set.OUTPUT_SIZE != outputSize) return;
        for (int i = 0; i < loops; i++) {
            TrainSet batch = set.extractBatch(batchSize);
            for (int b = 0; b < batchSize; b++) {
                this.train(batch.getInput(b), batch.getOutput(b), 0.3);
            }
            System.out.println(MSE(batch));
        }
    }

    public double MSE(double[] input, double[] target) {
        if (input.length != inputSize || target.length != outputSize) return 0;
        calculate(input);
        double v = 0;
        for (int i = 0; i < target.length; i++) {
            v += (target[i] - output[networkSize-1][i]) * (target[i] - output[networkSize-1][i]);
        }
        return v / (2d * target.length);
    }

    public double MSE(TrainSet set) {
        double v = 0;
        for (int i = 0; i < set.size(); i++) {
            v += MSE(set.getInput(i), set.getOutput(i));
        }
        return v / set.size();
    }

    public void train(double[] input, double[] target, double eta) {
        if (input.length != inputSize || target.length != outputSize)
            return;
        calculate(input);
        backpropError(target);
        updateWeights(eta);
    }

    public void backpropError(double[] target) {
        for (int neuron = 0; neuron < networkLayerSizes[networkSize - 1]; neuron++) {
            errorSignal[networkSize - 1][neuron] = (output[networkSize - 1][neuron] - target[neuron])
                    * outputDerivative[networkSize - 1][neuron];
        }

        for (int layer = networkSize - 2; layer > 0; layer--) {
            for (int neuron = 0; neuron < networkLayerSizes[layer]; neuron++) {
                double sum = 0;
                for (int nextNeuron = 0; nextNeuron < networkLayerSizes[layer + 1]; nextNeuron++) {
                    sum += weights[layer + 1][nextNeuron][neuron] * errorSignal[layer + 1][nextNeuron];
                }
                this.errorSignal[layer][neuron] = sum * outputDerivative[layer][neuron];
            }
        }
    }

    public void updateWeights(double eta) {
        for (int layer = 1; layer < networkSize; layer++) {
            for (int neuron = 0; neuron < networkLayerSizes[layer]; neuron++) {
                double delta = -eta * errorSignal[layer][neuron];
                bias[layer][neuron] += delta;
                for (int prevNeuron = 0; prevNeuron < networkLayerSizes[layer - 1]; prevNeuron++) {
                    double deltaWeights = delta * output[layer - 1][prevNeuron];
                    weights[layer][neuron][prevNeuron] += deltaWeights;
                }
            }
        }
    }

    private double sigmoid(double x) {
        return 1d / (1 + Math.exp(-x));
    }


}
