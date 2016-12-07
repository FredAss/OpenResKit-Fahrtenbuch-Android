package buis.openreskit.odata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.j256.ormlite.field.DatabaseField;


/**
 * Klasse für die Daten einer Fahrt mit dem Auto.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Car {
    @DatabaseField
    @JsonProperty("Id")
    private int id;
    @JsonIgnore
    @DatabaseField(id = true)
    private int internalId;
    @JsonProperty("Consumption")
    @DatabaseField
    private double consumption;
    @JsonProperty("m_Fuel")
    @DatabaseField
    private int fuel;
    @JsonProperty("Kilometrage")
    @DatabaseField
    private double distance;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private FootprintPosition footprintPosition;
    @DatabaseField
    String calculationCategory;
    @JsonProperty("CarbonProduction")
    @DatabaseField
    private String carbonProduction;
    @JsonProperty("CarId")
    @DatabaseField
    private int carId;

    public Car() {
        super();
    }

    public int getInternalId() {
        return internalId;
    }

    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    /**
     * Liefert die Distanz einer Autofahrt.
     * @return distance
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Speichert die Distanz einer Autofahrt.
     * @param distance
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * Liefert die Calculation Category der Footprint Position.
     * Notwendig für Decorator Pattern.
     * @return calculationCategory
     */
    public String getCalculationCategory() {
        return calculationCategory;
    }

    /**
     * Speichert die Calculation Category der Footprint Position.
     * Notwendig für Decorator Pattern.
     * @param calculationCategory
     */
    public void setCalculationCategory(String calculationCategory) {
        this.calculationCategory = calculationCategory;
    }

    /**
     * Liefert die ID als Primärschlüssel der Fahrt.
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * Speichert die ID als Primärschlüssel der Fahrt.
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Liefert die FootprintPosition der Autofahrt.
     * @return footprintPosition
     */
    public FootprintPosition getFootprintPosition() {
        return footprintPosition;
    }

    /**
     * Speichert die FootprintPosition der Autofahrt.
     * @param footprintPosition
     */
    public void setFootprintPosition(FootprintPosition footprintPosition) {
        this.footprintPosition = footprintPosition;
    }

    /**
     * Liefert den Verbrauch des Fahrzeugs pro 100 km.
     * @return consumption
     */
    public double getConsumption() {
        return consumption;
    }

    /**
     * Speichert den Verbrauch des Fahrzeugs pro 100 km.
     * @param consumption
     */
    public void setConsumption(double consumption) {
        this.consumption = consumption;
    }

    /**
     * Liefert den Kraftstofftyp des Fahrzeugs.
     * @return fuel
     */
    public int getFuel() {
        return fuel;
    }

    /**
     * Speichert den Kraftstofftyp des Fahrzeugs.
     * @param fuel
     */
    public void setFuel(int fuel) {
        this.fuel = fuel;
    }

    /**
     * Liefert die Treibhausbildung die vom Typ des Fahrzeugs abhängen kann.
     * @return carbonProduction
     */
    public String getCarbonProduction() {
        return carbonProduction;
    }

    /**
     * Speichert die Treibhausbildung die vom Typ des Fahrzeugs abhängen kann.
     * @param carbonProduction
     */
    public void setCarbonProduction(String carbonProduction) {
        this.carbonProduction = carbonProduction;
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }
}
