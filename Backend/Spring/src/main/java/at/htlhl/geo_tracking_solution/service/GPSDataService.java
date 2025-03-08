package at.htlhl.geo_tracking_solution.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.htlhl.geo_tracking_solution.model.cassandra.GPSData;
import at.htlhl.geo_tracking_solution.repositories.GPSDataRepository;

import java.time.Instant;
import java.util.List;

@Service
public class GPSDataService {

    @Autowired
    private GPSDataRepository gpsDataRepository;

    public List<GPSData> getAllGPSData() {
        return gpsDataRepository.findAll();
    }

    public List<GPSData> getGPSDataOf(String userEmail, Instant timestamp) {
        return gpsDataRepository.getGPSDataOfUser(userEmail, timestamp);
    }

    public GPSData saveGPSData(GPSData gpsData) {
        return gpsDataRepository.save(gpsData);
    }

    public void deleteGPSData(GPSData gpsData) {
        gpsDataRepository.delete(gpsData);
    }
}
