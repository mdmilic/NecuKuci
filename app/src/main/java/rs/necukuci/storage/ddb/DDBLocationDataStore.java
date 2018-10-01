package rs.necukuci.storage.ddb;

import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import rs.necukuci.config.AWSConfig;
import rs.necukuci.model.Location;
import rs.necukuci.storage.ddb.model.GeoStoreRow;
import rs.necukuci.user.UserManager;
import timber.log.Timber;

import static rs.necukuci.storage.LocationMapper.createLocationFromLine;

public class DDBLocationDataStore extends AsyncTask<Path, Void, Void> {
    private static final int BATCH_SIZE = 100;
    private static final DynamoDBMapper DYNAMO_DB_MAPPER = DynamoDBMapper.builder()
                                                                         .dynamoDBClient(new AmazonDynamoDBClient(AWSConfig.getMobileHubCredentialsProvider()))
                                                                         .awsConfiguration(AWSConfig.getMobileHubAwsConfiguration())
                                                                         .build();

    @Override
    protected Void doInBackground(final Path... paths) {
        Timber.i("DDB Executing in background...");
        try {
            final String userID = UserManager.getUserID();
            for (final Path path : paths) {
                batchStoreInDatabase(path, userID);

                final File backupFile = new File(path.toFile().getAbsolutePath() + ".bak");
                Files.copy(path, backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Delete the file that is successfully stored in DDB and S3
                Timber.i("Attempting to delete %s", path.getFileName());
                path.toFile().delete();
            }
        } catch (final Exception e) {
            Timber.e(e, "Failed to store file in the DDB: ");
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Void aVoid) {
        super.onPostExecute(aVoid);
        Timber.i("DDB Write async task finished!");
    }

    private void batchStoreInDatabase(final Path locations, final String userID) throws Exception {

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
        final List<GeoStoreRow> geoStoreRows = new ArrayList<>();
        final String[] split = locations.getFileName().toString().split("-");
        final String tag;
        if (split.length > 1) {
            tag = split[0];
        } else {
            tag = "android";
        }
        try {
            for (final String line : lines) {
                geoStoreRows.add(convertToRow(line, userID, tag));
            }

            Timber.i("Converted file, storing!");
            // TODO: DDBMapper does partitioning by into batches of 25 items, so no point to do our own.
//            for (List<GeoStoreRow> batch : Lists.partition(new ArrayList<>(distinctTimes.values()), BATCH_SIZE)) {
//                writeBatch(batch);
//            }
            writeBatch(geoStoreRows);
        } catch (final Exception e) {
            Timber.e(e, "File %s had exception converting row: ", locations.getFileName());
            throw e;
        }
    }

    private void writeBatch(final List<GeoStoreRow> batch) throws Exception {
        final List<DynamoDBMapper.FailedBatch> failedBatches = DYNAMO_DB_MAPPER.batchSave(batch);
        Timber.i("Failures: %s", failedBatches.size());
        if (!failedBatches.isEmpty()) {
            final DynamoDBMapper.FailedBatch failedBatch = failedBatches.get(0);
            Timber.e(failedBatch.getException(), "Failed to save row %s", failedBatch.getUnprocessedItems());
            throw failedBatch.getException();
        }
    }

    private GeoStoreRow convertToRow(final String line, final String userID, final String... tags) {
        final Location location = createLocationFromLine(line);
//        Log.i(TAG, "Deserialized location: " + location);
        return GeoStoreRow.from(userID, location, tags);
    }
}