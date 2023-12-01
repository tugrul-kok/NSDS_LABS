package it.polimi.nsds.spark.eval;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.OutputMode;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.Dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
This is my solution for
 */
public class Solutionfor2022 {
    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("SparkEval")
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> citiesRegionsFields = new ArrayList<>();
        citiesRegionsFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesRegionsFields.add(DataTypes.createStructField("region", DataTypes.StringType, false));
        final StructType citiesRegionsSchema = DataTypes.createStructType(citiesRegionsFields);

        final List<StructField> citiesPopulationFields = new ArrayList<>();
        citiesPopulationFields.add(DataTypes.createStructField("id", DataTypes.IntegerType, false));
        citiesPopulationFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesPopulationFields.add(DataTypes.createStructField("population", DataTypes.IntegerType, false));
        final StructType citiesPopulationSchema = DataTypes.createStructType(citiesPopulationFields);

        final Dataset<Row> citiesPopulation = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesPopulationSchema)
                .csv(filePath + "files/cities_population.csv");

        final Dataset<Row> citiesRegions = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesRegionsSchema)
                .csv(filePath + "files/cities_regions.csv");


        // TODO: add code here if necessary
        Dataset<Row> joinedData = citiesPopulation.join(citiesRegions, "city");

        final Dataset<Row> q1 = joinedData
                .groupBy("region")
                .agg(functions.sum("population").as("total_population"));

        q1.show();

        final Dataset<Row> q2 =  joinedData
                .groupBy("region")
                .agg(
                        functions.count("city").as("number_of_cities"),
                        functions.max("population").as("max_population"));

        // Bookings: the value represents the city of the booking
        q2.show();

//         JavaRDD where each element is an integer and represents the population of a city
        JavaRDD<Integer> population = citiesPopulation.toJavaRDD().map(r -> r.getInt(2));
        population.cache();
//        // TODO: add code here to produce the output for query Q3
        int year = 0;
        int totalPopulation = population.reduce(Integer::sum);

        while (totalPopulation <= 100_000_000) {
            // Calculate the new population based on the evolution rules
            population = population.map(value -> {
                if (value > 1000) {
                    // Increase by 1% for cities with more than 1000 inhabitants
                    return (int) (value * 1.01);
                } else {
                    // Decrease by 1% for cities with less than 1000 inhabitants
                    return (int) (value * 0.99);
                }
            });
            // Update the total population for the current year
            totalPopulation = population.reduce(Integer::sum);
            population.cache();

            // Print the evolution for each year
            System.out.println("Year: " + (++year) + ", total population: " + totalPopulation);
        }
        final Dataset<Row> bookings = spark
                .readStream()
                .format("rate")
                .option("rowsPerSecond", 100)
                .load();

        final StreamingQuery q4 =  bookings
                .groupBy(
                        functions.window(bookings.col("timestamp"), "30 seconds", "5 seconds"),
                        bookings.col("value"))
                .count()
                .writeStream()
                .outputMode(OutputMode.Complete())  // Use Complete mode for aggregations
                .format("console")
                .trigger(Trigger.ProcessingTime("5 seconds"))  // Adjust the trigger interval
                .start();

        try {
            q4.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }
}