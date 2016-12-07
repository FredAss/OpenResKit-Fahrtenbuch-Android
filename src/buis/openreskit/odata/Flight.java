package buis.openreskit.odata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.Collection;


/**
 * Klasse f�r die Daten eines Energieverbrauchs.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Flight {
    @DatabaseField
    @JsonProperty("Id")
    public int id;
    @JsonIgnore
    @DatabaseField(id = true)
    public int internalId;
    @JsonProperty("Kilometrage")
    @DatabaseField
    public double distance;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    public FootprintPosition footprintPosition;
    @JsonProperty("RadiativeForcing")
    @DatabaseField
    public Boolean radiativeForcing;
    @DatabaseField
    public String calculationCategory;
    @JsonProperty("m_FlightType")
    @DatabaseField
    public int mFlighType;
    @DatabaseField
    public int startAirportNr;
    @DatabaseField
    public int endAirportNr;
    @ForeignCollectionField(eager = true)
    Collection<AirportPosition> airportPositions;

    public Flight() {
        super();
    }

    public Collection<AirportPosition> getAirportPositions() {
        return airportPositions;
    }

    public void setAirportPositions(Collection<AirportPosition> airportPositions) {
        this.airportPositions = airportPositions;
    }

    public int getInternalId() {
        return internalId;
    }

    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    /**
     * Liefert die Distanz f�r einen Flug.
     * @return distance
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Speichert die Distanz f�r einen Flug.
     * @param distance
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    //	/**
    //	 * Liefert die Emissionen eines Flugs.
    //	 * @return emission
    //	 */
    //	public Double getEmission() {
    //		return emission;
    //	}
    //	/**
    //	 * Speichert die Emissionen eines Flugs.
    //	 * @param emission
    //	 */
    //	public void setEmission(Double emission) {
    //		this.emission = emission;
    //	}
    /**
     * Liefert die Footprint Position eines Flugs.
     * @return footprintPosition
     */
    public FootprintPosition getFootprintPosition() {
        return footprintPosition;
    }

    /**
     * Speichert die Footprint Position eines Flugs.
     * @param footprintPosition
     */
    public void setFootprintPosition(FootprintPosition footprintPosition) {
        this.footprintPosition = footprintPosition;
    }

    /**
     * Liefert ob ein Flug unter Einflu� von radiative Forcing steht.
     * @return radiativeForcing
     */
    public Boolean getRadiativeForcing() {
        return radiativeForcing;
    }

    /**
     * Speichert ob ein Flug unter Einflu� von radiative Forcing steht.
     * @param radiativeForcing
     */
    public void setRadiativeForcing(Boolean radiativeForcing) {
        this.radiativeForcing = radiativeForcing;
    }

    /**
     * Liefert die Kalkulationskategorie f�r einen Flug.
     * @return calculationCategory
     */
    public String getCalculationCategory() {
        return calculationCategory;
    }

    /**
     * Speichert die Kalkulationskategorie f�r einen Flug.
     * @param calculationCategory
     */
    public void setCalculationCategory(String calculationCategory) {
        this.calculationCategory = calculationCategory;
    }

    /**
     * Liefert die ID als Prim�rschl�ssel f�r einen Flug.
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * Speichert die ID als Prim�rschl�ssel f�r einen Flug.
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Liefert den Typ der Strecke eines Flugs.
     * @return mFlighType
     */
    public int getmFlighType() {
        return mFlighType;
    }

    /**
     * Speichert den Typ der Strecke eines Flugs.
     * @param mFlighType
     */
    public void setmFlighType(int mFlighType) {
        this.mFlighType = mFlighType;
    }

    public int getStartAirportNr() {
        return startAirportNr;
    }

    public void setStartAirportNr(int startAirportNr) {
        this.startAirportNr = startAirportNr;
    }

    public int getEndAirportNr() {
        return endAirportNr;
    }

    public void setEndAirportNr(int endAirportNr) {
        this.endAirportNr = endAirportNr;
    }
}
