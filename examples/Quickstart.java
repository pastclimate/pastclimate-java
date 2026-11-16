import com.pastclimate.ApiException;
import com.pastclimate.ApiResponse;
import com.pastclimate.ConnectionException;
import com.pastclimate.PastClimateClient;
import com.pastclimate.Place;
import com.pastclimate.QuotaExceededException;
import com.pastclimate.RateLimitedException;
import com.pastclimate.Units;
import com.pastclimate.model.DailyRow;
import com.pastclimate.model.LocationDetail;
import com.pastclimate.model.LocationSearchResult;
import com.pastclimate.model.MeData;
import java.time.LocalDate;
import java.util.List;

/**
 * A quickstart against the live API. It runs only when PASTCLIMATE_API_KEY is set.
 *
 * <pre>
 * mvn -q package -DskipTests
 * mvn -q dependency:build-classpath -Dmdep.outputFile=cp.txt
 * PASTCLIMATE_API_KEY=... java -cp target/classes:$(cat cp.txt) examples/Quickstart.java
 * </pre>
 *
 * Optional: PASTCLIMATE_BASE_URL points it at another host (a test host, say). The last call asks for
 * weather data, which uses credits; the response says how many.
 */
public class Quickstart {
    public static void main(String[] args) {
        String key = System.getenv("PASTCLIMATE_API_KEY");
        if (key == null || key.trim().isEmpty()) {
            System.err.println("Set PASTCLIMATE_API_KEY to run this example. It makes real calls to the PastClimate API.");
            System.exit(1);
        }

        PastClimateClient client = PastClimateClient.builder().build(); // reads PASTCLIMATE_API_KEY

        try {
            ApiResponse<MeData> me = client.getMe();
            System.out.println("Plan: " + me.getData().getPlan().getName());

            ApiResponse<List<LocationSearchResult>> found = client.searchLocations("lon", "gb");
            LocationSearchResult place = found.getData().get(0);
            System.out.println("Found: " + place.getId() + " (" + place.getName() + ")");

            ApiResponse<LocationDetail> detail = client.getLocation(place.getId());
            LocalDate lastDay = detail.getData().getCoverage().getLastDate();
            System.out.println("Data for " + detail.getData().getName() + " runs to " + lastDay);

            ApiResponse<List<DailyRow>> daily =
                    client.getDaily(Place.id(place.getId()), lastDay, lastDay, Units.METRIC);
            DailyRow row = daily.getData().get(0);
            System.out.println(row.getDate() + ": " + row.getDailyMinTemp() + " to " + row.getDailyMaxTemp() + " "
                    + daily.getMeta().getUnits().getTemperature() + ", rain " + row.getDailyTotalRain() + " "
                    + daily.getMeta().getUnits().getPrecipitation());
            System.out.println("That call used " + daily.getCreditsUsed() + " credits; "
                    + daily.getCreditsRemaining() + " left.");
            System.out.println("Requests left this minute: " + daily.getRateLimit().getRemaining());
        } catch (QuotaExceededException | RateLimitedException e) {
            System.err.println("Limit reached (" + e.getCode() + "): " + e.getMessage());
            if (e.getRetryAfter() != null) {
                System.err.println("Wait " + e.getRetryAfter() + " seconds.");
            }
            System.exit(1);
        } catch (ApiException e) {
            System.err.println(e.getClass().getSimpleName() + " " + e.getStatus() + " " + e.getCode() + ": "
                    + e.getMessage() + " (" + e.getDocsUrl() + ")");
            System.exit(1);
        } catch (ConnectionException e) {
            System.err.println("Could not reach the API: " + e.getMessage());
            System.exit(1);
        }
    }
}
