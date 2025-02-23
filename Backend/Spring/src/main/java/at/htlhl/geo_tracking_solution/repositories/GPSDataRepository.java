package at.htlhl.geo_tracking_solution.repositories;


import java.time.Instant;
import java.util.List;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import at.htlhl.geo_tracking_solution.model.GPSData;
import at.htlhl.geo_tracking_solution.model.GPSData.GPSDataKey;

@Repository
public interface GPSDataRepository extends CassandraRepository<GPSData, GPSDataKey> {

    @Query("SELECT * FROM gps_data WHERE user_email = ?0 AND timestamp > ?1")
    List<GPSData> getGPSDataOfUser(String user_email, Instant timestamp);

}
