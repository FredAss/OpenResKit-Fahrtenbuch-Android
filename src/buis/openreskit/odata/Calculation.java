package buis.openreskit.odata;

public class Calculation {
    public Double CarCalculation(Car car, CarData carData) {
        Double calculatedValue = null;
        
        int carbonProduction = carData.getCO2();
        double consumption = carData.getImperialCombined();         
        double distance = car.getDistance();
        
        double intermidiateResult = (distance / 1000) * consumption;
        
        switch (car.getFuel()) {
        case (0):
            //Benzin
            calculatedValue = car.getDistance() / car.getConsumption() * 3142;
        	intermidiateResult *= 780;
        
        
        
            break;

        case (1):
            //Diesel
            calculatedValue = car.getDistance() / car.getConsumption() * 2778;

        	intermidiateResult *= 470;
        
        
            break;
        }
        
        intermidiateResult += carbonProduction * distance;

        return intermidiateResult;
    }

    public Double FlightCalculation(Flight flight) {
        Double calculatedValue = null;

        switch (flight.getmFlighType()) {
        case (0):
            //Langstrecke
            calculatedValue = flight.getDistance() * 131.43;

            break;

        case (1):
            //Mittelstrecke
            calculatedValue = flight.getDistance() * 114.86;

            break;

        case (2):
            //Kurzstrecke
            calculatedValue = flight.getDistance() * 201.24;

            break;
        }

        return calculatedValue;
    }

    public Double PublicTransportCalculation(PublicTransport publicTransport) {
        Double calculatedValue = null;

        switch (publicTransport.getTransportType()) {
        case (0):
            //Fernzug
            calculatedValue = publicTransport.getDistance() * 17;

            break;

        case (1):
            //Regionalzug
            calculatedValue = publicTransport.getDistance() * 67;

            break;

        case (2):
            //Metro
            calculatedValue = publicTransport.getDistance() * 82;

            break;

        //Straﬂenbahn
        case (3):
            calculatedValue = publicTransport.getDistance() * 77;

            break;

        //Reisebus
        case (4):
            calculatedValue = publicTransport.getDistance() * 136;

            break;
        }

        return calculatedValue;
    }
}
