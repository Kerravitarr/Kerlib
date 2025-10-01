package kerlib.graphs;

public class AxisNumber<T extends Number> extends Axis<T>{
    public AxisNumber(String name, String unit) {
        super(name, unit);
    }
    @Override
    double transform(T v) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'transform'");
    }    
}
