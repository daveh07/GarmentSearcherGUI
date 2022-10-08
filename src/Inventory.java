import java.util.*;

public class Inventory {

    private final Set<Garment> allGarments = new HashSet<>();

    public void addGarment(Garment Garment){
        this.allGarments.add(Garment);
    }

    public Set<String> getAllBrands(GarmentType garmentType){
        Set<String> allBrands = new HashSet<>();
        for(Garment tee: allGarments){
            allBrands.add((String) tee.getGarmentSpecs().getFilter(Filter.BRAND));
        }
        allBrands.add("NA");
        return allBrands;
    }

    //EDIT Part 1.3
    /**
     * @return the value of the oldest pet available for adoption
     */
    public double highestPrice(){
        double highest = 0.0;
        for(Garment garment: allGarments){
            if(garment.getPrice()>highest) highest = garment.getPrice();
        }
        return highest;
    }

    /**
     * a method used to map pet type to relevant breeds
     * @return a Map<src.Type,Set</String> containing the mapping
     */
    public Map<GarmentType,Set<String>> getTypeToBrandMapping(){
        Map<GarmentType,Set<String>> typeToBrand = new HashMap<>();
        for(Garment garment: allGarments){
            GarmentType garmentType = (GarmentType) garment.getGarmentSpecs().getFilter(Filter.GARMENT_TYPE);
            if(!typeToBrand.containsKey(garmentType)) typeToBrand.put(garmentType,this.getAllBrands(garmentType));
        }
        return typeToBrand;
    }

    public List<Garment> findMatch(GarmentSpecs dreamGarment){
        List<Garment> matchingGarments = new ArrayList<>();
        for(Garment Garment: allGarments){
            if(!dreamGarment.matches(Garment.getGarmentSpecs())) continue;
            if(Garment.getPrice()<dreamGarment.getMinPrice()||Garment.getPrice()>dreamGarment.getMaxPrice()) continue;
            matchingGarments.add(Garment);
        }
        return matchingGarments;
    }
}
