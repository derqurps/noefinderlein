package at.qurps.noefinderlein.app.APIData;

import java.util.List;

import at.qurps.noefinderlein.app.APIData.CurrentIds;
import at.qurps.noefinderlein.app.APIData.OpenData;
import at.qurps.noefinderlein.app.DB_Location_NoeC;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface NoefinderleinAPI {

    @GET("Changevals/getCurrentIds")
    Call<CurrentIds> loadChanges(@Query("year") Integer year);

    @PUT("Locations/getChangedDestinationIds")
    Call<List<Integer>> getChangedDestinationIds(@Body RequestBody body);


    @PUT("Locations/getLocationsToIds")
    Call<List<DB_Location_NoeC>> getLocationsToIds(@Body RequestBody body);

    @GET("Locations/findAllIdsToYear")
    Call<List<Integer>> findAllIdsToYear(@Query("year") Integer year);


    @GET("Days/getChangeSegmentCount")
    Call<List<OpenData>> getChangeSegmentCount(@Query("year") Integer year, @Query("changeStart") Integer changeStart, @Query("count") Integer count);
}
