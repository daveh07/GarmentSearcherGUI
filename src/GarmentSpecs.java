import java.util.*;

public class GarmentSpecs {
    private final double minPrice;
    private final double maxPrice;
    private final Map<Filter,Object> filterMap;

    public GarmentSpecs(Map<Filter,Object> filterMap, double minPrice, double maxPrice) {
        this.minPrice=minPrice;
        this.maxPrice=maxPrice;
        this.filterMap=new LinkedHashMap<>(filterMap);
    }

    public GarmentSpecs(Map<Filter,Object> filterMap) {
        this.filterMap=new LinkedHashMap<>(filterMap);
        minPrice = -1;
        maxPrice = -1;
    }

    public double getMinPrice() {
        return minPrice;
    }
    public double getMaxPrice() {
        return maxPrice;
    }

    public Map<Filter, Object> getAllFilters() {
        return new HashMap<>(filterMap);
    }

    public Object getFilter(Filter key){return getAllFilters().get(key);}

    public String getGarmentSpecInfo(Filter[] filter){
        StringBuilder description = new StringBuilder();
        for(Filter key: filterMap.keySet()) description.append("\n").append(key).append(": ").append(getFilter(key));
        return description.toString();
    }

    public boolean matches(GarmentSpecs garmentSpecs){
        for(Filter key : garmentSpecs.getAllFilters().keySet()) {
            if(this.getAllFilters().containsKey(key)){
                if(getFilter(key) instanceof Collection<?> && garmentSpecs.getFilter(key) instanceof Collection<?>){
                    Set<Object> intersect = new HashSet<>((Collection<?>) garmentSpecs.getFilter(key));
                    intersect.retainAll((Collection<?>) getFilter(key));
                    if(intersect.size()==0) return false;
                }
                else if(garmentSpecs.getFilter(key) instanceof Collection<?>){
                    Set<Object> items = new HashSet<>((Collection<?>) garmentSpecs.getFilter(key));
                    if(!items.contains(this.getFilter(key))) return false;
                }
                else if(!getFilter(key).equals(garmentSpecs.getFilter(key))) return false;
            }

        }
        return true;
    }


}
