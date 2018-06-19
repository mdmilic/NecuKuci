package rs.necukuci.storage.ddb;

import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.common.collect.Lists;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import rs.necukuci.config.AWSConfig;
import rs.necukuci.storage.LocationMapper;
import rs.necukuci.storage.ddb.model.GeoStoreTableRow;

public class DDBLocationDataStore extends AsyncTask<Path, Void, Void> {
    private static final String TAG = DDBLocationDataStore.class.getSimpleName();
    private static final int BATCH_SIZE = 100;
    private static final LocationMapper LOCATION_MAPPER = new LocationMapper();

    private final DynamoDBMapper dynamoDBMapper;

    public DDBLocationDataStore(final AWSConfig awsConfig) {
        Log.i(TAG, "Creating DDBStore");
        this.dynamoDBMapper = DynamoDBMapper.builder()
                                            .dynamoDBClient(new AmazonDynamoDBClient(AWSConfig.getCredentialsProvider()))
                                            .awsConfiguration(AWSConfig.getAwsConfiguration())
                                            .build();
        Log.i(TAG, "Created DDBStore");
    }

    @Override
    protected Void doInBackground(final Path... paths) {
        Log.i(TAG, "Executing in background...");
        try {
            for (final Path path : paths) {
                batchStoreInDatabase(path);
                // Delete the file that is successfully stored in DDB and S3
                Log.i(TAG, "Attempting to delete " + path.getFileName());
//                path.toFile().delete();
            }
        } catch (final Exception e) {
            Log.e(TAG, "Failed to store file in the DDB: ", e);
        }
        return null;
    }

    private void batchStoreInDatabase(final Path locations) throws Exception {

//        final Function<String, GeoStoreTableRow> stringObjectFunction = new Function<String, GeoStoreTableRow>() {
//            @Override
//            public GeoStoreTableRow apply(final String s) {
//                return convertToRow(s);
//            }
//        };//l -> convertToRow(l);
//        final List<GeoStoreTableRow> geoStoreRows = Files.lines(locations)
//                                                         .map(stringObjectFunction)
//                                                         .collect(Collectors.toList());

        final List<String> lines = Files.readAllLines(locations, Charset.defaultCharset());
        final List<GeoStoreTableRow> geoStoreRows = new ArrayList<>();
        final String[] split = locations.getFileName().toString().split("-");
        final String tag;
        if (split.length == 2) {
            tag = split[0];
        } else {
            tag = "android";
        }
        Log.i(TAG, "Converting line ");
        for (final String line : lines) {
            geoStoreRows.add(convertToRow(line, tag));
        }

        for (List<GeoStoreTableRow> batch : Lists.partition(geoStoreRows, BATCH_SIZE)) {
            writeBatch(batch);
        }
    }

    private void writeBatch(final List<GeoStoreTableRow> batch) throws Exception {
        final List<DynamoDBMapper.FailedBatch> failedBatches = dynamoDBMapper.batchSave(batch);
        Log.i(TAG, "Failures: " + failedBatches.size());
        for (DynamoDBMapper.FailedBatch failedBatch : failedBatches) {
            Log.e(TAG, "Failed to save row " + failedBatch.getUnprocessedItems(), failedBatch.getException());
            throw failedBatch.getException();
        }
    }

    @VisibleForTesting
    protected GeoStoreTableRow convertToRow(final String line, final String...tags) {
        final String[] lineParts = line.split(": ");

        final Location location;
        if (lineParts.length == 2) {
            final String locationJson = lineParts[1];
            location = LOCATION_MAPPER.fromJson(locationJson);
        } else if (lineParts.length == 1) { // First few files didn't have timestamp
            final String locationJson = lineParts[0];
            location = LOCATION_MAPPER.fromJson(locationJson);
        } else {
            throw new IllegalArgumentException("Line is malformed: " + line);
        }
        return GeoStoreTableRow.from("us-east-1:6bd5c573-8cbd-4917-ba39-784747e7cb98", location, tags);
    }
}