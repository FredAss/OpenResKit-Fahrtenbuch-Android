package buis.openreskit.odata;

import com.j256.ormlite.field.DatabaseField;

import org.ksoap2.serialization.SoapObject;


public class CarData {
    @DatabaseField
    private int id;
    @DatabaseField(id = true)
    private int internalId;
    @DatabaseField
    private String manufactur;
    @DatabaseField
    private String model;
    @DatabaseField
    private String description;
    @DatabaseField
    private double imperialCombined;
    @DatabaseField
    private int CO2;

    public double getImperialCombined() {
		return imperialCombined;
	}

	public void setImperialCombined(double imperialCombined) {
		this.imperialCombined = imperialCombined;
	}

	public int getCO2() {
		return CO2;
	}

	public void setCO2(int cO2) {
		CO2 = cO2;
	}

	public CarData() {
        super();
    }

    public CarData(SoapObject carData) {
        super();
        setId(Integer.parseInt(carData.getPropertyAsString("Id")));
        setInternalId(Integer.parseInt(carData.getPropertyAsString("Id")));
        setDescription(carData.getPropertyAsString("Description"));
        setManufactur(carData.getPropertyAsString("Manufacturer"));
        setModel(carData.getPropertyAsString("Model"));
        setImperialCombined(Double.parseDouble(carData.getPropertyAsString("ImperialCombined")));
        setCO2(Integer.parseInt(carData.getPropertyAsString("CO2")));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInternalId() {
        return internalId;
    }

    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    public String getManufactur() {
        return manufactur;
    }

    public void setManufactur(String manufactur) {
        this.manufactur = manufactur;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
