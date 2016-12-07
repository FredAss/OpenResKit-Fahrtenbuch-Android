package buis.openreskit.odata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.j256.ormlite.field.DatabaseField;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AirportPosition {
    @DatabaseField
    @JsonProperty("Id")
    private int id;
    @JsonIgnore
    @DatabaseField(id = true)
    private int internalId;
    @DatabaseField
    @JsonProperty("Order")
    private int order;
    @DatabaseField
    @JsonProperty("AirportID")
    private int airportId;
    @DatabaseField
    @JsonIgnore
    private int airportBasedFlight_Id;
    @JsonIgnore
    @DatabaseField(foreign = true)
    private Flight flight;

    public AirportPosition() {
        super();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getAirportId() {
        return airportId;
    }

    public void setAirportId(int airportId) {
        this.airportId = airportId;
    }

    public int getAirportBasedFlight_Id() {
        return airportBasedFlight_Id;
    }

    public void setAirportBasedFlight_Id(int airportBasedFlight_Id) {
        this.airportBasedFlight_Id = airportBasedFlight_Id;
    }

    public int getInternalId() {
        return internalId;
    }

    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }
}
