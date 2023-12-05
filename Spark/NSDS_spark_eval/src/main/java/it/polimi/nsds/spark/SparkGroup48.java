package it.polimi.nsds.spark;

import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.window;

/*
 * Group number: 48
 *
 * Group members
 *  - Nisanur Camuzcu
 *  - Lukas Girschick
 *  - Tugrul Kok
 */

public class SparkGroup48 {
    private static final int numCourses = 3000;

    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("SparkEval")
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> profsFields = new ArrayList<>();
        profsFields.add(DataTypes.createStructField("prof_name", DataTypes.StringType, false));
        profsFields.add(DataTypes.createStructField("course_name", DataTypes.StringType, false));
        final StructType profsSchema = DataTypes.createStructType(profsFields);

        final List<StructField> coursesFields = new ArrayList<>();
        coursesFields.add(DataTypes.createStructField("course_name", DataTypes.StringType, false));
        coursesFields.add(DataTypes.createStructField("course_hours", DataTypes.IntegerType, false));
        coursesFields.add(DataTypes.createStructField("course_students", DataTypes.IntegerType, false));
        final StructType coursesSchema = DataTypes.createStructType(coursesFields);

        final List<StructField> videosFields = new ArrayList<>();
        videosFields.add(DataTypes.createStructField("video_id", DataTypes.IntegerType, false));
        videosFields.add(DataTypes.createStructField("video_duration", DataTypes.IntegerType, false));
        videosFields.add(DataTypes.createStructField("course_name", DataTypes.StringType, false));
        final StructType videosSchema = DataTypes.createStructType(videosFields);

        // Professors: prof_name, course_name
        final Dataset<Row> profs = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(profsSchema)
                .csv(filePath + "files/profs.csv");

        // Courses: course_name, course_hours, course_students
        final Dataset<Row> courses = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(coursesSchema)
                .csv(filePath + "files/courses.csv");

        // Videos: video_id, video_duration, course_name
        final Dataset<Row> videos = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(videosSchema)
                .csv(filePath + "files/videos.csv");

        // Visualizations: value, timestamp
        // value represents the video id
        final Dataset<Row> visualizations = spark
                .readStream()
                .format("rate")
                .option("rowsPerSecond", 10)
                .load()
                .withColumn("value", col("value").mod(numCourses));

        /**
         * TODO: Enter your code below
         */

        /*
         * Query Q1. Compute the total number of lecture hours per prof
         */

        final Dataset<Row> joinedProfCourse = profs.join(courses, "course_name")
                .select("prof_name", "course_name", "course_hours", "course_students");

        final Dataset<Row> q1 = joinedProfCourse.groupBy("prof_name").sum("course_hours"); // TODO

        q1.show();

        /*
         * Query Q2. For each course, compute the total duration of all the visualizations of videos of that course,
         * computed over a minute, updated every 10 seconds
         */
        Dataset<Row> amountVisualization = videos.join(visualizations, videos.col("video_id").equalTo(visualizations.col("value")))
                .drop(visualizations.col("value"))
                .groupBy(
                        window(col("timestamp"), "60 seconds", "10 seconds"),
                        col("course_name")).sum("video_duration");

        final StreamingQuery q2 = amountVisualization.writeStream()
                .outputMode("update")
                .format("console")
                .start();


        /*
         * Query Q3. For each video, compute the total number of visualizations of that video
         * with respect to the number of students in the course in which the video is used.
         */

        Dataset<Row> joinedRatioVideoStudent = courses.join(videos.withColumnRenamed("course_name", "video_course"), courses.col("course_name").equalTo(col("video_course")))
                .drop("course_hours", "video_course")
                .select("video_id", "course_name", "course_students");


        final StreamingQuery q3 = visualizations
                .join(videos, visualizations.col("value").equalTo(videos.col("video_id")))
                .groupBy(col("video_id"))        
                .agg(org.apache.spark.sql.functions.count("value").as("count_visual"))
                .join(joinedRatioVideoStudent, "video_id")        
                .withColumn("ratio", col("count_visual").divide(col("course_students")))
                .writeStream()       
                .outputMode("update")
                .format("console")
                .start();


        try {
            q2.awaitTermination();
            q3.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }
}