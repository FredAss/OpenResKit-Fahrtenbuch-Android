package buis.openreskit.odata;

import android.location.Location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoLocation {

    @DatabaseField
    @JsonProperty("Id")
    private int id;
    
    @JsonIgnore
    @DatabaseField(id = true)
    public int internalId;    
    
    @DatabaseField
    @JsonProperty("Latitude")
    private double latitude;
    
    @DatabaseField
    @JsonProperty("Longitude")
    private double longitude;    
	
    @JsonIgnore
    @DatabaseField(foreign = true)
    private FootprintPosition footprintPosition;

	public int getInternalId() {
		return internalId;
	}

	public void setInternalId(int internalId) {
		this.internalId = internalId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public FootprintPosition getFootprintPosition() {
		return footprintPosition;
	}

	public void setFootprintPosition(FootprintPosition footprintPosition) {
		this.footprintPosition = footprintPosition;
	}    
    
	public GeoLocation(){
		super();
	}
	
	public GeoLocation(Location location){
		this.setLatitude(location.getLatitude());
		this.setLongitude(location.getLongitude());
	}
	
	public GeoLocation(Location location, FootprintPosition footprintPosition){
		this.setLatitude(location.getLatitude());
		this.setLongitude(location.getLongitude());
		this.setFootprintPosition(footprintPosition);
	}	
	
}
