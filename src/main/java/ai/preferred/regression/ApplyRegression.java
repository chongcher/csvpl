package ai.preferred.regression;

import ai.preferred.regression.io.ARFFDataReader;
import ai.preferred.regression.io.CSVInputData;
import ai.preferred.regression.io.CSVUtils;
import org.apache.commons.csv.CSVPrinter;
import org.kohsuke.args4j.Option;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class ApplyRegression extends Command {

  @Option(name = "-s", aliases = {"--train"}, usage = "the path to the training data", required = true)
  private File train;

  @Option(name = "-i", aliases = {"--test"}, usage = "the path to the testing data", required = true)
  private File test;

  @Option(name = "-o", aliases = {"--output"}, usage = "the path to the output CSV file", required = true)
  private File output;

  @Option(name = "-m", aliases = {"--model"}, usage = "the path to the model file", required = true)
  private File model;

  @Option(name = "-h", aliases = {"--header"}, usage = "specifies if the input CSV files have headers")
  private boolean header = true;

  @Override
  protected void exec() throws Exception {
    try (final FileInputStream stream = new FileInputStream(model)) {
      final Classifier classifier = (Classifier) SerializationHelper.read(stream);
      final boolean nominal = WekaUtils.isLogisticClassifier(classifier);

      final ARFFDataReader reader = new ARFFDataReader(train, nominal, header);
      final Instances data = reader.read(test);

      try (final CSVPrinter printer = CSVUtils.printer(output);
           final CSVInputData csvData = CSVUtils.reader(test, header)) {
        if (csvData.hasHeader()) {
          printer.printRecord(csvData.getHeader());
        }

        int index = 0;
        for (final ArrayList<String> record : csvData) {
          final double prediction = classifier.classifyInstance(data.get(index));
          if (nominal) {
            record.set(0, data.classAttribute().value((int) prediction));
          } else {
            record.set(0, String.valueOf(prediction));
          }
          printer.printRecord(record);
        }
      }
    }
  }

  public static void main(String[] args) {
    parseArgsAndRun(ApplyRegression.class, args);
  }

}
