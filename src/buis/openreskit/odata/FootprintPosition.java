package buis.openreskit.odata;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;


/**
 * Klasse für die Daten einer Footprint Position.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FootprintPosition {
    @DatabaseField
    @JsonProperty("Description")
    public String description;
    @JsonProperty("Id")
    @DatabaseField
    public int id = 0;
    @DatabaseField
    @JsonProperty("Start")
    public String date;
    @DatabaseField
    @JsonProperty("Name")
    public String name;
    @JsonIgnore
    @DatabaseField(id = true)
    public int internalId;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    public Footprint footprint;
    @DatabaseField
    @JsonProperty("CarbonFootprintCategoryId")
    public String carbonFootprintCategoryId;
    @DatabaseField
    @JsonProperty("odata.type")
    public String positionType;
    @DatabaseField
    @JsonProperty("IconId")
    public String iconId;
    @DatabaseField
    @JsonProperty("Calculation")
    public Double calculation;
    @DatabaseField
    @JsonProperty("StartLatitude")
    public Double startLat = 0d;
    @DatabaseField
    @JsonProperty("StartLongitude")
    public Double startLng = 0d;
    @DatabaseField
    @JsonProperty("DestinationLatitude")
    public Double endLat = 0d;
    @DatabaseField
    @JsonProperty("DestinationLongitude")
    public Double endLng = 0d;
    @DatabaseField
    @JsonProperty("StartName")
    public String startAddress;
    @DatabaseField
    @JsonProperty("DestinationName")
    public String endAddress;
    @DatabaseField(foreign = true)
    public Employee responsibleSubject;
    
    @ForeignCollectionField(eager = true)
    Collection<GeoLocation> geoLocations;
        
    public Collection<GeoLocation> getGeoLocations() {
		return geoLocations;
	}

	public void setGeoLocations(Collection<GeoLocation> geoLocations) {
		this.geoLocations = geoLocations;
	}

	public FootprintPosition() {
        super();
    }

    public FootprintPosition(int id) {
        this();
        this.id = id;
    }

    /**
     * Liefert die Beschreibung für eine Footprint Position.
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Speichert die Beschreibung für eine Footprint Position.
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Liefert die ID als Primärschlüssel für eine Footprint Position.
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * Speichert die ID als Primärschlüssel für eine Footprint Position.
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Liefert das Datum für eine Footprint Position.
     * @return date
     */
    public String getDate() {
        return date;
    }

    /**
     * Speichert das Datum für eine Footprint Position.
     * @param date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Liefert den Namen für eine Footprint Position.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Speichert den Namen für eine Footprint Position.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Liefert die Id auf dem Server für eine Footprint Position.
     * @return serverId
     */
    public int getInternalId() {
        return internalId;
    }

    /**
     * Speichert die Id auf dem Server für eine Footprint Position.
     * @param internalId
     */
    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    /**
     * Liefert den Footprint zu einer Footprint Position.
     * @return footprint
     */
    public Footprint getFootprint() {
        return footprint;
    }

    /**
     * Speichert den Footprint zu einer  Footprint Position.
     * @param footprint
     */
    public void setFootprint(Footprint footprint) {
        this.footprint = footprint;
    }

    /**
     * Liefert Id für die Carbon Footprint Kategorie auf dem Server für eine Footprint Position.
     * @return carbonFootprintCategoryId
     */
    public String getCarbonFootprintCategoryId() {
        return carbonFootprintCategoryId;
    }

    /**
     * Speichert Id für die Carbon Footprint Kategorie auf dem Server für eine Footprint Position.
     * @param carbonFootprintCategoryId
     */
    public void setCarbonFootprintCategoryId(String carbonFootprintCategoryId) {
        this.carbonFootprintCategoryId = carbonFootprintCategoryId;
    }

    /**
     * Liefert den Typ der Position für eine Footprint Position.
     * @return positionType
     */
    public String getPositionType() {
        return positionType;
    }

    /**
     * Speichert den Typ der Position für eine Footprint Position.
     * @param positionType
     */
    public void setPositionType(String positionType) {
        this.positionType = positionType;
    }

    /**
     * Liefert das Icon für eine Footprint Position.
     * @return iconId
     */
    public String getIconId() {
        return iconId;
    }

    /**
     * Speichert das Icon für eine Footprint Position.
     * @param iconId
     */
    public void setIconId(String iconId) {
        this.iconId = iconId;
    }

    public Double getCalculation() {
        return calculation;
    }

    public void setCalculation(Double calculation) {
        this.calculation = calculation;
    }

    public Double getStartLat() {
        return startLat;
    }

    public void setStartLat(Double startLat) {
        this.startLat = startLat;
    }

    public Double getStartLng() {
        return startLng;
    }

    public void setStartLng(Double startLng) {
        this.startLng = startLng;
    }

    public Double getEndLat() {
        return endLat;
    }

    public void setEndLat(Double endLat) {
        this.endLat = endLat;
    }

    public Double getEndLng() {
        return endLng;
    }

    public void setEndLng(Double endLng) {
        this.endLng = endLng;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public Employee getResponsibleSubject() {
        return responsibleSubject;
    }

    public void setResponsibleSubject(Employee responsibleSubject) {
        this.responsibleSubject = responsibleSubject;
    }
}
