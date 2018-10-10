package net.kozelka.h2omojojava.impl;

import hex.genmodel.GenModel;
import hex.genmodel.MojoModel;
import hex.genmodel.algos.gbm.GbmMojoModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.exception.PredictUnknownCategoricalLevelException;
import hex.genmodel.easy.prediction.BinomialModelPrediction;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Ignore("cannot run automatically - contains reference to personal filesystem area")
public class H2oMojoJavaTest {

    private static final File TESTMODELS = new File(System.getProperty("user.home"), "h2o/h2o-mojo/testmodels");

    @Test
    public void testNamesV100() throws IOException, PredictException {
        final EasyPredictModelWrapper.Config config = new EasyPredictModelWrapper.Config();
        final GenModel model = GbmMojoModel.load("src/test/resources/gbm_v1.00_names.mojo");
        config.setModel(model);
        config.setConvertInvalidNumbersToNa(false);
        final EasyPredictModelWrapper easyModel = new EasyPredictModelWrapper(config);
        final RowData row = new RowData();
        row.put("age", "68");
        row.put("sex", "M");
        row.put("pclass", "1");
        final BinomialModelPrediction pred = easyModel.predictBinomial(row);
        System.out.printf("Result: %s\n", pred.label);
        System.out.printf("Class probabilities: %f %f\n", pred.classProbabilities[0], pred.classProbabilities[1]);
    }

    @Test
    public void testProstate() throws IOException, PredictException {
        final EasyPredictModelWrapper.Config config = new EasyPredictModelWrapper.Config();
        final GenModel model = GbmMojoModel.load(new File(TESTMODELS, "prostate/unzipped").getAbsolutePath());
        config.setModel(model);
        config.setConvertInvalidNumbersToNa(false);
        final EasyPredictModelWrapper easyModel = new EasyPredictModelWrapper(config);
        final RowData row = new RowData();
        row.put("AGE", "68");
        row.put("RACE", "2");
        row.put("DCAPS", "2");
        final BinomialModelPrediction pred = easyModel.predictBinomial(row);
        System.out.printf("Has penetrated the prostatic capsule (1=yes; 0=no): %s\n", pred.label);
        System.out.printf("Class probabilities: %f %f\n", pred.classProbabilities[0], pred.classProbabilities[1]);
    }

    @Test
    public void testAirlines1() throws IOException {
        GenModel model = GbmMojoModel.load(new File(TESTMODELS, "airlines1/unzipped").getAbsolutePath());
        double[] row = {68,2,2,0,6};

        double[] pred = new double[model.getPredsSize()];
        model.score0(row, 0.0, pred);

        System.out.println("Predictions: " + Arrays.asList(pred));

        double difference = Math.abs(pred[1] - 0.4719485774690201);
        Assert.assertTrue("value differs by " + difference, difference < 1e-5);

        /*
         * Expected output:
         *
         * X: 1.0000000000000000
         * X: 0.4719485774690201
         * X: 0.5280514225309799
         */

    }

    @Test(expected = PredictUnknownCategoricalLevelException.class)
    public void testAirlines1Easy() throws IOException, PredictException {
        MojoModel model = MojoModel.load(new File(TESTMODELS, "airlines1/unzipped").getAbsolutePath());
        final EasyPredictModelWrapper.Config config = new EasyPredictModelWrapper.Config();
        config.setModel(model);
        config.setConvertInvalidNumbersToNa(true);
        final EasyPredictModelWrapper easyModel = new EasyPredictModelWrapper(config);
        final RowData row = new RowData();
        row.put("Year", "68");
        row.put("Month", "2");
        row.put("DayofMonth", "2");
        row.put("DayOfWeek", "0");
        row.put("UniqueCarrier", "6");
        final BinomialModelPrediction pred = easyModel.predictBinomial(row);
        System.out.printf("Prediction result: label[%d]='%s'%n", pred.labelIndex, pred.label);
        System.out.printf("- probabilities: [0]=%f, [1]=%f%n", pred.classProbabilities[0], pred.classProbabilities[1]);
    }
}
