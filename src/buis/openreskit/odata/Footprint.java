package buis.openreskit.odata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.Collection;


/**
 * Klasse f�r die Daten eines Footprints.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Footprint {
    @DatabaseField
    @JsonProperty("Id")
    int id;
    @DatabaseField
    @JsonProperty("Employees")
    int employees;
    @DatabaseField
    @JsonProperty("Name")
    String name;
    @DatabaseField
    @JsonProperty("BalanceYear")
    String balanceYear;
    @DatabaseField
    @JsonProperty("Description")
    String description;
    @DatabaseField(id = true)
    @JsonIgnore
    int internalId;
    @DatabaseField
    @JsonProperty("SiteLocation")
    String siteLocation;
    @DatabaseField
    @JsonProperty("Calculation")
    int calculation;
    @ForeignCollectionField(eager = true)
    @JsonProperty("Positions")
    Collection<FootprintPosition> footprintPositions;
    @DatabaseField
    private int nr;
    @DatabaseField
    public String calculationCategory;

    public Footprint() {
        super();
    }

    public Footprint(int id) {
        this();
        this.id = id;
    }

    /**
     * Liefert die Anzahl der Mitarbeiter in einem Werk f�r einen Footprint
     * @return employees
     */
    public int getEmployees() {
        return employees;
    }

    /**
     * Speichert die Anzahl der Mitarbeiter in einem Werk f�r einen Footprint
     * @param employees
     */
    public void setEmployees(int employees) {
        this.employees = employees;
    }

    /**
     * Liefert den Namen f�r einen Footprint.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Speichert den Namen f�r einen Footprint.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Liefert das Bilanzjahr f�r einen Footprint.
     * @return balanceYear
     */
    public String getBalanceYear() {
        return balanceYear;
    }

    /**
     * Speichert das Bilanzjahr f�r einen Footprint.
     * @param balanceYear
     */
    public void setBalanceYear(String balanceYear) {
        this.balanceYear = balanceYear;
    }

    /**
     * Liefert die Beschreibung f�r einen Footprint.
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Speichert die Beschreibung f�r einen Footprint.
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Liefert die Id als Prim�rschl�ssel f�r einen Footprint.
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * Speichert die Id als Prim�rschl�ssel f�r einen Footprint.
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Liefert die Id auf dem Server f�r einen Footprint.
     * @return serverId
     */
    public int getInternalId() {
        return internalId;
    }

    /**
     * Speichert die Id auf dem Server f�r einen Footprint.
     * @param serverId
     */
    public void setInternalId(int serverId) {
        this.internalId = serverId;
    }

    /**
     * Liefert Footprint Positionen f�r einen Footprint.
     * @return footprintPositions
     */
    public Collection<FootprintPosition> getFootprintPositions() {
        return footprintPositions;
    }

    /**
     * Speichert Footprint Positionen f�r einen Footprint.
     * @param footprintPositions
     */
    public void setFootprintPositions(Collection<FootprintPosition> footprintPositions) {
        this.footprintPositions = footprintPositions;
    }

    /**
     * Liefert die Location eines Standortes f�r einen Footprint.
     * @return siteLocation
     */
    public String getSiteLocation() {
        return siteLocation;
    }

    /**
     * Speichert die Location eines Standortes f�r einen Footprint.
     * @param siteLocation
     */
    public void setSiteLocation(String siteLocation) {
        this.siteLocation = siteLocation;
    }

    /**
     * Liefert die gesamten Emissionen f�r einen Footprint.
     * @return calculation
     */
    public int getCalculation() {
        return calculation;
    }

    /**
     * Speichert die gesamten Emissionen f�r einen Footprint.
     * @param calculation
     */
    public void setCalculation(int calculation) {
        this.calculation = calculation;
    }

    /**
     * Speichert Nummer eines Footprints f�r die Listensortierung.
     * @return nr
     */
    public int getNr() {
        return nr;
    }

    /**
     * Speichert Nummer eines Footprints f�r die Listensortierung.
     * @param nr
     */
    public void setNr(int nr) {
        this.nr = nr;
    }

    /**
     * Liefert Berechnungskategorie eines Footprints.
     * @return calculationCategory
     */
    public String getCalculationCategory() {
        return calculationCategory;
    }

    /**
     * Speichert Berechnungskategorie eines Footprints.
     * @param calculationCategory
     */
    public void setCalculationCategory(String calculationCategory) {
        this.calculationCategory = calculationCategory;
    }
}
